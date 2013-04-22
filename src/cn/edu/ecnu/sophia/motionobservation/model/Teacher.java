package cn.edu.ecnu.sophia.motionobservation.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Teacher implements Serializable {
	private int tid;// 教师id
	private String tno; // 教师工号
	private String tname; // 教师姓名
	private PeClass[] classes; // 教师所带班级
	
	public int loginResult; // 登录返回结果
	
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	
	public String getTno() {
		return tno;
	}
	public void setTno(String tno) {
		this.tno = tno;
	}
	
	public String getTname() {
		return tname;
	}
	public void setTname(String tname) {
		this.tname = tname;
	}
	
	public PeClass[] getClasses() {
		return classes;
	}
	public void setClasses(PeClass[] classes) {
		this.classes = classes;
	}
	
	
}
