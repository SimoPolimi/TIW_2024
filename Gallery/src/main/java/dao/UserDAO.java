package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkCredentials(String mail, String password) throws SQLException {
		String query = "SELECT  id, username FROM user  WHERE mail = ? AND password =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, mail);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					return user;
				}
			}
		}
	}
	
	public User registerUser(String username, String mail, String password) throws SQLException {
		String query = "INSERT INTO user (username, mail, password) VALUES (?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, mail);
			pstatement.setString(3, password);
			pstatement.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {}
		}
		// Set user to return
		User user = null;
		try {
			user = checkCredentials(mail, password);
		} catch (SQLException e) {}
		return user;
	}
	
	public boolean isNewUsername(String username) throws SQLException {
		String query = "SELECT username FROM user WHERE username = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return true;
				else {
					result.next();
					return false;
				}
			}
		}
	}
	
	public boolean isNewEmail(String mail) throws SQLException {
		String query = "SELECT mail FROM user WHERE mail = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, mail);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return true;
				else {
					result.next();
					return false;
				}
			}
		}
	}
}
