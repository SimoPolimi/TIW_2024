package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import beans.Image;

public class ImageDAO {
	private Connection connection;

	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Image getImageById(int imageId) throws SQLException{
		String query = "SELECT  * FROM image WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					Image image = new Image();
					UserDAO userDAO = new UserDAO(connection);
					image.setId(result.getInt("id"));
					image.setUser(userDAO.getUserById(result.getInt("id_user")));
					image.setTitle(result.getString("title"));
					image.setCreation_date(result.getTimestamp("creation_date"));
					image.setDescription(result.getString("description"));
					image.setPath(result.getString("path"));
					return image;
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}

	public List<Image> getAlbumImages(int albumId, int limit, int offset) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT image.* FROM image "
				+ "INNER JOIN album_image on image.id=album_image.id_image "
				+ "INNER JOIN album on album_image.id_album=album.id "
				+ "WHERE album.id=? ORDER BY creation_date DESC LIMIT ? OFFSET ?;";

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, limit);
			pstatement.setInt(3, offset);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Image image = new Image();
						UserDAO userDAO = new UserDAO(connection);
						image.setId(result.getInt("id"));
						image.setUser(userDAO.getUserById(result.getInt("id_user")));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getTimestamp("creation_date"));
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
	
	public List<Image> getUserImages(int userId) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT * FROM image where id_user = ? ORDER BY creation_date DESC;";

		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Image image = new Image();
						image.setId(result.getInt("id"));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getTimestamp("creation_date"));
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
	
	public boolean isMyImage(int imageId, int userId) throws SQLException {
		String query = "SELECT * FROM image WHERE id = ? AND id_user = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			pstatement.setInt(2, userId);
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
	
	public boolean areImagesOwnedByUser(List<Integer> imageIds, int userId) throws SQLException {
	    if (imageIds == null || imageIds.isEmpty()) {
	        return true;
	    }
	    
	    // Check id IN (?,?,?)    ? equals to imageIds.size()
	    String query = "SELECT COUNT(*) FROM image WHERE id IN (" +
	                   String.join(",", Collections.nCopies(imageIds.size(), "?")) +
	                   ") AND id_user = ?";
	    
	    try (PreparedStatement pstatement = connection.prepareStatement(query)) {
	        int index = 1;
	        for (Integer imageId : imageIds) {
	            pstatement.setInt(index++, imageId);
	        }
	        pstatement.setInt(index, userId);
	        
	        try (ResultSet result = pstatement.executeQuery()) {
	            if (result.next()) {
	                int count = result.getInt(1);
	                return count == imageIds.size();
	            } else {
	                return false;
	            }
	        }
	    } catch (SQLException e) {
	        throw new SQLException(e);
	    }
	}


	
	public void uploadImage(int userId, String title, Timestamp date, String description, String path) throws SQLException {
        String query = "INSERT INTO image (id_user, title, creation_date, description, path) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, userId);
            pstatement.setString(2, title);
            pstatement.setTimestamp(3, date);
            pstatement.setString(4, description);
            pstatement.setString(5, path);
            pstatement.executeUpdate();
        } catch (SQLException e) {
			throw new SQLException(e);
		}
        return;
    }
	
	public boolean isNewImage(String path) throws SQLException {
		String query = "SELECT path FROM image WHERE path = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, path);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return true;
				else {
					result.next();
					return false;
				}
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}
}