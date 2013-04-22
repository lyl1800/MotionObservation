package cn.edu.ecnu.sophia.motionobservation.dao;

import java.util.Comparator;

import cn.edu.ecnu.sophia.motionobservation.model.Student;

public class StudentEnergyComparator implements Comparator<Student> {

	@Override
	public int compare(Student stu1, Student stu2) {
		int compareValue = (int) (stu2.getEnergyConsumption()*100 - stu1
				.getEnergyConsumption()*100);
		if (compareValue == 0) {
			return stu1.getSno().compareTo(stu2.getSno());
		} else {
			return compareValue;
		}
	}
}
