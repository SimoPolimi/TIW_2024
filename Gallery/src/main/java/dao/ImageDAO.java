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

	public List<Image> showAlbumImages(int albumId) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT image.* FROM image "
				+ "INNER JOIN album_image on image.id=album_image.id_image "
				+ "INNER JOIN album on album_image.id_album=album.id "
				+ "WHERE album.id=?;";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, albumId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Image image = new Image();
						image.setId(result.getInt("id"));
						image.setId_user(result.getInt("id_user"));
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
	
}