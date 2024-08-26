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

import com.google.gson.Gson;

import beans.User;
import dao.UserDAO;
import utils.ConnectionHandler;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckLogin() {
		 super();
	}
	
	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		// TODO: deleted something... needed????
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email").trim();
		String password = request.getParameter("password").trim();

		// TODO - Debug print
		System.out.println("req.getParameter(\"email\"): " + request.getParameter("email") + "\nreq.getParameter(\"password\"): " + request.getParameter("password"));
		
		if(email == null || password == null || email.isBlank() || password.isBlank()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Empty field.");
			return;
		}
		
		if (!isValidEmail(email)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid email.");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(email, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to check user credentials.");
			return;
		}
		
		if(user == null) { // Wrong credentials
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Invalid credentials, wrong email or password.");
			return;
		} else { // Correct credentials
			// Conversion to json needed because sessionStorage can only contain String
			String jsonUser = new Gson().toJson(user);
			
			request.getSession().setAttribute("user", user);
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			// Conversion to json needed because sessionStorage can only contain String
			response.getWriter().write(jsonUser);
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
