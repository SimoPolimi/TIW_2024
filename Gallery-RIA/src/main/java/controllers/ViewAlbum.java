package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.Comment;
import beans.ImageWithComments;
import beans.User;
import dao.CommentDAO;
import dao.ImageDAO;
import dao.UserImageOrderDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewAlbum")
@MultipartConfig
public class ViewAlbum extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public ViewAlbum() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ImageDAO imageDAO = new ImageDAO(connection);
        List<ImageWithComments> images = new ArrayList<ImageWithComments>();
        
        int albumId = 0;
        
        try {
            albumId = Integer.parseInt(request.getParameter("albumId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unable to find album page, invalid input.");
            return;
        }
        
        int userId = ((User) request.getSession().getAttribute("user")).getId();
        
        UserImageOrderDAO userImageOrderDAO = new UserImageOrderDAO(connection);
		boolean isExistingOrder;

        try {
        	isExistingOrder = userImageOrderDAO.isExistingOrder(userId, albumId);
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to find custom order.");
            return;
        }
        
        try {
        	if(!isExistingOrder) {
        		images = imageDAO.getAlbumImages(albumId);
        	}else {
        		images = imageDAO.getAlbumImagesOrdered(albumId, userId);
        	}
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to find album.");
            return;
        }
        
        // Check if album has 0 images
        if (images == null) {
            // No images found, return an appropriate JSON response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"images\": [], \"hasImages\": false}");
            return;
        }
        
        // Recupera i commenti per ogni immagine
        CommentDAO commentDAO = new CommentDAO(connection);
        for (ImageWithComments image : images) {
            try {
                List<Comment> comments = commentDAO.getImageComments(image.getId());
                image.setComments(comments);  // Aggiungi i commenti all'immagine
            } catch (SQLException e) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Database error while retrieving comments.");
                return;
            }
        }

        // Convert the list of images with comments to JSON
        String jsonResponse = new Gson().toJson(images);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        response.getWriter().write(jsonResponse);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }
}
