package cn.edu.ecnu.sophia.motionobservation.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Teacher implements Serializable {
	private int tid;// ��ʦid
	private String tno; // ��ʦ����
	private String tname; // ��ʦ����
	private PeClass[] classes; // ��ʦ�����༶
	
	public int loginResult; // ��¼���ؽ��
	
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
