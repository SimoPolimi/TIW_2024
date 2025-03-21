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

import beans.Album;
import beans.Image;
import beans.User;
import dao.AlbumDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewHome")
public class ViewHome extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ViewHome() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AlbumDAO albumDAO = new AlbumDAO(connection);
		List<Album> myAlbums = new ArrayList<Album>();
		List<Album> otherAlbums = new ArrayList<Album>();

		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> userImages = new ArrayList<Image>();

		User user = (User) request.getSession().getAttribute("user");

		try {
			myAlbums = albumDAO.getUserAlbums(user.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Database can't be reached, unable to find user albums.");
			return;
		}

		try {
			otherAlbums = albumDAO.getOtherUserAlbums(((User) request.getSession().getAttribute("user")).getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Database can't be reached, unable to find other albums.");
			return;
		}

		try {
			userImages = imageDAO.getUserImages(user.getId()); // Show my images
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Database can't be reached, unable to find user images.");
			return;
		}

		// Redirect
		String path = "/WEB-INF/home.html";
		ServletContext servletContext = getServletContext();
		response.setContentType("text");
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		// Recover errorMsg from uploadImage
		String errorMsg = (String) request.getAttribute("uploadErrorMsg");
		webContext.setVariable("uploadErrorMsg", errorMsg);
		
		if(myAlbums == null) {
			webContext.setVariable("myAlbumsMsg", "There are no albums.");
		}
		
		if(otherAlbums == null) {
			webContext.setVariable("otherAlbumsMsg", "There are no albums.");
		}
		
		
		
		// return
		webContext.setVariable("myAlbums", myAlbums);
		webContext.setVariable("otherAlbums", otherAlbums);
		webContext.setVariable("userImages", userImages);
		templateEngine.process(path, webContext, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
