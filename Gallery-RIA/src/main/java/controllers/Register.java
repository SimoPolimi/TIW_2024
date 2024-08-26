package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.UserDAO;
import utils.ConnectionHandler;

@WebServlet("/Register")
@MultipartConfig
public class Register extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Register() {
		 super();
	}
	
	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username").trim();
		String email = request.getParameter("email").trim();
		String password = request.getParameter("password").trim();
		String confirmPassword = request.getParameter("confirmPassword").trim();
				
		if(username == null || email == null || password == null || confirmPassword == null || username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Empty field.");
			return;
		}
		
		if(!password.equals(confirmPassword)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Passwords do not match.");
			return;
		}
		
		if (!isValidEmail(email)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid email.");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		boolean isNewUsername = false;
		boolean isNewEmail = false;
		try {
			isNewUsername = userDao.isNewUsername(username);
			isNewEmail = userDao.isNewEmail(email);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to check if user already exists.");
			return;
		}
		
		if(!isNewUsername || !isNewEmail || !password.equals(confirmPassword)) { // Wrong credentials
			if(!isNewUsername) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            response.getWriter().println("Username already in use.");
				return;
			}else if(!isNewEmail) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            response.getWriter().println("Email already in use.");
				return;
			}
		}else { // Correct parameters
			try {
				userDao.registerUser(username, email, password);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            response.getWriter().println("Database can't be reached, unable to register user.");
				return;
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write("{\"status\":\"success\"}");	
		}
	}
	
	private boolean isValidEmail(String email) {
		final String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
		final Pattern emailPattern = Pattern.compile(emailRegex);
	    if (email == null) {
	        return false;
	    }
	    Matcher matcher = emailPattern.matcher(email);
	    return matcher.matches();
	}
	
	
	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
