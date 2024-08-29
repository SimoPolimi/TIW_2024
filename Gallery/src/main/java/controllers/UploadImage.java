package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.User;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/UploadImage")
@MultipartConfig(maxFileSize = 16177215) // 16MB
public class UploadImage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

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
        ImageDAO imageDAO = new ImageDAO(connection);
        String name = request.getParameter("name").trim();
        String description = request.getParameter("description").trim();
        
        // Controlla se i campi name e description sono presenti
        if (name == null || name.isEmpty() || description == null || description.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing field.");
            return;
        }

        // Ottieni tutte le parti del file dalla richiesta
        int imageCount = 0;
        Part imagePart = null;
        for (Part part : request.getParts()) {
            if (part.getName().equals("image") && part.getSize() > 0) {
                imageCount++;
                imagePart = part;
            }
        }

        // Verifica che ci sia esattamente una parte immagine
        if (imageCount != 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You must upload exactly one image.");
            return;
        }

        String fileName = imagePart.getSubmittedFileName();

        // Verifica se il nome del file è valido
        if (fileName == null || fileName.trim().isEmpty() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file name.");
            return;
        }
        
        String filePath = getServletContext().getRealPath("/images/") + File.separator + fileName;

        // Valida il file
        String contentType = imagePart.getContentType();
        if (!contentType.startsWith("image/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file type. Only image files are allowed.");
            return;
        }

        // Salva il file nel file system
        try (InputStream inputStream = imagePart.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        // Ottieni l'ID dell'utente dalla sessione
        int userId = ((User) request.getSession().getAttribute("user")).getId();
        LocalDate date = LocalDate.now();
        Date sqlDate = Date.valueOf(date);

        // Inserisci i dati nel database
        try {
            imageDAO.uploadImage(userId, name, sqlDate, description, fileName);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to upload image.");
            return;
        }

        // Reindirizza alla pagina di creazione dell'album
        String path = getServletContext().getContextPath() + "/ViewCreateAlbum";
        response.sendRedirect(path);
    }
}
