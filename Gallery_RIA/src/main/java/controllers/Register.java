package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import dao.UserDAO;
import utils.ConnectionHandler;

@WebServlet("/Register")
public class Register extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public Register() {
		 super();
	}
	
	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		String path;
		
		ServletContext servletContext = getServletContext();
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		
		if(username == null || email == null || password == null || confirmPassword == null || username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
			// Reload
			webContext.setVariable("errorMsg", "Empty field.");
			path = "/registration.html";
			templateEngine.process(path, webContext, response.getWriter());
			return;
		}
		
		if(!password.equals(confirmPassword)){
			webContext.setVariable("errorMsg", "Password do not match");
			path = "/registration.html";
			templateEngine.process(path, webContext, response.getWriter());
			return;
		}
		
		if (!isValidEmail(email)) {
			// Reload
			webContext.setVariable("errorMsg", "Invalid email.");
			path = "/registration.html";
			templateEngine.process(path, webContext, response.getWriter());
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		boolean isNewUsername = false;
		boolean isNewEmail = false;
		try {
			isNewUsername = userDao.isNewUsername(username);
			isNewEmail = userDao.isNewEmail(email);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to check if user already exists.");
			return;
		}
		
		if(!isNewUsername || !isNewEmail || !password.equals(confirmPassword)) { // Wrong credentials
			webContext.setVariable("emailReceived", ((email != null || !email.isBlank()) && isNewEmail ? email : ""));
			if(!isNewUsername) {
				webContext.setVariable("errorMsg", "Username already in use");
			}else if(!isNewEmail) {
				webContext.setVariable("errorMsg", "Email already in use");
			}
			path = "/registration.html";
			templateEngine.process(path, webContext, response.getWriter());	
		}else { // Correct parameters
			try {
				userDao.registerUser(username, email, password);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to register user.");
				return;
			}
			webContext.setVariable("errorMsg", "Registration successful");
			path = "/login.html";
			templateEngine.process(path, webContext, response.getWriter());	
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
