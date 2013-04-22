package cn.edu.ecnu.sophia.motionobservation.model;

public class NavTitle {
	
	private int imgId;
	private String text;
	
	public NavTitle(int imgId, String text) {
		super();
		this.imgId = imgId;
		this.text = text;
	}
	
	public int getImgId() {
		return imgId;
	}
	public void setImgId(int imgId) {
		this.imgId = imgId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
