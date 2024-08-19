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

import beans.User;
import dao.UserDAO;
import utils.ConnectionHandler;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckLogin() {
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
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String path;
		
		ServletContext servletContext = getServletContext();
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		
		if(email == null || password == null || email.isBlank() || password.isBlank()) {
			// Reload
			webContext.setVariable("errorMsg", "Empty field.");
			path = "/login.html";
			templateEngine.process(path, webContext, response.getWriter());
			return;
		}
		
		if (!isValidEmail(email)) {
			// Reload
			webContext.setVariable("errorMsg", "Invalid email.");
			path = "/login.html";
			templateEngine.process(path, webContext, response.getWriter());
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(email, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to check user credentials.");
			return;
		}
		
		if(user == null) { // Wrong credentials
			webContext.setVariable("emailReceived", (email != null || !email.isBlank() ? email : ""));
			webContext.setVariable("errorMsg", "Invalid credentials, wrong email or password.");
			path = "/login.html";
			templateEngine.process(path, webContext, response.getWriter());
		} else { // Correct credentials, redirect to Home
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/ViewHome";
			response.sendRedirect(path);
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
