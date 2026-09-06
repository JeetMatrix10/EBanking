package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Admin;
import utilities.PasswordUtil;
import utilities.ConnectionFactory;

public class AdminDao {

	public Admin validateAdminLogin(String username, String password) {
		String sql = "SELECT * FROM admin WHERE username = ?";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String storedHash = rs.getString("password");

				if (PasswordUtil.checkPassword(password, storedHash)) {
					Admin admin = new Admin();
					admin.setAdminId(rs.getInt("admin_id"));
					admin.setUsername(rs.getString("username"));
					admin.setPassword(storedHash);
					return admin;
				}
			}

		} catch (SQLException e) {
			System.out.println("Error validating admin login: " + e.getMessage());
		}

		return null;
	}
}