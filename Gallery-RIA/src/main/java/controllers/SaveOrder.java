package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import dao.UserImageOrderDAO;
import utils.ConnectionHandler;

import com.google.gson.JsonSyntaxException;

import beans.User;

@WebServlet("/SaveOrder")
@MultipartConfig
public class SaveOrder extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public SaveOrder() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

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

			// Verifica che albumId e order siano presenti nel JSON
			if (jsonObject.has("albumId") && jsonObject.has("order")) {
				int albumId = jsonObject.get("albumId").getAsInt();
				JsonArray orderArray = jsonObject.getAsJsonArray("order");

				List<Integer> imageIds = new ArrayList<>();
				List<Integer> positions = new ArrayList<>();

				for (JsonElement element : orderArray) {
					JsonObject orderItem = element.getAsJsonObject();
					int imageId = orderItem.get("imageId").getAsInt();
					int position = orderItem.get("position").getAsInt();

					imageIds.add(imageId);
					positions.add(position);
				}

				int userId = ((User) request.getSession().getAttribute("user")).getId();
				UserImageOrderDAO userImageOrderDAO = new UserImageOrderDAO(connection);
				boolean isExistingOrder = userImageOrderDAO.isExistingOrder(userId, albumId);

				int rowsAffected = 0;
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