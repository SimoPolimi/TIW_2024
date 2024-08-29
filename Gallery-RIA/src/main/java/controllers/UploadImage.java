package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import beans.User;
import dao.ImageDAO;
import utils.ConnectionHandler;

@WebServlet("/UploadImage")
@MultipartConfig(maxFileSize = 16177215) // 16MB
public class UploadImage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection;
    private static final String IMAGE_PATH = "C:/Users/Simo/TIW_images";


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ImageDAO imageDAO = new ImageDAO(connection);
        String name = request.getParameter("uploadImageTitle").trim();
        String description = request.getParameter("uploadImageDescription").trim();
        
        // Checks if fields are present
        if (name == null || name.isEmpty() || description == null || description.isEmpty()) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing field.");
            return;
        }

        // Check only 1 image to upload
        int imageCount = 0;
        Part imagePart = null;
        for (Part part : request.getParts()) {
            if (part.getName().equals("uploadImageFile") && part.getSize() > 0) {
                imageCount++;
                imagePart = part;
            }
        }

        if (imageCount != 1) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("You must upload exactly one image.");
            return;
        }

        String fileName = imagePart.getSubmittedFileName();

	    // Check file name
        if (fileName == null || fileName.trim().isEmpty() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid file name.");
            return;
        }
        
        String filePath = IMAGE_PATH + File.separator + fileName;

        // Check validity file
        String contentType = imagePart.getContentType();
        if (!contentType.startsWith("image/")) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid file type. Only image files are allowed.");
            return;
        }

        // Save in fileSystem
        try (InputStream inputStream = imagePart.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        int userId = ((User) request.getSession().getAttribute("user")).getId();
        LocalDate date = LocalDate.now();
        Date sqlDate = Date.valueOf(date);

        try {
            imageDAO.uploadImage(userId, name, sqlDate, description, fileName);
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database can't be reached, unable to upload image.");
            return;
        }

        String path = getServletContext().getContextPath() + "/ViewCreateAlbum";
        response.sendRedirect(path);
    }
}
