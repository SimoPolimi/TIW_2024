package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumImageDAO {
	private Connection connection;

	public AlbumImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int saveAlbumImages(int albumId, List<Integer> imageIds) throws SQLException {
        String query = "INSERT INTO album_image (id_album, id_image) VALUES (?, ?)";
        int[] result = null;

        try (PreparedStatement pstatement = connection.prepareStatement(query)){
            for (int imageId : imageIds) {
                pstatement.setInt(1, albumId);
                pstatement.setInt(2, imageId);
                pstatement.addBatch();
            }
            /* Batch for multiple query in 1 access */
            result = pstatement.executeBatch();
        } catch (SQLException e) {
            throw new SQLException(e);
        }

        // Checks how many rows updated
        int rowsUpdated = 0;
        if (result != null) {
            for (int r : result) {
                if (r != PreparedStatement.EXECUTE_FAILED) {
                	rowsUpdated += r;
                }
            }
        }
        //System.out.print(rowsUpdated);
        return rowsUpdated;
    }
	
	// For order controls (check if all the images are in the album and each is ordered)
	public List<Integer> getImageIdsByAlbum(int albumId) throws SQLException {
        String query = "SELECT id_image FROM album_image WHERE id_album = ?";
        List<Integer> imageIds = new ArrayList<>();

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, albumId);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    imageIds.add(result.getInt("id_image"));
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }

        return imageIds;
    }
}
