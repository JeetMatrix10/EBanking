package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import model.TransferResult;
import exception.InsufficientBalanceException;
import java.util.ArrayList;
import java.util.List;

import model.Transaction;
import utilities.ConnectionFactory;

public class TransactionDao {

	// Why this returns a List<Transaction>, not a single Transaction: a customer's
	// history page needs every past transaction for their account, not just one
	// row.
	public List<Transaction> getTransactionsByAccno(String accno) {
		String sql = "SELECT * FROM transaction WHERE saccno = ? OR benaccno = ? ORDER BY transdt DESC";
		List<Transaction> transactions = new ArrayList<>();

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			// Why saccno = ? OR benaccno = ?: an account can appear as EITHER
			// the sender or the receiver in a transfer, so both need to be
			// checked to show the account's complete history, not just outgoing
			// transactions.
			ps.setString(1, accno);
			ps.setString(2, accno);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Transaction t = new Transaction();
				t.setTransactionId(rs.getInt("transaction_id"));
				t.setSaccno(rs.getString("saccno"));
				t.setBenaccno(rs.getString("benaccno"));
				t.setAmount(rs.getBigDecimal("amount"));
				t.setTransdt(rs.getTimestamp("transdt"));
				t.setType(rs.getString("type"));
				transactions.add(t);
			}

		} catch (SQLException e) {
			System.out.println("Error fetching transactions: " + e.getMessage());
		}

		return transactions;
	}

	private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("5000.00");

	// Why the method signature changed from "boolean ... throws
	// InsufficientBalanceException"
	// to "TransferResult ...": since minimum balance is now just a warning, not a
	// hard failure, there's no exception to throw for that case anymore — we
	// only need real exceptions for genuine account-not-found style errors, and
	// even those we're handling as a false/failed result here rather than a throw.
	public TransferResult transfer(String saccno, String benaccno, BigDecimal amount) {

		String getBalanceSql = "SELECT balance FROM account WHERE accno = ? FOR UPDATE";
		String checkBenAccSql = "SELECT accno FROM account WHERE accno = ?";
		String debitSql = "UPDATE account SET balance = balance - ? WHERE accno = ?";
		String creditSql = "UPDATE account SET balance = balance + ? WHERE accno = ?";
		String insertTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
				+ "VALUES (?, ?, ?, 'TRANSFER')";

		Connection conn = null;
		try {
			conn = ConnectionFactory.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement checkPs = conn.prepareStatement(checkBenAccSql)) {
				checkPs.setString(1, benaccno);
				ResultSet rs = checkPs.executeQuery();
				if (!rs.next()) {
					conn.rollback();
					return new TransferResult(false, null);
				}
			}

			BigDecimal senderBalance;
			try (PreparedStatement selectPs = conn.prepareStatement(getBalanceSql)) {
				selectPs.setString(1, saccno);
				ResultSet rs = selectPs.executeQuery();
				if (!rs.next()) {
					conn.rollback();
					return new TransferResult(false, null);
				}
				senderBalance = rs.getBigDecimal("balance");
			}

			// Why senderBalance < amount is still a HARD failure (unlike the
			// minimum balance check below): this isn't a policy warning, it's
			// physically impossible — you cannot send money you don't have at all.
			if (senderBalance.compareTo(amount) < 0) {
				conn.rollback();
				return new TransferResult(false, null);
			}

			BigDecimal balanceAfterTransfer = senderBalance.subtract(amount);

			// Why this is just a variable now, not a throw: we calculate the
			// warning message here but DON'T stop execution — the debit/credit
			// below still runs regardless of whether this is null or not.
			String warning = null;
			if (balanceAfterTransfer.compareTo(MINIMUM_BALANCE) < 0) {
				warning = "Warning: your balance (" + balanceAfterTransfer
						+ ") is now below the minimum required balance of " + MINIMUM_BALANCE + ".";
			}

			try (PreparedStatement debitPs = conn.prepareStatement(debitSql)) {
				debitPs.setBigDecimal(1, amount);
				debitPs.setString(2, saccno);
				debitPs.executeUpdate();
			}

			try (PreparedStatement creditPs = conn.prepareStatement(creditSql)) {
				creditPs.setBigDecimal(1, amount);
				creditPs.setString(2, benaccno);
				creditPs.executeUpdate();
			}

			try (PreparedStatement insertPs = conn.prepareStatement(insertTransactionSql)) {
				insertPs.setString(1, saccno);
				insertPs.setString(2, benaccno);
				insertPs.setBigDecimal(3, amount);
				insertPs.executeUpdate();
			}

			conn.commit();
			return new TransferResult(true, warning);

		} catch (SQLException e) {
			System.out.println("Error processing transfer: " + e.getMessage());
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					System.out.println("Rollback failed: " + ex.getMessage());
				}
			}
			return new TransferResult(false, null);

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("Error closing connection: " + e.getMessage());
				}
			}
		}
	}

	public boolean withdraw(String accno, BigDecimal amount) throws InsufficientBalanceException {
		String getBalanceSql = "SELECT balance FROM account WHERE accno = ? FOR UPDATE";
		String updateBalanceSql = "UPDATE account SET balance = balance - ? WHERE accno = ?";
		String insertTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
				+ "VALUES (?, NULL, ?, 'WITHDRAW')";

		Connection conn = null;
		try {
			conn = ConnectionFactory.getConnection();
			conn.setAutoCommit(false);

			BigDecimal currentBalance;

			// Why "FOR UPDATE" in the SELECT: this locks the row for the rest of
			// this transaction, so if two withdrawal requests for the SAME account
			// hit the server at nearly the same moment, the second one waits until
			// the first fully commits or rolls back. Without this lock, both could
			// read the same "before" balance simultaneously and both succeed,
			// letting someone withdraw more than they actually have.
			try (PreparedStatement selectPs = conn.prepareStatement(getBalanceSql)) {
				selectPs.setString(1, accno);
				ResultSet rs = selectPs.executeQuery();

				if (!rs.next()) {
					conn.rollback();
					return false; // account doesn't exist
				}
				currentBalance = rs.getBigDecimal("balance");
			}

			// Why this check happens in Java, not relying only on a database
			// constraint: it lets us throw a specific, descriptive exception
			// ("Insufficient balance") that the servlet can catch and show to
			// the user, instead of a generic SQL error.
			if (currentBalance.compareTo(amount) < 0) {
				conn.rollback();
				throw new InsufficientBalanceException("Insufficient balance. Available: " + currentBalance);
			}

			try (PreparedStatement updatePs = conn.prepareStatement(updateBalanceSql)) {
				updatePs.setBigDecimal(1, amount);
				updatePs.setString(2, accno);
				updatePs.executeUpdate();
			}

			try (PreparedStatement insertPs = conn.prepareStatement(insertTransactionSql)) {
				insertPs.setString(1, accno);
				insertPs.setBigDecimal(2, amount);
				insertPs.executeUpdate();
			}

			conn.commit();
			return true;

		} catch (SQLException e) {
			System.out.println("Error processing withdrawal: " + e.getMessage());
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					System.out.println("Rollback failed: " + ex.getMessage());
				}
			}
			return false;

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("Error closing connection: " + e.getMessage());
				}
			}
		}
	}

	// Why this returns boolean, not the transaction id: the caller (servlet)
	// only needs to know success/failure to decide what message to show —
	// it doesn't need to display a transaction ID anywhere yet.
	public boolean deposit(String accno, BigDecimal amount) {
		String updateBalanceSql = "UPDATE account SET balance = balance + ? WHERE accno = ?";
		String insertTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
				+ "VALUES (?, NULL, ?, 'DEPOSIT')";

		Connection conn = null;
		try {
			conn = ConnectionFactory.getConnection();

			// Why setAutoCommit(false): by default, every single SQL statement
			// commits (saves permanently) the instant it runs. That's a problem
			// here — if the balance UPDATE succeeds but the transaction INSERT
			// then fails, we'd be left with money added but no record of why.
			// Turning off auto-commit lets us treat both statements as ONE
			// all-or-nothing unit, using conn.commit() / conn.rollback() below.
			conn.setAutoCommit(false);

			try (PreparedStatement updatePs = conn.prepareStatement(updateBalanceSql)) {
				updatePs.setBigDecimal(1, amount);
				updatePs.setString(2, accno);
				int updated = updatePs.executeUpdate();

				if (updated == 0) {
					// Why we rollback and return here: if no row was updated,
					// the account number doesn't exist — nothing to deposit into.
					conn.rollback();
					return false;
				}
			}

			try (PreparedStatement insertPs = conn.prepareStatement(insertTransactionSql)) {
				insertPs.setString(1, accno);
				insertPs.setBigDecimal(2, amount);
				insertPs.executeUpdate();
			}

			// Why commit() here specifically: only once BOTH statements have
			// succeeded do we make the changes permanent. This is the "success"
			// path of the all-or-nothing unit described above.
			conn.commit();
			return true;

		} catch (SQLException e) {
			System.out.println("Error processing deposit: " + e.getMessage());
			// Why rollback in the catch block too: if anything threw an
			// exception mid-way (e.g. after the balance update but before the
			// transaction insert), this undoes the balance change too — so
			// the account never ends up in a half-updated state.
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					System.out.println("Rollback failed: " + ex.getMessage());
				}
			}
			return false;

		} finally {
			// Why we manually close here instead of try-with-resources: this
			// method needed the connection to stay open across MULTIPLE
			// statements and a commit/rollback decision — try-with-resources
			// closes too early for that pattern, so we manage it by hand.
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("Error closing connection: " + e.getMessage());
				}
			}
		}
	}
}