package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Comment;

public class CommentDAO {
	private Connection connection;

	public CommentDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Comment> showImageComments(int imageId) throws SQLException {
		List<Comment> comments = new ArrayList<Comment>();
		String query = "SELECT comment.* FROM comment WHERE id_image = ? ORDER BY date";

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Comment comment = new Comment();
						ImageDAO imageDAO = new ImageDAO(connection);
						UserDAO userDAO = new UserDAO(connection);
						comment.setId(result.getInt("id"));
						comment.setImage(imageDAO.getImageById(result.getInt("id_image")));
						comment.setUser(userDAO.getUserById(result.getInt("id_user")));
						comment.setDate(result.getDate("date"));
						comment.setText(result.getString("text"));
						comments.add(comment);
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}		
		return comments;
	}
	
	
	// int????????????????????????????
	public int writeComment(int imageId, int userId, Date date, String text) throws SQLException {
		String query = "INSERT INTO comment (id_image, id_user, date, text) VALUES (?, ?, ?, ?)";
		int code = 0;		
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			pstatement.setInt(2, userId);
			pstatement.setDate(3, date);
			pstatement.setString(4, text);
			code = pstatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return code;
	}

}
