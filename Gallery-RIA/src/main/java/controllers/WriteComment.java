package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.User;
import dao.CommentDAO;
import utils.ConnectionHandler;

@WebServlet("/WriteComment")
@MultipartConfig
public class WriteComment extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public WriteComment() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommentDAO commentDAO = new CommentDAO(connection);
		String text = request.getParameter("text").trim();
		int imageId = 0;
		
		// Check parameter is present
		if (text == null || text.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing text.");
            return;
		}
		
		LocalDateTime date = LocalDateTime.now();
		Timestamp sqlDate = Timestamp.valueOf(date);
		try {
			imageId = Integer.parseInt(request.getParameter("imageId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unable to find image, invalid input.");
            return;
        }
		int userId = ((User)request.getSession().getAttribute("user")).getId();
		
		// TODO: do better
		try {
			commentDAO.writeComment(imageId, userId, sqlDate, text);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to write comment.");
            return;
		}
		
		
	}

}
