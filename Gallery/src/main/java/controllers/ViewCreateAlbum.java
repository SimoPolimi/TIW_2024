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
import beans.User;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/ViewCreateAlbum")
public class ViewCreateAlbum extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public ViewCreateAlbum() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ImageDAO imageDAO = new ImageDAO(connection);
        List<Image> images = new ArrayList<Image>();

        try {
            User user = (User) request.getSession().getAttribute("user");
            images = imageDAO.getUserImages(user.getId()); // Show my images
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String path = "/WEB-INF/createAlbum.html";
        ServletContext servletContext = getServletContext();
        response.setContentType("text/html;charset=UTF-8");
        final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
        webContext.setVariable("images", images);
        templateEngine.process(path, webContext, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
