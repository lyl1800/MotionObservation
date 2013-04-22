package cn.edu.ecnu.sophia.motionobservation.model;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

@SuppressWarnings("serial")
public class Student implements Serializable {
	private int sid; // 学生id
	private String sno; // 学生学号
	private String sname; // 学生姓名
	private byte[] avatar; //学生头像
	private String deviceId; // 计步器ID
	private String remainPower; // 计步器剩余电量
	private double weight; // 学生体重
	private double energyConsumption; // 能量消耗
	private int peClassid; // 所在班级id
	private int[] steps; // 运动步数
	private double[] realdata; // 实时能量消耗
	private double[] classAvgEnergy; // 学生所在班级的实时平均能量消耗
	
	public Student(){
		
	}
	public Student(String sno, String sname) {
		super();
		this.sno = sno;
		this.sname = sname;
		this.energyConsumption = 0.0;
	}
	public Student(String sno, String sname, double energy_consumption) {
		super();
		this.sno = sno;
		this.sname = sname;
		this.energyConsumption = energy_consumption;
	}
	
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public String getSno() {
		return sno;
	}
	public void setSno(String sno) {
		this.sno = sno;
	}
	public String getSname() {
		return sname;
	}
	public void setSname(String sname) {
		this.sname = sname;
	}
	public double getEnergyConsumption() {
		return energyConsumption;
	}
	public void setEnergyConsumption(double energyConsumption) {
		this.energyConsumption = energyConsumption;
	}
	public double[] getRealdata() {
		return realdata;
	}
	public void setRealdata(double[] realdata) {
		this.realdata = realdata;
	}
	public double[] getClassAvgEnergy() {
		return classAvgEnergy;
	}
	public void setClassAvgEnergy(double[] classAvgEnergy) {
		this.classAvgEnergy = classAvgEnergy;
	}

	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getRemainPower() {
		return remainPower;
	}
	public void setRemainPower(String remainPower) {
		this.remainPower = remainPower;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public int[] getSteps() {
		return steps;
	}
	public void setSteps(int[] steps) {
		this.steps = steps;
	}
	public int getPeClassid() {
		return peClassid;
	}
	public void setPeClassid(int peClassid) {
		this.peClassid = peClassid;
	}
	
	public Bitmap getBitmapAvatar(){
		if(this.avatar == null){
			return null;
		} else {
			return BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
		}
	}
	
	public void setBitmapAvatar(Bitmap bm){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 0, baos);
		this.avatar = baos.toByteArray();
	}
}
