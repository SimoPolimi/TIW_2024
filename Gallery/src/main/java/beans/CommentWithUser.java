package beans;

import java.sql.Date;

public class CommentWithUser {
	
	private int id;
	private int id_image;
	private String username;
	private Date date;
	private String text;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}


	public int getId_image() {
		return id_image;
	}

	public void setId_image(int id_image) {
		this.id_image = id_image;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	

}
