package cn.edu.ecnu.sophia.motionobservation.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;

import cn.edu.ecnu.sophia.motionobservation.model.Announcement;
import cn.edu.ecnu.sophia.motionobservation.model.PeClass;
import cn.edu.ecnu.sophia.motionobservation.model.Student;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;

public class JSONParser {
	/**
	 * 返回是否为心跳包的标志
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return true if the return message is a heart beat message. else false.
	 */
	public static boolean isHeartBeat(String returnMsg) {
		boolean isBeat = false;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject teacherJsonObject = new JSONObject(returnMsg);
			flag = teacherJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = teacherJsonObject.getString("RES");
				if (res.equals("beat")) { // 返回的消息是对教师登陆请求的响应结果，继续
					isBeat = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isBeat;
	}

	/**
	 * 返回消息的标识字符串
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 消息的标识字符串
	 */
	public static String getResponseString(String returnMsg) {
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject teacherJsonObject = new JSONObject(returnMsg);
			flag = teacherJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = teacherJsonObject.getString("RES");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 根据教师登录返回的消息获得已登录的教师对象
	 * 
	 * @param returnMsg
	 *            返回的json格式消息
	 * @return 教师对象
	 */
	public static Teacher getLoginTeacher(String returnMsg) {
		Teacher teacher = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		int result = -1; // 登录结果标志
		try {
			JSONObject teacherJsonObject = new JSONObject(returnMsg);
			flag = teacherJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = teacherJsonObject.getString("RES");
				if (res.equals("login_teacher")) { // 返回的消息是对教师登陆请求的响应结果，继续
					JSONObject dataJsonObject = teacherJsonObject
							.getJSONObject("DAT");
					// 获得登录结果标识
					result = dataJsonObject.getInt("result");
					System.out.println("result:" + result);
					// 判断登录结果
					if (result == 0) { // 登录成功
						teacher = new Teacher();
						teacher.loginResult = 0;
						teacher.setTid(dataJsonObject.getInt("tid"));
						System.out.println("tid:" + teacher.getTid());
						teacher.setTname(dataJsonObject.getString("tname"));
						System.out.println("tname:" + teacher.getTname());
						// 获取班级数组
						JSONArray classJsonArray = dataJsonObject
								.getJSONArray("classes");
						// 班级数目
						int len = classJsonArray.length();
						PeClass[] peClasses = new PeClass[len];
						for (int i = 0; i < len; i++) {
							peClasses[i] = new PeClass();
							JSONObject classObject = (JSONObject) classJsonArray
									.opt(i);
							System.out.println("第i个班级:");
							peClasses[i].setCid(classObject.getInt("cid"));
							System.out.println("cid:" + peClasses[i].getCid());
							peClasses[i].setCname(classObject
									.getString("cname"));
							System.out.println("cname:"
									+ peClasses[i].getCname());
						}
						teacher.setClasses(peClasses);
					} else if (result == 1) { // 用户不存在
						teacher = new Teacher();
						teacher.loginResult = 1;
					} else if (result == 2) { // 密码错误
						teacher = new Teacher();
						teacher.loginResult = 2;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return teacher;
	}

	/**
	 * 获得指定班级对象
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 班级对象
	 */
	public static PeClass getClassStudents(String returnMsg) {
		PeClass peClass = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject studentsJsonObject = new JSONObject(returnMsg);
			flag = studentsJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				peClass = new PeClass();
				res = studentsJsonObject.getString("RES");
				if (res.equals("class_students")) { // 返回的消息是对根据班级id请求班级学生的响应结果，继续
					JSONObject dataJsonObject = studentsJsonObject
							.getJSONObject("DAT");
					// 获取学生数组
					JSONArray studentJsonArray = dataJsonObject
							.getJSONArray("students");
					// 学生数目
					int len = studentJsonArray.length();
					Student[] students = new Student[len];
					for (int i = 0; i < len; i++) {
						students[i] = new Student();
						JSONObject classObject = (JSONObject) studentJsonArray
								.opt(i);
						//System.out.println("第" + i + "个学生:");
						students[i].setSid(classObject.getInt("sid"));
						//System.out.println("sid:" + students[i].getSid());
						students[i].setSno(classObject.getString("sno"));
						//System.out.println("sno:" + students[i].getSno());
						students[i].setSname(classObject.getString("sname"));
						//System.out.println("sname:" + students[i].getSname());
					}
					peClass.setStudents(students);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return peClass;
	}
	
	/**
	 * 获得指定班级所有学生头像的网络地址
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 班级对象
	 */
	public static Bundle getClassStudentAvatarsUrl(String returnMsg) {
		Bundle bundle = new Bundle();
		String prefix;
		int[] studentIds;
		String[] strs;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject studentsJsonObject = new JSONObject(returnMsg);
			flag = studentsJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = studentsJsonObject.getString("RES");
				if (res.equals("class_avatar")) { // 返回的消息是对根据班级id请求班级学生的响应结果，继续
					JSONObject dataJsonObject = studentsJsonObject
							.getJSONObject("DAT");
					
					prefix = dataJsonObject.getString("prefix");
					
					// 获取学生数组
					JSONArray avatarsJsonArray = dataJsonObject
							.getJSONArray("avatars");
					// 学生数目
					int len = avatarsJsonArray.length();
					studentIds = new int[len];
					strs = new String[len];
					for (int i = 0; i < len; i++) {
						JSONObject classObject = (JSONObject) avatarsJsonArray
								.opt(i);
						studentIds[i] = classObject.getInt("sid");
						strs[i] = classObject.getString("img");
					}
					bundle.putString("prefix", prefix);
					bundle.putIntArray("sids", studentIds);
					bundle.putStringArray("images", strs);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bundle;
	}
	
	/**
	 * 获得班级的实时数据（每10秒变化一次）
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 班级对象
	 */
	public static PeClass getClassStudentRealdata(String returnMsg) {
		PeClass peClass = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		//long timestamp; // 时间戳
		try {
			JSONObject classJsonObject = new JSONObject(returnMsg);
			flag = classJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				peClass = new PeClass();
				res = classJsonObject.getString("RES");
				if (res.equals("class_realdata")) { // 返回的消息是对根据班级id请求班级学生的响应结果，继续
					JSONObject dataJsonObject = classJsonObject
							.getJSONObject("DAT");
					// 获得时间戳
					//timestamp = dataJsonObject.getLong("timestamp");
					//System.out.println("timestamp:" + timestamp);
					// 获取班级实时运动数据
					
					int cid = dataJsonObject.getInt("cid");
					peClass.setCid(cid);
					
					JSONArray studentJsonArray = dataJsonObject
							.getJSONArray("realdatas");
					// 班级学生数目
					int len = studentJsonArray.length();
					Student[] students = new Student[len];
					for (int i = 0; i < len; i++) {
						students[i] = new Student();
						JSONObject stuObject = (JSONObject) studentJsonArray
								.opt(i);
						students[i].setSid(stuObject.getInt("sid"));
						double energy = stuObject.getDouble("data");
						students[i].setEnergyConsumption(energy);
					}
					peClass.setStudents(students);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return peClass;
	}

	/**
	 * 获得带实时运动数据的学生对象（每秒变化）
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 学生对象
	 */
	public static Student getStudentRealdata(String returnMsg) {
		Student students = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		//long timestamp; // 时间戳
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				students = new Student();
				res = stuJsonObject.getString("RES");
				if (res.equals("student_realdata")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");
					// 获得时间戳
					//timestamp = dataJsonObject.getLong("timestamp");
					//System.out.println("timestamp:" + timestamp);
					
					JSONArray stepsJsonArray = dataJsonObject
							.getJSONArray("steps"); // 获得学生运动步数
					int length = stepsJsonArray.length();
					int[] steps = new int[length];
					for (int m = 0; m < length; m++) {
						steps[m] = stepsJsonArray.getInt(m);
						//System.out.println("steps:" + steps[m]);
					}
					students.setSteps(steps);
					
					JSONArray personDataJsonArray = dataJsonObject
							.getJSONArray("person_data"); // 获得学生个人数据
					int length1 = personDataJsonArray.length();
					double[] persondatas = new double[length1];
					for (int m = 0; m < length1; m++) {
						persondatas[m] = personDataJsonArray.getDouble(m);
						//System.out.println("person_data:" + persondatas[m]);
					}
					students.setRealdata(persondatas);
					
					JSONArray classAvgDataJsonArray = dataJsonObject
							.getJSONArray("class_avg_data"); // 获得班级平均运动数据
					int length2 = classAvgDataJsonArray.length();
					double[] class_avg_datas = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_datas[n] = classAvgDataJsonArray.getDouble(n);
						//System.out.println("class_avg_data:" + class_avg_datas[n]);
					}
					students.setClassAvgEnergy(class_avg_datas);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return students;
	}
	
	/**
	 * 返回学生的详细信息
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 学生对象
	 */
	public static Student getStudentDetail(String returnMsg) {
		Student student = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				student = new Student();
				res = stuJsonObject.getString("RES");
				if (res.equals("student_detail")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");
					
					JSONArray detailJsonArray = dataJsonObject
							.getJSONArray("detail"); // 获得学生运动步数
					int length = detailJsonArray.length();
					for (int i = 0; i < length; i++) {
						JSONObject studentObject = detailJsonArray.getJSONObject(i);
						student.setSid(studentObject.getInt("sid"));
						student.setDeviceId(studentObject.getString("device_id"));
						student.setRemainPower(studentObject.getString("remain_power"));
						student.setWeight(studentObject.getDouble("weight"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return student;
	}
	
	/**
	 * 返回学生的本日运动数据
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 学生对象
	 */
	public static Student getStudentCurDayData(String returnMsg) {
		Student students = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				students = new Student();
				res = stuJsonObject.getString("RES");
				if (res.equals("curDay_student")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");

					// 获得学生运动步数
					int[] steps = new int[]{dataJsonObject.getInt("steps")};
					students.setSteps(steps);
					
					// 获得学生个人数据
					JSONArray stuDayJsonArray = dataJsonObject
							.getJSONArray("student_energy_day"); 
					int length1 = stuDayJsonArray.length();
					double[] persondatas = new double[length1];
					for (int m = 0; m < length1; m++) {
						persondatas[m] = stuDayJsonArray.getDouble(m);
						System.out.println("student_energy_day:" + persondatas[m]);
					}
					students.setRealdata(persondatas);
					
					// 获得班级平均运动数据
					JSONArray classAvgDayJsonArray = dataJsonObject
							.getJSONArray("class_avg_day"); 
					int length2 = classAvgDayJsonArray.length();
					double[] class_avg_data = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_data[n] = classAvgDayJsonArray.getDouble(n);
						System.out.println("class_avg_day:"
								+ class_avg_data[n]);
					}
					students.setClassAvgEnergy(class_avg_data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return students;
	}
	
	/**
	 * 返回学生的本周运动数据
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 学生对象
	 */
	public static Student getStudentCurWeekData(String returnMsg) {
		Student students = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				students = new Student();
				res = stuJsonObject.getString("RES");
				if (res.equals("curWeek_student")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");
					
					// 获得学生运动步数
					int[] steps = new int[]{dataJsonObject.getInt("steps")};
					students.setSteps(steps);
					
					// 获得学生个人数据
					JSONArray stuWeekJsonArray = dataJsonObject
							.getJSONArray("student_energy_week"); 
					int length1 = stuWeekJsonArray.length();
					double[] persondata = new double[length1];
					for (int m = 0; m < length1; m++) {
						persondata[m] = stuWeekJsonArray.getDouble(m);
						System.out.println("student_energy_week:" + persondata[m]);
					}
					students.setRealdata(persondata);
					
					// 获得班级平均运动数据
					JSONArray classAvgWeekJsonArray = dataJsonObject
							.getJSONArray("class_avg_week"); 
					int length2 = classAvgWeekJsonArray.length();
					double[] class_avg_data = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_data[n] = classAvgWeekJsonArray.getDouble(n);
						System.out.println("class_avg_week:"
								+ class_avg_data[n]);
					}
					students.setClassAvgEnergy(class_avg_data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return students;
	}

	/**
	 * 返回学生的本月运动数据
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 学生对象
	 */
	public static Student getStudentCurMonthData(String returnMsg) {
		Student students = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				students = new Student();
				res = stuJsonObject.getString("RES");
				if (res.equals("curMonth_student")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");

					// 获得学生运动步数
					int[] steps = new int[]{dataJsonObject.getInt("steps")};
					students.setSteps(steps);
					
					// 获得学生个人数据
					JSONArray stuMonthJsonArray = dataJsonObject
							.getJSONArray("student_energy_month"); 
					int length1 = stuMonthJsonArray.length();
					double[] persondata = new double[length1];
					for (int m = 0; m < length1; m++) {
						persondata[m] = stuMonthJsonArray.getDouble(m);
						System.out.println("student_energy_month:" + persondata[m]);
					}
					students.setRealdata(persondata);
					
					// 获得班级平均运动数据
					JSONArray classAvgMonthJsonArray = dataJsonObject
							.getJSONArray("class_avg_month"); 
					int length2 = classAvgMonthJsonArray.length();
					double[] class_avg_data = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_data[n] = classAvgMonthJsonArray.getDouble(n);
						System.out.println("class_avg_month:"
								+ class_avg_data[n]);
					}
					students.setClassAvgEnergy(class_avg_data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return students;
	}
	
	/**
	 * 获得班级的实时数据
	 * 
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 班级对象
	 */
	public static PeClass getClassRealdata(String returnMsg) {
		PeClass peClass = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		long timestamp; // 时间戳
		try {
			JSONObject classJsonObject = new JSONObject(returnMsg);
			flag = classJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				peClass = new PeClass();
				res = classJsonObject.getString("RES");
				if (res.equals("class_avg_realdata")) { // 返回的消息是对根据班级id请求班级学生的响应结果，继续
					JSONObject dataJsonObject = classJsonObject
							.getJSONObject("DAT");
					// 获得时间戳
					timestamp = dataJsonObject.getLong("timestamp");
					System.out.println("timestamp:" + timestamp);
					// 获取班级实时运动数据
					JSONArray classJsonArray = dataJsonObject
							.getJSONArray("class_avg_data");
					// 班级学生数目
					int len = classJsonArray.length();
					double[] avgData = new double[len];
					for (int i = 0; i < len; i++) {
						avgData[i] = classJsonArray.getDouble(i);
					}
					peClass.setClassAvgEnergy(avgData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return peClass;
	}

	public static PeClass getClassCurDayData(String returnMsg) {
		PeClass day_avg = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject classJsonObject = new JSONObject(returnMsg);
			flag = classJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				day_avg = new PeClass();
				res = classJsonObject.getString("RES");
				if (res.equals("curDay_class")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = classJsonObject
							.getJSONObject("DAT");						
									
					// 获得班级平均运动数据
					JSONArray classAvgDayJsonArray = dataJsonObject
							.getJSONArray("class_energy_day"); 
					int length2 = classAvgDayJsonArray.length();
					double[] class_avg_data = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_data[n] = classAvgDayJsonArray.getDouble(n);
						System.out.println("class_energy_day:"
								+ class_avg_data[n]);
					}
					day_avg.setClassDayAvgEnergy(class_avg_data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return day_avg;
	}
	
	/**
	 * 返回班级的本周运动数据
	 * @param returnMsg
	 *            服务端返回的json格式消息
	 * @return 学生对象
	 */
	public static PeClass getClassCurWeekData(String returnMsg) {
		PeClass energy_week = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				energy_week  = new PeClass();
				res = stuJsonObject.getString("RES");
				if (res.equals("curWeek_class")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");
									
					// 获得班级平均运动数据
					JSONArray classAvgWeekJsonArray = dataJsonObject
							.getJSONArray("class_energy_week"); 
					int length2 = classAvgWeekJsonArray.length();
					double[] class_avg_data = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_data[n] = classAvgWeekJsonArray.getDouble(n);
						System.out.println("class_avg_week:"
								+ class_avg_data[n]);
					}
					energy_week.setClassWeekAvgEnergy(class_avg_data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return energy_week;
	}
	
	public static PeClass getClassCurMonthData(String returnMsg) {
		PeClass energy_month = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject stuJsonObject = new JSONObject(returnMsg);
			flag = stuJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				energy_month  = new PeClass();
				res = stuJsonObject.getString("RES");
				if (res.equals("curMonth_class")) { // 返回的消息是对根据学生id请求学生实时运动数据的响应结果，继续
					JSONObject dataJsonObject = stuJsonObject
							.getJSONObject("DAT");
									
					// 获得班级平均运动数据
					JSONArray classAvgWeekJsonArray = dataJsonObject
							.getJSONArray("class_energy_month"); 
					int length2 = classAvgWeekJsonArray.length();
					double[] class_avg_data = new double[length2];
					for (int n = 0; n < length2; n++) {
						class_avg_data[n] = classAvgWeekJsonArray.getDouble(n);
						System.out.println("class_avg_month:"
								+ class_avg_data[n]);
					}
					energy_month .setClassMonthAvgEnergy(class_avg_data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return energy_month ;
	}
	
	public static Announcement[] getAnnouncement(String returnMsg){
		Announcement[] announcement = null;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject annJsonObject = new JSONObject(returnMsg);
			flag = annJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = annJsonObject.getString("RES");
				if (res.equals("getAnnouncement")) { // 返回的消息是获取的公告
					JSONObject dataJsonObject = annJsonObject
							.getJSONObject("DAT");
					
					// 公告总数
					//int totalAnn = dataJsonObject.getInt("total");
					
					// 获得公告
					JSONArray announcemsJsonArray = dataJsonObject
							.getJSONArray("announcements"); 
					int length = announcemsJsonArray.length();
					announcement = new Announcement[length];
					for (int i = 0; i < length; i++) {
						JSONObject jsonObject = announcemsJsonArray.getJSONObject(i);
						announcement[i] = new Announcement();
						announcement[i].setNewsId(jsonObject.getInt("news_id"));
						announcement[i].setTitle(jsonObject.getString("title"));
						announcement[i].setContent(jsonObject.getString("content"));
						announcement[i].setPublisher(jsonObject.getString("publisher"));
						announcement[i].setPublishTime(jsonObject.getString("publish_time"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return announcement;
	}
	

	public static boolean isNewAnnouncementSaved(String returnMsg){
		boolean success = false;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject annJsonObject = new JSONObject(returnMsg);
			flag = annJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = annJsonObject.getString("RES");
				if (res.equals("saveNewAnnouncement")) { // 返回的消息是获取的公告
					JSONObject dataJsonObject = annJsonObject
							.getJSONObject("DAT");
					int state = dataJsonObject.getInt("save_flag");
					if(state == 0){
						success = true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}
	
	public static int getDeleteAnnouncementCount(String returnMsg){
		int count = 0;
		int flag = -1; // 返回消息的状态代码
		String res = ""; // 返回消息的标识
		try {
			JSONObject annJsonObject = new JSONObject(returnMsg);
			flag = annJsonObject.getInt("RET");
			if (flag == 0) { // 返回状态正常，继续往下解析数据
				res = annJsonObject.getString("RES");
				if (res.equals("deleteAnnouncement")) { // 返回的消息是获取的公告
					JSONObject dataJsonObject = annJsonObject
							.getJSONObject("DAT");
					JSONArray stateJsonArray = dataJsonObject.getJSONArray("delete_flag");
					for(int i = 0; i < stateJsonArray.length(); i ++){
						if(stateJsonArray.getInt(i) == 0){
							count++;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	
}
