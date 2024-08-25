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

import beans.ImageWithComments;
import beans.User;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewCreateAlbum")
@MultipartConfig
public class ViewCreateAlbum extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ViewCreateAlbum() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ImageDAO imageDAO = new ImageDAO(connection);
		List<ImageWithComments> images = new ArrayList<ImageWithComments>();

		try {
			User user = (User) request.getSession().getAttribute("user");
			images = imageDAO.getUserImages(user.getId()); // Show my images
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to find user images.");
			return;
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
		doGet(request, response);
	}
}
