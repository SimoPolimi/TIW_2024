package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserImageOrderDAO {
	private Connection connection;

	public UserImageOrderDAO(Connection connection) {
		this.connection = connection;
	}

	public boolean isExistingOrder(int userId, int albumId) throws SQLException {
		String query = "SELECT * FROM user_image_order WHERE id_user = ? AND id_album = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, userId);
			pstatement.setInt(2, albumId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return false;
				else {
					result.next();
					return true;
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}

	public int updateImageOrder(int userId, int albumId, List<Integer> imageIds, List<Integer> positions)
			throws SQLException {
		String query = "UPDATE user_image_order SET position = ? WHERE id_user = ? AND id_image = ? AND id_album = ?";
		int totalRowsUpdated = 0;

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			for (int i = 0; i < imageIds.size(); i++) {
				pstatement.setInt(1, positions.get(i));
				pstatement.setInt(2, userId);
				pstatement.setInt(3, imageIds.get(i));
				pstatement.setInt(4, albumId);
				pstatement.addBatch();
			}

			int[] results = pstatement.executeBatch();

			// Numbers of rows updated
			for (int result : results) {
				if (result != PreparedStatement.EXECUTE_FAILED) {
					totalRowsUpdated += result;
				}
			}

		} catch (SQLException e) {
			throw new SQLException(e);
		}

		return totalRowsUpdated;
	}

	public int insertImageOrder(int userId, int albumId, List<Integer> imageIds, List<Integer> positions)
			throws SQLException {
		String query = "INSERT INTO user_image_order (id_user, id_image, id_album, position) VALUES (?, ?, ?, ?)";
		int totalRowsInserted = 0;

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			for (int i = 0; i < imageIds.size(); i++) {
				pstatement.setInt(1, userId);
				pstatement.setInt(2, imageIds.get(i));
				pstatement.setInt(3, albumId);
				pstatement.setInt(4, positions.get(i));
				pstatement.addBatch();
			}

			int[] results = pstatement.executeBatch();

			// Numbers of rows updated
			for (int result : results) {
				if (result != PreparedStatement.EXECUTE_FAILED) {
					totalRowsInserted += result;
				}
			}

		} catch (SQLException e) {
			throw new SQLException(e);
		}

		return totalRowsInserted;
	}

}
