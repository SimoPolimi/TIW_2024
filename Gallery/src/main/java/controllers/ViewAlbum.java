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

import beans.Image;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewAlbum")
public class ViewAlbum extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	/* Constant */
	private static final int IMAGES_PER_PAGE = 5;

	public ViewAlbum() {
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
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> images = new ArrayList<Image>();
		int albumId = Integer.parseInt(request.getParameter("albumId"));
		int totalPages = 0;
		int pageNumber; /* Starts from 0 */
		if(request.getParameter("pageNumber") == null) {
			pageNumber = 0;
		} else {
			pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
		}
			
		
			
		try {
			int offset = pageNumber * IMAGES_PER_PAGE;
			images = imageDAO.showAlbumImages(albumId, IMAGES_PER_PAGE, offset);
			int totalImages = imageDAO.countImagesByAlbumId(albumId);
            totalPages = (int) Math.ceil((double) totalImages / IMAGES_PER_PAGE);
            
            /* fills with null images to fill table cells */
            /*****************************************************/
            if(images != null) {
            	while (images.size() < 5) {
            		images.add(null);
            	}
            }
            /*****************************************************/
            
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	
		// Redirect
		String path = "/WEB-INF/album.html";
		ServletContext servletContext = getServletContext();
		response.setContentType("text");
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		// return
		webContext.setVariable("images", images);
		webContext.setVariable("currentPage", pageNumber);
		webContext.setVariable("totalPages", totalPages);
		templateEngine.process(path, webContext, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
