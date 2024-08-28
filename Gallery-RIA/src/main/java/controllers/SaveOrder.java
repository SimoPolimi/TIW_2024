package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import dao.AlbumImageDAO;
import dao.UserImageOrderDAO;
import utils.ConnectionHandler;

import beans.User;

@WebServlet("/SaveOrder")
@MultipartConfig
public class SaveOrder extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection;

    public SaveOrder() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        System.out.println("JSON Received: " + sb.toString());

        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);

            // Checks if albumId and order exist in JSON
            if (jsonObject.has("albumId") && jsonObject.has("order")) {
                JsonElement albumIdElement = jsonObject.get("albumId");
                JsonElement orderArrayElement = jsonObject.get("order");

                if (albumIdElement.isJsonNull() || !orderArrayElement.isJsonArray()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Invalid or missing fields in JSON.");
                    return;
                }

                int albumId = albumIdElement.getAsInt();
                JsonArray orderArray = orderArrayElement.getAsJsonArray();

                List<Integer> imageIds = new ArrayList<>();
                List<Integer> positions = new ArrayList<>();
                Map<Integer, Integer> imageCountMap = new HashMap<>();

                for (JsonElement element : orderArray) {
                    if (element.isJsonObject()) {
                        JsonObject orderItem = element.getAsJsonObject();
                        JsonElement imageIdElement = orderItem.get("imageId");
                        JsonElement positionElement = orderItem.get("position");

                        if (imageIdElement.isJsonNull() || positionElement.isJsonNull()) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write("Missing imageId or position in order item.");
                            return;
                        }

                        int imageId = imageIdElement.getAsInt();
                        int position = positionElement.getAsInt();

                        if (imageId <= 0 || position < 0) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write("Invalid image ID or position.");
                            return;
                        }

                        // Track occurrences of imageId
                        imageIds.add(imageId);
                        positions.add(position);
                        imageCountMap.put(imageId, imageCountMap.getOrDefault(imageId, 0) + 1);
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("Invalid format in order array.");
                        return;
                    }
                }

                // Check for duplicates
                if (imageIds.size() != new HashSet<>(imageIds).size()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Duplicate image IDs detected.");
                    return;
                }

                AlbumImageDAO albumImageDAO = new AlbumImageDAO(connection);
                List<Integer> existingImageIds = albumImageDAO.getImageIdsByAlbum(albumId);

                // Check if all imageIds are present in existingImageIds
                if (!existingImageIds.containsAll(imageIds) || !imageIds.containsAll(existingImageIds) || existingImageIds.size() != imageIds.size()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Some image IDs are not valid for this album.");
                    return;
                }

                int userId = ((User) request.getSession().getAttribute("user")).getId();
                UserImageOrderDAO userImageOrderDAO = new UserImageOrderDAO(connection);
                boolean isExistingOrder = userImageOrderDAO.isExistingOrder(userId, albumId);

                int rowsAffected;
                if (!isExistingOrder) {
                    rowsAffected = userImageOrderDAO.insertImageOrder(userId, albumId, imageIds, positions);
                } else {
                    rowsAffected = userImageOrderDAO.updateImageOrder(userId, albumId, imageIds, positions);
                }

                response.setContentType("text/plain");
                response.getWriter().write("Order saved successfully, rows affected: " + rowsAffected);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Missing required fields in JSON.");
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid JSON format.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error.");
        }
    }
}
