package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import model.FixedDeposit;
import utilities.ConnectionFactory;

public class FixedDepositDao {

    // Why this whole operation is wrapped in one transaction (commit/rollback),
    // same as deposit/withdraw/transfer: booking an FD does TWO things —
    // deducts from the savings account AND creates the FD record. Both must
    // succeed together, or you'd end up with money missing but no FD to show for it.
    public boolean bookFD(FixedDeposit fd) {
        String getBalanceSql = "SELECT balance FROM account WHERE accno = ? FOR UPDATE";
        String debitSql = "UPDATE account SET balance = balance - ? WHERE accno = ?";
        String insertFdSql = "INSERT INTO fixed_deposit "
                + "(cid, accno, amount, no_of_years, interest_rate, book_date, maturity_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        String logTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
                                  + "VALUES (?, NULL, ?, 'FD_BOOKED')";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            BigDecimal currentBalance;
            try (PreparedStatement ps = conn.prepareStatement(getBalanceSql)) {
                ps.setString(1, fd.getAccno());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                currentBalance = rs.getBigDecimal("balance");
            }

            // Why this is a hard block (not a warning like the transfer's
            // minimum balance): unlike a transfer where 5000 is a policy
            // preference, here we simply cannot deduct more money than
            // exists — this is the same "impossible, not just discouraged"
            // situation as withdraw's balance check.
            if (currentBalance.compareTo(fd.getAmount()) < 0) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(debitSql)) {
                ps.setBigDecimal(1, fd.getAmount());
                ps.setString(2, fd.getAccno());
                ps.executeUpdate();
            }

            // Why we calculate maturity_date here in Java (using
            // LocalDate.plusYears) rather than in the SQL itself: Java's date
            // arithmetic is more readable and testable than MySQL's DATE_ADD
            // syntax, and this calculation is pure business logic that
            // belongs with the rest of the FD booking rules, not buried in a
            // SQL string.
            LocalDate bookLocalDate = fd.getBookDate().toLocalDate();
            LocalDate maturityLocalDate = bookLocalDate.plusYears(fd.getNoOfYears());
            Date maturityDate = Date.valueOf(maturityLocalDate);

            try (PreparedStatement ps = conn.prepareStatement(insertFdSql)) {
                ps.setString(1, fd.getCid());
                ps.setString(2, fd.getAccno());
                ps.setBigDecimal(3, fd.getAmount());
                ps.setInt(4, fd.getNoOfYears());
                ps.setBigDecimal(5, fd.getInterestRate());
                ps.setDate(6, fd.getBookDate());
                ps.setDate(7, maturityDate);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(logTransactionSql)) {
                ps.setString(1, fd.getAccno());
                ps.setBigDecimal(2, fd.getAmount());
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Error booking FD: " + e.getMessage());
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
}