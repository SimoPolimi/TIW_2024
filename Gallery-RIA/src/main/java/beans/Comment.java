package beans;

import java.sql.Timestamp;

public class Comment {
	
	private int id;
	private ImageWithComments image;
	private User user;
	private Timestamp date;
	private String text;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}


	public ImageWithComments getImage() {
		return image;
	}

	public void setImage(ImageWithComments image) {
		this.image = image;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	

}
