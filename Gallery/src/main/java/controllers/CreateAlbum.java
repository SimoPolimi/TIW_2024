package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import dao.AlbumDAO;
import dao.AlbumImageDAO;
import utils.ConnectionHandler;

@WebServlet("/CreateAlbum")
public class CreateAlbum extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CreateAlbum() {
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
		AlbumDAO albumDAO = new AlbumDAO(connection);
		AlbumImageDAO albumImageDAO = new AlbumImageDAO(connection);
		String title = request.getParameter("title");
		
		// Check parameter is present
		if (title == null || title.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing title");
			return;
		}
		
		LocalDate date = LocalDate.now();
		java.sql.Date sqlDate = java.sql.Date.valueOf(date);
		int userId = ((User)request.getSession().getAttribute("user")).getId();
		int albumId = 0;
		
		try {
			albumId = albumDAO.createAlbum(title, userId, sqlDate);
			String[] imageIds = request.getParameterValues("selectedImages");
            if (imageIds != null) {
                List<Integer> imageIdList = new ArrayList<>();
                for (String id : imageIds) {
                    imageIdList.add(Integer.parseInt(id));
                }
                albumImageDAO.saveAlbumImages(albumId, imageIdList);
            }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(albumId != 0) {
			String path = getServletContext().getContextPath() + "/ViewAlbum?albumId=" + albumId + "&pageNumber=0";
			response.sendRedirect(path);
		}else {
			String path = getServletContext().getContextPath() + "/ViewHome";
			response.sendRedirect(path);
		}
		
	}

}
