package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.User;
import dao.AlbumDAO;
import dao.AlbumImageDAO;
import dao.ImageDAO;
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
        ImageDAO imageDAO = new ImageDAO(connection);
        String title = request.getParameter("title").trim();
        String[] imageIds = request.getParameterValues("selectedImages");

        int userId = ((User) request.getSession().getAttribute("user")).getId();

        // Check parameter is present
        if (title == null || title.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing title");
            return;
        }

        List<Integer> imageIdList = new ArrayList<>();

        if (imageIds != null) {
            Set<Integer> imageIdSet = new HashSet<>();

            // Each id is valid
            for (String id : imageIds) {
                try {
                    int imageId = Integer.parseInt(id);
                    if (!imageIdSet.add(imageId)) {
                    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("Duplicate image ID found: " + id);
                        return;
                    }
                    imageIdList.add(imageId);
                } catch (NumberFormatException e) {
                	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("Invalid image ID: " + id);
                    return;
                }
            }

            // Each image is mine
            boolean areImagesOwnedByUser = false;

            try {
                areImagesOwnedByUser = imageDAO.areImagesOwnedByUser(imageIdList, userId);
            } catch (SQLException e) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Database can't be reached, unable to check if every image is yours.");
                return;
            }

            if (!areImagesOwnedByUser) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Error, some images are not yours.");
                return;
            }
        }

        // Create album
        LocalDateTime date = LocalDateTime.now();
        Timestamp sqlDate = Timestamp.valueOf(date);
        int albumId = 0;

        try {
            albumId = albumDAO.createAlbum(title, userId, sqlDate);
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to create album.");
            return;
        }

        // Save images to the album if there are any
        if (!imageIdList.isEmpty()) {
            try {
                albumImageDAO.saveAlbumImages(albumId, imageIdList);
            } catch (SQLException e) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Database can't be reached, unable to save images in album.");
                return;
            }
        }

        if (albumId != 0) {
            String path = getServletContext().getContextPath() + "/ViewAlbum?albumId=" + albumId + "&pageNumber=0";
            response.sendRedirect(path);
        } else {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Unable to create album.");
        }
    }
	

}
