package dao;

import java.math.BigDecimal;
import java.sql.Connection;
//import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
//import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;

import model.FixedDeposit;
import utilities.ConnectionFactory;

public class FixedDepositDao {

	// Why this queries by cid, not accno: a customer might have FDs across
	// multiple accounts (since one customer can hold multiple accounts, as you
	// pointed out earlier) — querying by cid shows ALL of them in one list,
	// which is what "My FDs" should mean from the customer's point of view.
	public List<FixedDeposit> getFDsByCid(String cid) {
	    String sql = "SELECT * FROM fixed_deposit WHERE cid = ? ORDER BY book_date DESC";
	    List<FixedDeposit> fds = new ArrayList<>();

	    try (Connection conn = ConnectionFactory.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, cid);
	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	            FixedDeposit fd = new FixedDeposit();
	            fd.setFdId(rs.getInt("fd_id"));
	            fd.setCid(rs.getString("cid"));
	            fd.setAccno(rs.getString("accno"));
	            fd.setAmount(rs.getBigDecimal("amount"));
	            fd.setNoOfYears(rs.getInt("no_of_years"));
	            fd.setInterestRate(rs.getBigDecimal("interest_rate"));
	            fd.setBookDate(rs.getTimestamp("book_date"));
	            fd.setMaturityDate(rs.getTimestamp("maturity_date"));
	            fd.setStatus(rs.getString("status"));
	            fds.add(fd);
	        }

	    } catch (SQLException e) {
	        System.out.println("Error fetching FDs: " + e.getMessage());
	    }

	    return fds;
	}
	// Why this whole operation is wrapped in one transaction (commit/rollback),
    // same as deposit/withdraw/transfer: booking an FD does TWO things —
    // deducts from the savings account AND creates the FD record. Both must
    // succeed together, or you'd end up with money missing but no FD to show for it.
    public boolean bookFD(FixedDeposit fd, boolean testMode) {
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
            // LocalDate bookLocalDate = fd.getBookDate().toLocalDate();
            LocalDateTime bookDateTime = fd.getBookDate().toLocalDateTime();

            // Why the branch here: in real mode, "noOfYears" means literal
            // years (matches your wireframe and real banking). In test mode,
            // we reinterpret the SAME number as minutes instead, so a "3"
            // typed into the form matures in 3 minutes instead of 3 years —
            // letting you watch the automatic crediting happen live without
            // touching the interest calculation logic at all.
            // LocalDate maturityLocalDate = bookLocalDate.plusYears(fd.getNoOfYears());
            LocalDateTime maturityDateTime = testMode
                    ? bookDateTime.plusMinutes(fd.getNoOfYears() * 5L)
                    : bookDateTime.plusYears(fd.getNoOfYears());
            // Date maturityDate = Date.valueOf(maturityLocalDate);
            Timestamp maturityTimestamp = Timestamp.valueOf(maturityDateTime);

            try (PreparedStatement ps = conn.prepareStatement(insertFdSql)) {
                ps.setString(1, fd.getCid());
                ps.setString(2, fd.getAccno());
                ps.setBigDecimal(3, fd.getAmount());
                ps.setInt(4, fd.getNoOfYears());
                ps.setBigDecimal(5, fd.getInterestRate());
//                ps.setDate(6, fd.getBookDate());
//                ps.setDate(7, maturityDate);
                ps.setTimestamp(6, fd.getBookDate());
                ps.setTimestamp(7, maturityTimestamp);
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
    // Why this is a completely separate method, called on a timer (see the
    // listener below) rather than checked only when a customer happens to
    // visit a page: maturity needs to happen even if NOBODY is actively
    // using the app at that moment — a real bank doesn't wait for you to
    // log in before crediting matured interest.
    public void processMaturedDeposits() {
        // Why "SELECT ... WHERE status = 'ACTIVE' AND maturity_date <= NOW()":
        // this finds every FD that has reached or passed its maturity
        // moment but hasn't been processed yet — NOW() is MySQL's current
        // server timestamp, compared directly against the DATETIME column.
        String findMaturedSql = "SELECT * FROM fixed_deposit WHERE status = 'ACTIVE' AND maturity_date <= NOW()";
        String creditSql = "UPDATE account SET balance = balance + ? WHERE accno = ?";
        String markMaturedSql = "UPDATE fixed_deposit SET status = 'MATURED' WHERE fd_id = ?";
        String logTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
                                  + "VALUES (?, NULL, ?, 'FD_MATURED')";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement findPs = conn.prepareStatement(findMaturedSql);
             ResultSet rs = findPs.executeQuery()) {

            while (rs.next()) {
                int fdId = rs.getInt("fd_id");
                String accno = rs.getString("accno");
                BigDecimal principal = rs.getBigDecimal("amount");
                int years = rs.getInt("no_of_years");
                BigDecimal rate = rs.getBigDecimal("interest_rate");

                // Why we calculate interest using the STATED "no_of_years"
                // value even in test mode (where the actual wait was only
                // minutes): test mode exists to demo the AUTOMATIC TRIGGER
                // working, not to test real interest accrual over real time.
                // Using the stated years keeps the interest math meaningful
                // and consistent, whether the FD matured in 3 real years or
                // 3 test-minutes.
                //
                // Simple interest formula: interest = principal * rate% * years
                BigDecimal interest = principal
                        .multiply(rate)
                        .multiply(BigDecimal.valueOf(years))
                        .divide(BigDecimal.valueOf(100));

                BigDecimal maturityAmount = principal.add(interest);

                try (PreparedStatement creditPs = conn.prepareStatement(creditSql)) {
                    creditPs.setBigDecimal(1, maturityAmount);
                    creditPs.setString(2, accno);
                    creditPs.executeUpdate();
                }

                try (PreparedStatement markPs = conn.prepareStatement(markMaturedSql)) {
                    markPs.setInt(1, fdId);
                    markPs.executeUpdate();
                }

                try (PreparedStatement logPs = conn.prepareStatement(logTransactionSql)) {
                    logPs.setString(1, accno);
                    logPs.setBigDecimal(2, maturityAmount);
                    logPs.executeUpdate();
                }

                System.out.println("FD #" + fdId + " matured. Credited " + maturityAmount + " to " + accno);
            }

        } catch (SQLException e) {
            System.out.println("Error processing matured FDs: " + e.getMessage());
        }
    }
    

 // Why 1.00 as a flat penalty percentage: this is a common, simple real-world
 // convention (reduce the rate by a fixed number of percentage points). Real
 // banks sometimes use more complex tiered penalties, but a flat penalty is
 // clear, easy to explain in a project writeup, and easy to adjust later.
 private static final BigDecimal PENALTY_RATE = new BigDecimal("1.00");

 // Why this returns BigDecimal (the credited amount) instead of boolean:
 // the servlet needs to SHOW the customer exactly how much they received
 // and how the penalty affected it — a plain true/false can't communicate that.
 // Returns null on failure (FD not found, not active, or ownership mismatch
 // caught earlier in the servlet).
 public BigDecimal prematureWithdrawFD(int fdId, String accno) {
     String getFdSql = "SELECT * FROM fixed_deposit WHERE fd_id = ? AND status = 'ACTIVE' FOR UPDATE";
     String creditSql = "UPDATE account SET balance = balance + ? WHERE accno = ?";
     String closeFdSql = "UPDATE fixed_deposit SET status = 'CLOSED_PREMATURE' WHERE fd_id = ?";
     String logTransactionSql = "INSERT INTO transaction (saccno, benaccno, amount, type) "
                               + "VALUES (?, NULL, ?, 'FD_PREMATURE_WITHDRAWAL')";

     Connection conn = null;
     try {
         conn = ConnectionFactory.getConnection();
         conn.setAutoCommit(false);

         BigDecimal principal;
         BigDecimal originalRate;
         Timestamp bookDate;

         try (PreparedStatement ps = conn.prepareStatement(getFdSql)) {
             ps.setInt(1, fdId);
             ResultSet rs = ps.executeQuery();

             // Why we check "!rs.next()" and bail out here: this covers BOTH
             // "FD doesn't exist" AND "FD already matured/closed" in one
             // check, since the SQL's WHERE clause already filters to
             // status = 'ACTIVE' — if nothing comes back, there's nothing
             // eligible to prematurely withdraw.
             if (!rs.next()) {
                 conn.rollback();
                 return null;
             }

             principal = rs.getBigDecimal("amount");
             originalRate = rs.getBigDecimal("interest_rate");
             bookDate = rs.getTimestamp("book_date");

             // Why we re-check accno here even though the servlet already
             // checked customer ownership: this is defense in depth — this
             // DAO method should be safe to call correctly on its own, not
             // rely ENTIRELY on the caller remembering to check first.
             String actualAccno = rs.getString("accno");
             if (!actualAccno.equals(accno)) {
                 conn.rollback();
                 return null;
             }
         }

         // Why ChronoUnit.DAYS.between(...) divided by 365.0, not .getYear()
         // subtraction: this gives a precise FRACTIONAL year (e.g. 0.667 for
         // 8 months), which is exactly what prorating interest requires —
         // whole-year subtraction would wrongly round 8 months down to 0.
         long daysHeld = ChronoUnit.DAYS.between(bookDate.toLocalDateTime(), LocalDateTime.now());
         BigDecimal yearsHeld = BigDecimal.valueOf(daysHeld).divide(BigDecimal.valueOf(365.0), 10, java.math.RoundingMode.HALF_UP);

         // Why max(0, rate - penalty): if someone's original rate was already
         // very low (e.g. 0.5%), subtracting a flat 1% penalty could go
         // negative — which would mean CHARGING them interest, nonsensical
         // for a withdrawal. Flooring at zero means worst case, they simply
         // earn no interest at all, never a negative return.
         BigDecimal effectiveRate = originalRate.subtract(PENALTY_RATE).max(BigDecimal.ZERO);

         // Simple interest formula, same shape as processMaturedDeposits():
         // interest = principal * effectiveRate% * yearsHeld
         BigDecimal interest = principal
                 .multiply(effectiveRate)
                 .multiply(yearsHeld)
                 .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

         BigDecimal payoutAmount = principal.add(interest);

         try (PreparedStatement ps = conn.prepareStatement(creditSql)) {
             ps.setBigDecimal(1, payoutAmount);
             ps.setString(2, accno);
             ps.executeUpdate();
         }

         try (PreparedStatement ps = conn.prepareStatement(closeFdSql)) {
             ps.setInt(1, fdId);
             ps.executeUpdate();
         }

         try (PreparedStatement ps = conn.prepareStatement(logTransactionSql)) {
             ps.setString(1, accno);
             ps.setBigDecimal(2, payoutAmount);
             ps.executeUpdate();
         }

         conn.commit();
         return payoutAmount;

     } catch (SQLException e) {
         System.out.println("Error processing premature withdrawal: " + e.getMessage());
         if (conn != null) {
             try {
                 conn.rollback();
             } catch (SQLException ex) {
                 System.out.println("Rollback failed: " + ex.getMessage());
             }
         }
         return null;

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