package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    // Why these are constants: the URL/username/password never change at runtime,
    // so we declare them once as static finals instead of recomputing them every call.
    private static final String URL = "jdbc:mysql://localhost:3306/ebanking_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    // Why static: we don't want DAOs to have to create a "new ConnectionFactory()"
    // object just to get a connection — this method belongs to the class itself.
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Why this line: it tells Java which JDBC driver to use (the MySQL
            // connector jar you placed in WEB-INF/lib). Some newer driver versions
            // auto-register without this line, but including it avoids version-specific bugs.
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Why we catch this separately: it specifically means the MySQL jar
            // isn't properly on the classpath (e.g., not in WEB-INF/lib correctly).
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            // Why we catch this separately: it means the driver was found, but the
            // connection itself failed (wrong password, MySQL service not running,
            // wrong database name, etc.) — a different problem needing a different fix.
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return connection;
    }
}