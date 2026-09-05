package dao;

import java.math.BigDecimal;
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
        String insertSql = "INSERT INTO recurring_deposit "
                + "(cid, accno, monthly_amount, no_of_months, installments_paid, book_date, next_debit_date, status) "
                + "VALUES (?, ?, ?, ?, 0, ?, ?, 'ACTIVE')";

        try (Connection conn = ConnectionFactory.getConnection()) {

            LocalDateTime bookDateTime = rd.getBookDate().toLocalDateTime();

            // Why the first debit is scheduled immediately at "next interval"
            // rather than at booking time itself: RD installments are paid
            // AHEAD for each period, similar to how a real RD's first
            // installment is due one month after opening, not on day one.
            LocalDateTime firstDebit = testMode
                    ? bookDateTime.plusMinutes(5)
                    : bookDateTime.plusMonths(1);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, rd.getCid());
                ps.setString(2, rd.getAccno());
                ps.setBigDecimal(3, rd.getMonthlyAmount());
                ps.setInt(4, rd.getNoOfMonths());
                ps.setTimestamp(5, rd.getBookDate());
                ps.setTimestamp(6, Timestamp.valueOf(firstDebit));
                int rows = ps.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error booking RD: " + e.getMessage());
            return false;
        }
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
        String completeSql = "UPDATE recurring_deposit SET status = 'COMPLETED' WHERE rd_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement findPs = conn.prepareStatement(findDueSql);
             ResultSet rs = findPs.executeQuery()) {

            while (rs.next()) {
                int rdId = rs.getInt("rd_id");
                String accno = rs.getString("accno");
                BigDecimal monthlyAmount = rs.getBigDecimal("monthly_amount");
                int noOfMonths = rs.getInt("no_of_months");
                int installmentsPaid = rs.getInt("installments_paid");
                Timestamp currentNextDebit = rs.getTimestamp("next_debit_date");

                BigDecimal currentBalance;
                try (PreparedStatement getPs = conn.prepareStatement(getBalanceSql)) {
                    getPs.setString(1, accno);
                    ResultSet balRs = getPs.executeQuery();
                    if (!balRs.next()) continue;
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
                    // Why we mark COMPLETED instead of scheduling another
                    // next_debit_date: once all installments are paid, there's
                    // nothing left to debit — continuing to advance the date
                    // would let it keep debiting forever.
                    try (PreparedStatement completePs = conn.prepareStatement(completeSql)) {
                        completePs.setInt(1, rdId);
                        completePs.executeUpdate();
                    }
                    System.out.println("RD #" + rdId + " completed all installments.");
                } else {
                    LocalDateTime nextDebit = testMode
                            ? currentNextDebit.toLocalDateTime().plusMinutes(5)
                            : currentNextDebit.toLocalDateTime().plusMonths(1);

                    try (PreparedStatement advancePs = conn.prepareStatement(advanceSql)) {
                        advancePs.setTimestamp(1, Timestamp.valueOf(nextDebit));
                        advancePs.setInt(2, rdId);
                        advancePs.executeUpdate();
                    }
                    System.out.println("RD #" + rdId + " installment " + newInstallmentsPaid + "/" + noOfMonths + " debited.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error processing RD installments: " + e.getMessage());
        }
    }

    public List<RecurringDeposit> getRDsByCid(String cid) {
        String sql = "SELECT * FROM recurring_deposit WHERE cid = ? ORDER BY book_date DESC";
        List<RecurringDeposit> rds = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cid);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RecurringDeposit rd = new RecurringDeposit();
                rd.setRdId(rs.getInt("rd_id"));
                rd.setCid(rs.getString("cid"));
                rd.setAccno(rs.getString("accno"));
                rd.setMonthlyAmount(rs.getBigDecimal("monthly_amount"));
                rd.setNoOfMonths(rs.getInt("no_of_months"));
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