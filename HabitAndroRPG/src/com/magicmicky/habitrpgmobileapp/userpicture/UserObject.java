package com.magicmicky.habitrpgmobileapp.userpicture;

public class UserObject {
	  private String image;
	  private int x;
	  private int y;
	  private int width;
	  private int height;
	  
	  /*public UserObject(String imageUrl, String position, String str_width, String str_height) {
		  String [] x_y=position.replace("px", "").split("\\s+");
		  String image = imageUrl.replace("url(",	"").replace(")", "");
		  int x = Integer.parseInt(x_y[0]);
		  int y =Integer.parseInt(x_y[1]);
		  int width = Integer.parseInt(str_width.replace("px", ""));
		  int height = Integer.parseInt(str_height.replace("px", ""));
		  //this(image,x,y,width,height);
	  }*/
	  public UserObject(String image, int x, int y, int width, int height) {
		  this.setImage(image);
		  this.setX(x);
		  this.setY(y);
		  this.setWidth(width);
		  this.setHeight(height);
	  }

	/**
	 * @return the image
	 */
	public String getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
}
