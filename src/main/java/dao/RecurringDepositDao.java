package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.RecurringDeposit;
import utilities.ConnectionFactory;

public class RecurringDepositDao {

	// Why testMode scales months into minutes here (same ratio idea as FD's
	// years-to-minutes, but applied to months since RD's natural unit is months):
	// this lets the very FIRST installment debit happen quickly for demo purposes,
	// then each SUBSEQUENT installment follow the same scaled interval.
	public boolean bookRD(RecurringDeposit rd, boolean testMode) {
		// Why interest_rate is now part of this INSERT: the RD needs its
		// own agreed rate stored at booking time, same principle as FD —
		// without storing it here, there'd be no rate to use later when
		// calculating the maturity payout.
		String insertSql = "INSERT INTO recurring_deposit "
				+ "(cid, accno, monthly_amount, no_of_months, interest_rate, installments_paid, book_date, next_debit_date, status) "
				+ "VALUES (?, ?, ?, ?, ?, 0, ?, ?, 'ACTIVE')";

		try (Connection conn = ConnectionFactory.getConnection()) {

			LocalDateTime bookDateTime = rd.getBookDate().toLocalDateTime();

			// Why the first debit is scheduled immediately at "next interval"
			// rather than at booking time itself: RD installments are paid
			// AHEAD for each period, similar to how a real RD's first
			// installment is due one month after opening, not on day one.
			LocalDateTime firstDebit = testMode ? bookDateTime.plusMinutes(5) : bookDateTime.plusMonths(1);

			try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
				ps.setString(1, rd.getCid());
				ps.setString(2, rd.getAccno());
				ps.setBigDecimal(3, rd.getMonthlyAmount());
				ps.setInt(4, rd.getNoOfMonths());
				ps.setBigDecimal(5, rd.getInterestRate());
				ps.setTimestamp(6, rd.getBookDate());
				ps.setTimestamp(7, Timestamp.valueOf(firstDebit));
				int rows = ps.executeUpdate();
				return rows > 0;
			}

		} catch (SQLException e) {
			System.out.println("Error booking RD: " + e.getMessage());
			return false;
		}
	}

	// Why this exists as its own method: same reasoning as FixedDepositDao's
	// calculateSimpleInterest() extraction — this formula belongs in exactly
	// ONE place, not duplicated inline wherever RD maturity is calculated.
	//
	// Formula: Total Interest = P × n(n+1)/2 × (r / 1200)
	// Maturity Amount = (P × n) + Total Interest
	// Why n(n+1)/2, not just n: each installment sits in the bank for a
	// DIFFERENT length of time — the 1st installment earns interest for the
	// full n months, the 2nd for (n-1) months, ..., the last for just 1
	// month. n(n+1)/2 is the standard mathematical shortcut that sums
	// exactly this declining series (n + (n-1) + ... + 1), avoiding the need
	// to loop over each installment individually to add up its own interest.
	// Why divide by 1200, not 100: dividing by 100 converts the rate from a
	// percentage to a decimal; dividing by an ADDITIONAL 12 converts the
	// ANNUAL rate into a MONTHLY rate, since installments are monthly, not
	// yearly, unlike FD's per-year calculation.
	private BigDecimal calculateRDMaturityAmount(BigDecimal monthlyAmount, int noOfMonths,
			BigDecimal annualRatePercent) {
		BigDecimal n = BigDecimal.valueOf(noOfMonths);
		BigDecimal totalPrincipal = monthlyAmount.multiply(n);

		BigDecimal interest = monthlyAmount.multiply(n).multiply(n.add(BigDecimal.ONE))
				.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP).multiply(annualRatePercent)
				.divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);

		return totalPrincipal.add(interest);
	}

	// Why this is structured almost identically to processMaturedDeposits():
	// both are "find due records, act on them, advance/close their status" —
	// same shape, different specifics (recurring debit vs one-time maturity).
	public void processDueInstallments(boolean testMode) {
		String findDueSql = "SELECT * FROM recurring_deposit WHERE status = 'ACTIVE' AND next_debit_date <= NOW()";
		String getBalanceSql = "SELECT balance FROM account WHERE accno = ? FOR UPDATE";
		String debitSql = "UPDATE account SET balance = balance - ? WHERE accno = ?";
		String logTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
				+ "VALUES (?, NULL, ?, 'RD_INSTALLMENT')";
		String advanceSql = "UPDATE recurring_deposit "
				+ "SET installments_paid = installments_paid + 1, next_debit_date = ? WHERE rd_id = ?";

		// Why "MATURED" now, not "COMPLETED": aligning terminology with
		// FixedDeposit's status naming ('ACTIVE' -> 'MATURED') — both
		// represent the same real-world event (a deposit product reaching
		// the end of its agreed term and paying out), so using the same
		// word for the same concept keeps the whole project's vocabulary
		// consistent rather than having two different words mean the same thing.
		String completeSql = "UPDATE recurring_deposit SET status = 'COMPLETED' WHERE rd_id = ?";

		// Why this credit statement and transaction type are new: this is
		// the entire fix for the "money vanishes" gap — without this,
		// finishing an RD did nothing but change a status label, with no
		// money ever returning to the customer.
		String creditMaturitySql = "UPDATE account SET balance = balance + ? WHERE accno = ?";
		String logMaturitySql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
				+ "VALUES (?, NULL, ?, 'RD_MATURED')";

		try (Connection conn = ConnectionFactory.getConnection();
				PreparedStatement findPs = conn.prepareStatement(findDueSql);
				ResultSet rs = findPs.executeQuery()) {

			while (rs.next()) {
				int rdId = rs.getInt("rd_id");
				String accno = rs.getString("accno");
				BigDecimal monthlyAmount = rs.getBigDecimal("monthly_amount");
				int noOfMonths = rs.getInt("no_of_months");
				BigDecimal interestRate = rs.getBigDecimal("interest_rate");
				int installmentsPaid = rs.getInt("installments_paid");
				Timestamp currentNextDebit = rs.getTimestamp("next_debit_date");

				BigDecimal currentBalance;
				try (PreparedStatement getPs = conn.prepareStatement(getBalanceSql)) {
					getPs.setString(1, accno);
					ResultSet balRs = getPs.executeQuery();
					if (!balRs.next())
						continue;
					currentBalance = balRs.getBigDecimal("balance");
				}

				// Why insufficient balance just SKIPS this cycle instead of
				// failing/closing the RD: a real bank would typically retry
				// next cycle rather than permanently cancel someone's RD over
				// one missed payment — this mirrors that leniency.
				if (currentBalance.compareTo(monthlyAmount) < 0) {
					System.out.println("RD #" + rdId + ": insufficient balance, skipping this cycle.");
					continue;
				}

				try (PreparedStatement debitPs = conn.prepareStatement(debitSql)) {
					debitPs.setBigDecimal(1, monthlyAmount);
					debitPs.setString(2, accno);
					debitPs.executeUpdate();
				}

				try (PreparedStatement logPs = conn.prepareStatement(logTransactionSql)) {
					logPs.setString(1, accno);
					logPs.setBigDecimal(2, monthlyAmount);
					logPs.executeUpdate();
				}

				int newInstallmentsPaid = installmentsPaid + 1;

				if (newInstallmentsPaid >= noOfMonths) {
					// Why the maturity payout happens HERE, in the same
					// cycle as the FINAL installment debit, not as a
					// separate later step: once the last installment is
					// paid, the RD term is immediately complete — there's
					// no reason to wait for a future check to pay it out
					BigDecimal maturityAmount = calculateRDMaturityAmount(monthlyAmount, noOfMonths, interestRate);

					try (PreparedStatement creditPs = conn.prepareStatement(creditMaturitySql)) {
						creditPs.setBigDecimal(1, maturityAmount);
						creditPs.setString(2, accno);
						creditPs.executeUpdate();
					}

					try (PreparedStatement logMaturityPs = conn.prepareStatement(logMaturitySql)) {
						logMaturityPs.setString(1, accno);
						logMaturityPs.setBigDecimal(2, maturityAmount);
						logMaturityPs.executeUpdate();
					}

					try (PreparedStatement completePs = conn.prepareStatement(completeSql)) {
						completePs.setInt(1, rdId);
						completePs.executeUpdate();
					}

					System.out.println("RD #" + rdId + " matured. Credited " + maturityAmount + " to " + accno);
				} else {
					LocalDateTime nextDebit = testMode ? currentNextDebit.toLocalDateTime().plusMinutes(5)
							: currentNextDebit.toLocalDateTime().plusMonths(1);

					try (PreparedStatement advancePs = conn.prepareStatement(advanceSql)) {
						advancePs.setTimestamp(1, Timestamp.valueOf(nextDebit));
						advancePs.setInt(2, rdId);
						advancePs.executeUpdate();
					}
					System.out.println(
							"RD #" + rdId + " installment " + newInstallmentsPaid + "/" + noOfMonths + " debited.");
				}
			}

		} catch (SQLException e) {
			System.out.println("Error processing RD installments: " + e.getMessage());
		}
	}

	public List<RecurringDeposit> getRDsByCid(String cid) {
		String sql = "SELECT * FROM recurring_deposit WHERE cid = ? ORDER BY book_date DESC";
		List<RecurringDeposit> rds = new ArrayList<>();

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cid);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				RecurringDeposit rd = new RecurringDeposit();
				rd.setRdId(rs.getInt("rd_id"));
				rd.setCid(rs.getString("cid"));
				rd.setAccno(rs.getString("accno"));
				rd.setMonthlyAmount(rs.getBigDecimal("monthly_amount"));
				rd.setNoOfMonths(rs.getInt("no_of_months"));
				rd.setInterestRate(rs.getBigDecimal("interest_rate"));
				rd.setInstallmentsPaid(rs.getInt("installments_paid"));
				rd.setBookDate(rs.getTimestamp("book_date"));
				rd.setNextDebitDate(rs.getTimestamp("next_debit_date"));
				rd.setStatus(rs.getString("status"));
				rds.add(rd);
			}

		} catch (SQLException e) {
			System.out.println("Error fetching RDs: " + e.getMessage());
		}

		return rds;
	}
}