package dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Customer;
import utilities.ConnectionFactory;
import utilities.PasswordUtil;

public class CustomerDao {

	// Why this is a private helper, not called directly from the servlet:
	// CID generation is an internal implementation detail of "how registration
	// works" — the servlet shouldn't need to know HOW ids are made, just that
	// registerCustomer() handles it.
	private String generateNextCid(Connection conn) throws SQLException {
		String sql = "SELECT cid FROM customer ORDER BY cid DESC LIMIT 1";

		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				String lastCid = rs.getString("cid"); // e.g. "CUST0007"

				// Why substring(4): "CUST" is always the first 4 characters,
				// so everything after that is the numeric part we need to increment.
				int number = Integer.parseInt(lastCid.substring(4));
				number++;

				// Why String.format("%04d", ...): this pads the number with
				// leading zeros to always be 4 digits (7 -> "0007", 23 -> "0023"),
				// matching the CUST0001 format you asked for.
				return "CUST" + String.format("%04d", number);
			} else {
				// Why this branch: if the table is empty (no customers yet),
				// there's no "last" row to increment from, so we start fresh.
				return "CUST0001";
			}
		}
	}

	// Why the return type changed from boolean to String: the servlet needs
	// to know WHICH cid got generated so it can show it to the user
	// ("Your Customer ID is CUST0001") — a boolean can't carry that information.
	// Returns the generated cid on success, or null on failure.
	public String registerCustomer(Customer customer) {
		String insertSql = "INSERT INTO customer (cid, name, phone, email, panno, aadhaarno, password) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = ConnectionFactory.getConnection()) {

			// Why we generate the cid using the SAME connection, inside the
			// same try block: keeps both operations (read last cid, insert
			// new row) tied to one connection lifecycle, which matters if we
			// later add transaction handling (conn.setAutoCommit(false)) to
			// prevent two people registering at the exact same millisecond
			// from getting the same generated CID.
			String newCid = generateNextCid(conn);

			try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
				ps.setString(1, newCid);
				ps.setString(2, customer.getName());
				ps.setString(3, customer.getPhone());
				ps.setString(4, customer.getEmail());
				ps.setString(5, customer.getPanno());
				ps.setString(6, customer.getAadhaarno());
				ps.setString(7, PasswordUtil.hashPassword(customer.getPassword()));

				int rowsInserted = ps.executeUpdate();
				return rowsInserted > 0 ? newCid : null;
			}

		} catch (SQLException e) {
			System.out.println("Error registering customer: " + e.getMessage());
			return null;
		}
	}

	public Customer validateLogin(String identifier, String password) {
		String sql = "SELECT * FROM customer WHERE (cid = ? OR email = ?)";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, identifier);
			ps.setString(2, identifier);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String storedHash = rs.getString("password");

				// Why we check the password here, AFTER fetching the row,
				// rather than in the SQL: this is the actual verification step
				// now — BCrypt.checkpw() re-hashes the plain-text attempt using
				// the salt embedded in storedHash, then compares the results.
				if (PasswordUtil.checkPassword(password, storedHash)) {
					Customer customer = new Customer();
					customer.setCid(rs.getString("cid"));
					customer.setName(rs.getString("name"));
					customer.setPhone(rs.getString("phone"));
					customer.setEmail(rs.getString("email"));
					customer.setPanno(rs.getString("panno"));
					customer.setAadhaarno(rs.getString("aadhaarno"));
					customer.setPassword(storedHash);
					return customer;
				}
			}

		} catch (SQLException e) {
			System.out.println("Error validating login: " + e.getMessage());
		}

		return null;
	}

	public Customer getCustomerByCid(String cid) {
		String sql = "SELECT * FROM customer WHERE cid = ?";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cid);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				Customer customer = new Customer();
				customer.setCid(rs.getString("cid"));
				customer.setName(rs.getString("name"));
				customer.setPhone(rs.getString("phone"));
				customer.setEmail(rs.getString("email"));
				customer.setPanno(rs.getString("panno"));
				customer.setAadhaarno(rs.getString("aadhaarno"));
				customer.setPassword(rs.getString("password"));
				return customer;
			}

		} catch (SQLException e) {
			System.out.println("Error fetching customer: " + e.getMessage());
		}

		return null;
	}

//	 Why this only updates phone/email plus password (matches your CustomerUpd.jsp
//	 wireframe):
//	 name, PAN, and Aadhaar are identity/KYC documents — real banks don't let
//	 these be casually edited without a formal re-verification process, so we
//	 deliberately don't expose them here.
//	public boolean updateCustomer(String cid, String phone, String email, String password) {
//		String sql = "UPDATE customer SET phone = ?, email = ?, password = ? WHERE cid = ?";
//
//		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
//
//			ps.setString(1, phone);
//			ps.setString(2, email);
//			ps.setString(3, password);
//			ps.setString(4, cid);
//
//			int rowsUpdated = ps.executeUpdate();
//			return rowsUpdated > 0;
//
//		} catch (SQLException e) {
//			System.out.println("Error updating customer: " + e.getMessage());
//			return false;
//		}
//	}
	// Why this method now needs to know whether "password" is already hashed:
	// ProfileServlet/ManageUsersServlet sometimes pass a BRAND NEW plain-text
	// password (user typed one in), and sometimes pass back the EXISTING
	// stored hash unchanged (user left the password field blank). Hashing an
	// already-hashed value AGAIN would corrupt it — the customer's real
	// password would then no longer match on their next login. We solve this
	// by hashing at the SOURCE (in the servlets, right before calling this
	// method) rather than inside this method — see ProfileServlet/
	// ManageUsersServlet changes below. This method now assumes whatever
	// "password" it receives is ALREADY in its final, correct form (hash or
	// unchanged hash) and stores it as-is.
	public boolean updateCustomer(String cid, String phone, String email, String password) {
		String sql = "UPDATE customer SET phone = ?, email = ?, password = ? WHERE cid = ?";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, phone);
			ps.setString(2, email);
			ps.setString(3, password);
			ps.setString(4, cid);

			int rowsUpdated = ps.executeUpdate();
			return rowsUpdated > 0;

		} catch (SQLException e) {
			System.out.println("Error updating customer: " + e.getMessage());
			return false;
		}
	}

	// Why this checks for existing accounts BEFORE attempting delete: without
	// this, the database's FOREIGN KEY constraint (account.cid references
	// customer.cid) would reject the delete anyway — but with a cryptic SQL
	// error instead of a clear message an admin can actually understand and act on.
	public boolean hasActiveAccounts(String cid) {
		String sql = "SELECT COUNT(*) AS total FROM account WHERE cid = ?";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cid);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getInt("total") > 0;
			}

		} catch (SQLException e) {
			System.out.println("Error checking accounts: " + e.getMessage());
		}

		return false;
	}

	public boolean deleteCustomer(String cid) {
		String sql = "DELETE FROM customer WHERE cid = ?";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cid);
			int rowsDeleted = ps.executeUpdate();
			return rowsDeleted > 0;

		} catch (SQLException e) {
			System.out.println("Error deleting customer: " + e.getMessage());
			return false;
		}
	}
}