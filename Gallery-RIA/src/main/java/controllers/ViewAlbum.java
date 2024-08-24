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

import beans.Image;
import dao.ImageDAO;
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
		List<Image> images = new ArrayList<Image>();
		
		int albumId = 0;
		
		try {
			albumId = Integer.parseInt(request.getParameter("albumId"));
		}catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unable to find album page, invalid input.");
			return;
		}
		
			
		try {
			images = imageDAO.getAlbumImages(albumId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to find album.");
			return;
		}
	
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
