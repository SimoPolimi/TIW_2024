package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import beans.Comment;
import beans.Image;
import dao.CommentDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewImage")
public class ViewImage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ViewImage() {
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
		// Image
		ImageDAO imageDAO = new ImageDAO(connection);
		Image image = new Image();
		int imageId = 0;
		
		try {
			imageId = Integer.parseInt(request.getParameter("imageId"));
		}catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to find image, invalid input.");
			return;
		}
		try {
			image = imageDAO.getImageById(imageId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to find image.");
			return;
		}
		
		// Comments
		
		CommentDAO commentDAO = new CommentDAO(connection);
		List<Comment> comments = new ArrayList<Comment>();
		
		try {
			comments = commentDAO.getImageComments(imageId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to find image comments.");
			return;
		}	
			
		// Redirect
		String path = "/WEB-INF/image.html";
		ServletContext servletContext = getServletContext();
		response.setContentType("text");
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		
		if(comments == null) {
			webContext.setVariable("CommentsMsg", "There are no comments.");
		}
		
		// return
		webContext.setVariable("clickedImage", image);
		webContext.setVariable("comments", comments);
		// To go back to albums
		webContext.setVariable("albumId", request.getParameter("albumId"));
		webContext.setVariable("pageNumber", request.getParameter("pageNumber"));

		// TODO: check
		templateEngine.process(path, webContext, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
