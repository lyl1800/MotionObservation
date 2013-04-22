package cn.edu.ecnu.sophia.motionobservation.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Announcement implements Serializable {
	private boolean checked; // 公告选中状态
	private int newsId; //公告id
	private String title; // 公告标题
	private int type; // 公告类型
	private String content; // 公告内容
	private String publisher; // 发布者
	private String publishTime; // 发布时间
	public int getNewsId() {
		return newsId;
	}
	public void setNewsId(int newsId) {
		this.newsId = newsId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	
}
