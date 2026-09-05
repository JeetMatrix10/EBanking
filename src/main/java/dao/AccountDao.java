package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import model.Account;
import utilities.ConnectionFactory;

public class AccountDao {

	// Why this returns a List<Account>, not just account numbers as Strings:
	// the dropdown will likely want to show more than just the number later
	// (e.g. "ACC00001 - Saving" instead of a bare number) — returning full
	// Account objects keeps that option open without refetching later.
	public List<Account> getAccountsByCid(String cid) {
	    String sql = "SELECT * FROM account WHERE cid = ?";
	    List<Account> accounts = new ArrayList<>();

	    try (Connection conn = ConnectionFactory.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, cid);
	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	            Account account = new Account();
	            account.setAccno(rs.getString("accno"));
	            account.setCid(rs.getString("cid"));
	            account.setOpendate(rs.getDate("opendate"));
	            account.setBalance(rs.getBigDecimal("balance"));
	            account.setAccounttype(rs.getString("accounttype"));
	            accounts.add(account);
	        }

	    } catch (SQLException e) {
	        System.out.println("Error fetching accounts: " + e.getMessage());
	    }

	    return accounts;
	}
	// Why we take the OLD balance first, calculate the difference, and log it
	// as a transaction: this preserves the audit trail principle from
	// deposit/withdraw — even an admin's manual correction should leave a
	// record of what changed and by how much, not just silently overwrite a number.
	public boolean updateAccountBalance(String accno, BigDecimal newBalance) {
	    String getOldBalanceSql = "SELECT balance FROM account WHERE accno = ?";
	    String updateSql = "UPDATE account SET balance = ? WHERE accno = ?";
	    String logSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
	                   + "VALUES (?, NULL, ?, 'ADMIN_ADJUSTMENT')";

	    Connection conn = null;
	    try {
	        conn = ConnectionFactory.getConnection();
	        conn.setAutoCommit(false);

	        BigDecimal oldBalance;
	        try (PreparedStatement getPs = conn.prepareStatement(getOldBalanceSql)) {
	            getPs.setString(1, accno);
	            ResultSet rs = getPs.executeQuery();
	            if (!rs.next()) {
	                conn.rollback();
	                return false;
	            }
	            oldBalance = rs.getBigDecimal("balance");
	        }

	        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
	            updatePs.setBigDecimal(1, newBalance);
	            updatePs.setString(2, accno);
	            updatePs.executeUpdate();
	        }

	        // Why we log (newBalance - oldBalance), not newBalance itself: the
	        // transaction table's "amount" column represents a CHANGE in value
	        // everywhere else (deposit amount, withdrawal amount) — logging the
	        // raw new balance instead would be inconsistent with every other row
	        // in that table and confusing to read later.
	        BigDecimal difference = newBalance.subtract(oldBalance);
	        try (PreparedStatement logPs = conn.prepareStatement(logSql)) {
	            logPs.setString(1, accno);
	            logPs.setBigDecimal(2, difference);
	            logPs.executeUpdate();
	        }

	        conn.commit();
	        return true;

	    } catch (SQLException e) {
	        System.out.println("Error updating account balance: " + e.getMessage());
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
	// Why this returns boolean instead of the Account itself: the caller just
	// needs a yes/no answer to decide whether to proceed or block — reusing
	// getAccountByAccno() and comparing the cid ourselves would work too, but
	// a dedicated method makes the INTENT ("check ownership") clear at the call site.
	public boolean isAccountOwnedByCustomer(String accno, String cid) {
	    Account account = getAccountByAccno(accno);
	    return account != null && account.getCid().equals(cid);
	}
	// Why account numbers get their own generator, separate from CID's:
    // account numbers follow a different business format in most banks
    // (often longer, sometimes with a branch code prefix) — keeping this
    // logic in AccountDao (not shared with CustomerDao) keeps each DAO
    // responsible only for its own table's ID scheme.
    private String generateNextAccno(Connection conn) throws SQLException {
        String sql = "SELECT accno FROM account ORDER BY accno DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastAccno = rs.getString("accno"); // e.g. "ACC00001"
                int number = Integer.parseInt(lastAccno.substring(3));
                number++;
                return "ACC" + String.format("%05d", number);
            } else {
                return "ACC00001";
            }
        }
    }

    // Why this checks cid exists BEFORE inserting, even though the database's
    // FOREIGN KEY constraint would also catch this: a foreign key violation
    // throws a raw SQLException with a technical message ("cannot add or
    // update a child row..."), which is bad UX to show a bank admin. Checking
    // first lets us return a clear, specific reason instead.
    public String addAccount(Account account) {
        String checkCidSql = "SELECT cid FROM customer WHERE cid = ?";
        String insertSql = "INSERT INTO account (accno, cid, opendate, balance, accounttype) "
                          + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {

            try (PreparedStatement checkPs = conn.prepareStatement(checkCidSql)) {
                checkPs.setString(1, account.getCid());
                ResultSet rs = checkPs.executeQuery();
                if (!rs.next()) {
                    System.out.println("Error adding account: CID does not exist");
                    return null;
                }
            }

            String newAccno = generateNextAccno(conn);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, newAccno);
                ps.setString(2, account.getCid());
                ps.setDate(3, account.getOpendate());
                ps.setBigDecimal(4, account.getBalance());
                ps.setString(5, account.getAccounttype());

                int rowsInserted = ps.executeUpdate();
                return rowsInserted > 0 ? newAccno : null;
            }

        } catch (SQLException e) {
            System.out.println("Error adding account: " + e.getMessage());
            return null;
        }
    }

    // Why this method exists: DepositServlet, WithdrawServlet, and
    // TransferServlet will all need to fetch an account's current balance
    // before modifying it.
    public Account getAccountByAccno(String accno) {
        String sql = "SELECT * FROM account WHERE accno = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accno);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Account account = new Account();
                account.setAccno(rs.getString("accno"));
                account.setCid(rs.getString("cid"));
                account.setOpendate(rs.getDate("opendate"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccounttype(rs.getString("accounttype"));
                return account;
            }

        } catch (SQLException e) {
            System.out.println("Error fetching account: " + e.getMessage());
        }

        return null;
    }
}