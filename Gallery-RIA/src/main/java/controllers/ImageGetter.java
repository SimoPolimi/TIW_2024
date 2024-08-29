package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/imageGetter")
public class ImageGetter extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String IMAGE_PATH = "C:/Users/Simo/TIW_images";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String imageName = request.getParameter("imageName");

        if (imageName == null || imageName.isEmpty()) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Image name is missing.");
            return;
        }

        File imageFile = new File(IMAGE_PATH, imageName);
        if (!imageFile.exists()) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("Image not found.");
            return;
        }

        response.setContentType(getServletContext().getMimeType(imageFile.getName()));
        response.setContentLength((int) imageFile.length());

        
        try (FileInputStream in = new FileInputStream(imageFile);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
