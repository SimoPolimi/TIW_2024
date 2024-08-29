package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.ImageWithComments;

public class ImageDAO {
	private Connection connection;

	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public ImageWithComments getImageById(int imageId) throws SQLException{
		String query = "SELECT  * FROM image WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					ImageWithComments image = new ImageWithComments();
					UserDAO userDAO = new UserDAO(connection);
					image.setId(result.getInt("id"));
					image.setUser(userDAO.getUserById(result.getInt("id_user")));
					image.setTitle(result.getString("title"));
					image.setCreation_date(result.getDate("creation_date"));
					image.setDescription(result.getString("description"));
					image.setPath(result.getString("path"));
					return image;
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}

	public List<ImageWithComments> getAlbumImages(int albumId) throws SQLException {
		List<ImageWithComments> images = new ArrayList<ImageWithComments>();
		String query = "SELECT image.* FROM image "
				+ "INNER JOIN album_image on image.id=album_image.id_image "
				+ "INNER JOIN album on album_image.id_album=album.id "
				+ "WHERE album.id=? ORDER BY creation_date DESC;";

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						ImageWithComments image = new ImageWithComments();
						UserDAO userDAO = new UserDAO(connection);
						image.setId(result.getInt("id"));
						image.setUser(userDAO.getUserById(result.getInt("id_user")));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getDate("creation_date"));
						image.setDescription(result.getString("description"));
						image.setPath(result.getString("path"));
						images.add(image);
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return images;
	}
	
	public List<ImageWithComments> getAlbumImagesOrdered(int albumId, int userId) throws SQLException {
		List<ImageWithComments> images = new ArrayList<ImageWithComments>();
		String query = "SELECT image.* "
				+ "FROM image "
				+ "INNER JOIN album_image ON image.id = album_image.id_image "
				+ "INNER JOIN album ON album_image.id_album = album.id "
				+ "INNER JOIN user_image_order ON image.id = user_image_order.id_image "
				+ "    AND album_image.id_album = user_image_order.id_album "
				+ "WHERE album.id = ? "
				+ "		  AND user_image_order.id_user = ? "
				+ "ORDER BY user_image_order.position;";
		
		System.out.println(query);

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						ImageWithComments image = new ImageWithComments();
						UserDAO userDAO = new UserDAO(connection);
						image.setId(result.getInt("id"));
						image.setUser(userDAO.getUserById(result.getInt("id_user")));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getDate("creation_date"));
						image.setDescription(result.getString("description"));
						image.setPath(result.getString("path"));
						images.add(image);
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return images;
	}
	
	public int countImagesByAlbumId(int albumId) throws SQLException {
        String query = "SELECT COUNT(*) FROM album_image WHERE id_album = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
        	pstatement.setInt(1, albumId);
            ResultSet result = pstatement.executeQuery();
            if (!result.isBeforeFirst()) // no results
				return 0;
			else {
				result.next();
				return result.getInt(1);
			}
        } catch (SQLException e) {
			throw new SQLException(e);
		}
    }
	
	public List<ImageWithComments> getUserImages(int userId) throws SQLException {
		List<ImageWithComments> images = new ArrayList<ImageWithComments>();
		String query = "SELECT * FROM image where id_user = ?;";

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						ImageWithComments image = new ImageWithComments();
						image.setId(result.getInt("id"));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getDate("creation_date"));
						image.setDescription(result.getString("description"));
						image.setPath(result.getString("path"));
						images.add(image);
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return images;
	}

	public void deleteImage(int imageId) throws SQLException {
        String query = "DELETE FROM image WHERE id = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, imageId);
            pstatement.executeUpdate();
        } catch (SQLException e) {
			throw new SQLException(e);
		}
        return;
    }
	
	public void uploadImage(int userId, String title, Date date, String description, String path) throws SQLException {
        String query = "INSERT INTO image (id_user, title, creation_date, description, path) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, userId);
            pstatement.setString(2, title);
            pstatement.setDate(3, date);
            pstatement.setString(4, description);
            pstatement.setString(5, path);
            pstatement.executeUpdate();
        } catch (SQLException e) {
			throw new SQLException(e);
		}
        return;
    }
}