package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Image;

public class ImageDAO {
	private Connection connection;

	public ImageDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Image> showAlbumImages(int albumId, int limit, int offset) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT image.*, user.username FROM image "
				+ "INNER JOIN album_image on image.id=album_image.id_image "
				+ "INNER JOIN album on album_image.id_album=album.id "
				+ "INNER JOIN user on image.id_user=user.id "
				+ "WHERE album.id=? ORDER BY creation_date DESC LIMIT ? OFFSET ?;";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, limit);
			pstatement.setInt(3, offset);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Image image = new Image();
						image.setId(result.getInt("id"));
						image.setUsername(result.getString("username"));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getDate("creation_date"));
						image.setDescription(result.getString("description"));
						image.setPath(result.getString("path"));
						images.add(image);
					}
				}
			}
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
        }
    }

	public Image showImage(int imageId) throws SQLException {
		Image image = new Image();
		String query = "SELECT image.*, user.username FROM image "
				+ "INNER JOIN user on image.id_user=user.id "
				+ "WHERE image.id=?;";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					image.setId(result.getInt("id"));
					image.setUsername(result.getString("username"));
					image.setTitle(result.getString("title"));
					image.setCreation_date(result.getDate("creation_date"));
					image.setDescription(result.getString("description"));
					image.setPath(result.getString("path"));
					return image;
				}
			}
		}	
	}
	
	public List<Image> showMyImages(int userId) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT * FROM image where id_user = ?;";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Image image = new Image();
						image.setId(result.getInt("id"));
						image.setTitle(result.getString("title"));
						image.setCreation_date(result.getDate("creation_date"));
						image.setDescription(result.getString("description"));
						image.setPath(result.getString("path"));
						images.add(image);
					}
				}
			}
		}
		return images;
	}

	public void deleteImage(int imageId) throws SQLException {
        String query = "DELETE FROM image WHERE id = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, imageId);
            pstatement.executeUpdate();
        }
    }
}