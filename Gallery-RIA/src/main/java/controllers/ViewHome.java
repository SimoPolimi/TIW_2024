package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.Album;
import beans.ImageWithComments;
import beans.User;
import dao.AlbumDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewHome")
@MultipartConfig
public class ViewHome extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ViewHome() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AlbumDAO albumDAO = new AlbumDAO(connection);
		List<Album> myAlbums = new ArrayList<Album>();
		List<Album> otherAlbums = new ArrayList<Album>();
		
		ImageDAO imageDAO = new ImageDAO(connection);
        List<ImageWithComments> userImages = new ArrayList<ImageWithComments>();
		
		User user = (User) request.getSession().getAttribute("user");
		
		try {
			myAlbums = albumDAO.getUserAlbums(((User)request.getSession().getAttribute("user")).getId());	
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to find user albums.");
			return;
		}
		
		try {
			otherAlbums = albumDAO.getOtherUserAlbums(((User)request.getSession().getAttribute("user")).getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to find other albums.");
			return;
		}
		
		try {
            userImages = imageDAO.getUserImages(user.getId()); // Show my images
        } catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to find user images.");
			return;
        }
		
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("myAlbums", myAlbums);
		responseMap.put("otherAlbums", otherAlbums);
		responseMap.put("userImages", userImages);

		String jsonResponse = new Gson().toJson(responseMap);

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
