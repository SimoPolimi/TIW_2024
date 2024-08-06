package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AlbumImageDAO {
	private Connection connection;

	public AlbumImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int saveAlbumImages(int albumId, List<Integer> imageIds) throws SQLException {
        String query = "INSERT INTO album_image (id_album, id_image) VALUES (?, ?)";
        PreparedStatement pstatement = null;
        int[] result = null;

        try {
            pstatement = connection.prepareStatement(query);
            for (int imageId : imageIds) {
                pstatement.setInt(1, albumId);
                pstatement.setInt(2, imageId);
                pstatement.addBatch();
            }
            /* Batch for multiple query in 1 access */
            result = pstatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                if (pstatement != null) {
                    pstatement.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
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
        System.out.print(rowsUpdated);
        return rowsUpdated;

    }
}
