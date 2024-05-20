package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Album;

public class AlbumDAO {
	private Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Album> showUserAlbums(int owner) throws SQLException {
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT  * FROM album WHERE owner = ?";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, owner);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Album album = new Album();
						album.setTitle(result.getString("title"));
						albums.add(album);
					}
				}
			}
		}
		return albums;
	}
	
	public List<Album> showOtherUserAlbums(int user) throws SQLException {
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT  * FROM album WHERE owner <> ?";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, user);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Album album = new Album();
						album.setId(result.getInt("id"));
						album.setTitle(result.getString("title"));
						albums.add(album);
					}
				}
			}
		}
		return albums;
	}
}
