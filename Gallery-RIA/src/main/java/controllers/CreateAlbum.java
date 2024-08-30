package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.User;
import dao.AlbumDAO;
import dao.AlbumImageDAO;
import utils.ConnectionHandler;

@WebServlet("/CreateAlbum")
@MultipartConfig
public class CreateAlbum extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateAlbum() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AlbumDAO albumDAO = new AlbumDAO(connection);
		AlbumImageDAO albumImageDAO = new AlbumImageDAO(connection);
		String title = request.getParameter("title").trim();
		
		// Check parameter is present
		if (title == null || title.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing title.");
            return;
		}
		
		LocalDateTime date = LocalDateTime.now();
		Timestamp sqlDate = Timestamp.valueOf(date);
		int userId = ((User)request.getSession().getAttribute("user")).getId();
		int albumId = 0;
		
		try {
			albumId = albumDAO.createAlbum(title, userId, sqlDate);
			String[] imageIds = request.getParameterValues("selectedImages");
            if (imageIds != null) {
                List<Integer> imageIdList = new ArrayList<>();
                for (String id : imageIds) {
                    imageIdList.add(Integer.parseInt(id));
                }
                albumImageDAO.saveAlbumImages(albumId, imageIdList);
            }
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to create album.");
            return;
		}
		
	}

}
