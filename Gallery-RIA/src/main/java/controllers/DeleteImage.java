package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.User;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/DeleteImage")
@MultipartConfig
public class DeleteImage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public DeleteImage() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ImageDAO imageDAO = new ImageDAO(connection);	
		User user = (User) request.getSession().getAttribute("user");
		
		int imageId = 0;
		try {
			imageId = Integer.parseInt(request.getParameter("imageId"));
		}catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to find image, invalid input.");
			return;
		}
		
		boolean isMyImage = false;
		
		try {
			isMyImage = imageDAO.isMyImage(imageId, user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to check if this is your image.");
			return;
		}
		
		if(!isMyImage) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("You can't delete other users' images.");
			return;
		}
		
		try {
			imageDAO.deleteImage(imageId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to delete image.");
			return;
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
