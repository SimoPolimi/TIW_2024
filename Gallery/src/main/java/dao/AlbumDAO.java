package dao;

import java.sql.Connection;
import java.sql.Date;
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
		String query = "SELECT  * FROM album WHERE owner = ? ORDER BY creation_date DESC";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, owner);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
						Album album = new Album();
						album.setId(result.getInt("id"));
						album.setTitle(result.getString("title"));
						album.setOwner(result.getString("owner"));
						album.setDate(result.getDate("creation_date"));
						albums.add(album);
					}
				}
			}
		}
		return albums;
	}
	
	public List<Album> showOtherUserAlbums(int user) throws SQLException {
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT  * FROM album WHERE owner <> ? ORDER BY creation_date DESC";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, user);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					while (result.next()) {
						Album album = new Album();
						album.setId(result.getInt("id"));
						album.setTitle(result.getString("title"));
						album.setOwner(result.getString("owner"));
						album.setDate(result.getDate("creation_date"));
						albums.add(album);
					}
				}
			}
		}
		return albums;
	}
	
	public int createAlbum(String title, int owner, Date creation_date) throws SQLException {
		 String query = "INSERT INTO album (title, owner, creation_date) VALUES (?, ?, ?)";
		 
		 try (PreparedStatement pstatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
		     pstatement.setString(1, title);
		     pstatement.setInt(2, owner);
		     pstatement.setDate(3, creation_date);
		        
		     int rowsUpdated = pstatement.executeUpdate();
		        
		     if (rowsUpdated > 0) {
		         try (ResultSet result = pstatement.getGeneratedKeys()) {
		             if (result.next()) {
		                 return result.getInt(1);
		             }
		         }
		    }
		    return 0;
		}
	}
}
