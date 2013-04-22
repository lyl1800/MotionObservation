package cn.edu.ecnu.sophia.motionobservation.dao;

import java.util.Comparator;

import cn.edu.ecnu.sophia.motionobservation.model.Student;

public class StudentNumberComparator implements Comparator<Student> {

	@Override
	public int compare(Student stu1, Student stu2) {
		int sno1 = Integer.parseInt(stu1.getSno().substring(8,11));
		int sno2 = Integer.parseInt(stu2.getSno().substring(8,11));
		return sno1 - sno2;
	}
}
