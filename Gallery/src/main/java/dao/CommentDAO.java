package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.CommentWithUser;

public class CommentDAO {
	private Connection connection;

	public CommentDAO(Connection connection) {
		this.connection = connection;
	}

	public List<CommentWithUser> showImageComments(int imageId) throws SQLException {
		List<CommentWithUser> comments = new ArrayList<CommentWithUser>();
		String query = "SELECT comment.*, user.username FROM comment INNER JOIN user ON comment.id_user = user.id  WHERE id_image = ? ORDER BY date";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						CommentWithUser comment = new CommentWithUser();
						comment.setId(result.getInt("id"));
						comment.setId_image(result.getInt("id_image"));
						comment.setUsername(result.getString("username"));
						comment.setDate(result.getDate("date"));
						comment.setText(result.getString("text"));
						comments.add(comment);
					}
				}
			}
		}
		return comments;
	}
	
	
	// int????????????????????????????
	public int writeComment(int imageId, int userId, Date date, String text) throws SQLException {
		String query = "INSERT INTO comment (id_image, id_user, date, text) VALUES (?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		int code = 0;		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, imageId);
			pstatement.setInt(2, userId);
			pstatement.setDate(3, date);
			pstatement.setString(4, text);
			code = pstatement.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {}
		}
		return code;
	}

}
