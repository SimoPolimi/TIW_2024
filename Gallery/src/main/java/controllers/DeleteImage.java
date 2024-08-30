package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.User;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/DeleteImage")
public class DeleteImage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public DeleteImage() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int imageId = Integer.parseInt(request.getParameter("imageId"));
		ImageDAO imageDAO = new ImageDAO(connection);
		
		User user = (User) request.getSession().getAttribute("user");
		boolean isMyImage = false;
		
		try {
			isMyImage = imageDAO.isMyImage(imageId, user.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to check if this is your image.");
			return;
		}
		
		if(!isMyImage) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You can't delete other users' images.");
			return;
		}
		
		try {
			imageDAO.deleteImage(imageId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to delete image.");
			return;
		}
		
		String path = getServletContext().getContextPath() + "/ViewHome";
		response.sendRedirect(path);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
