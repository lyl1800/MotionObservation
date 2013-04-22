package cn.edu.ecnu.sophia.motionobservation.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PeClass implements Serializable {
	private int cid; //班级id
	private String cname; //班级名称
	private Student[] students; // 班级的学生
	private double[] classAvgEnergy; // 学生所在班级的实时平均能量消耗
	private double[] classWeekAvgEnergy;
	private double[] classDayAvgEnergy;
	private double[] classMonthAvgEnergy;	
	
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public String getCname() {
		return cname;
	}
	public void setCname(String cname) {
		this.cname = cname;
	}
	public Student[] getStudents() {
		return students;
	}
	public void setStudents(Student[] students) {
		this.students = students;
	}
	public double[] getClassAvgEnergy() {
		return classAvgEnergy;
	}
	public void setClassAvgEnergy(double[] avg_energy) {
		this.classAvgEnergy= avg_energy;
	}
	public double[] getClassDayAvgEnergy() {
		return classDayAvgEnergy;
	}
	public void setClassDayAvgEnergy(double[] avg_energy) {
		this.classDayAvgEnergy= avg_energy;
	}
	public double[] getClassWeekAvgEnergy() {
		return classWeekAvgEnergy;
	}
	public void setClassWeekAvgEnergy(double[] avg_energy) {
		this.classWeekAvgEnergy= avg_energy;
	}
	public double[] getClassMonthAvgEnergy() {
		return classMonthAvgEnergy;
	}
	public void setClassMonthAvgEnergy(double[] avg_energy) {
		this.classMonthAvgEnergy= avg_energy;
	}
	
}
