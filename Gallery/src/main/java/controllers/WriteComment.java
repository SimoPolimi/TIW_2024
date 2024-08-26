package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

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
import dao.CommentDAO;
import utils.ConnectionHandler;

@WebServlet("/WriteComment")
public class WriteComment extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public WriteComment() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommentDAO commentDAO = new CommentDAO(connection);
		String text = request.getParameter("text").trim();
		
		// Check parameter is present
		if (text == null || text.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing text");
			return;
		}
		
		LocalDate date = LocalDate.now();
		java.sql.Date sqlDate = java.sql.Date.valueOf(date);
		int imageId = Integer.parseInt(request.getParameter("imageId"));
		int userId = ((User)request.getSession().getAttribute("user")).getId();
		
		// TODO: do better
		try {
			commentDAO.writeComment(imageId, userId, sqlDate, text);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to write comment.");
			return;
		}
		
		int albumId = Integer.parseInt(request.getParameter("albumId"));
		int pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
		String path = getServletContext().getContextPath() + "/ViewImage?imageId=" + imageId +"&albumId=" + albumId + "&pageNumber=" + pageNumber;
		response.sendRedirect(path);
	}

}
