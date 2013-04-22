package cn.edu.ecnu.sophia.motionobservation.motion.classes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.dao.DownloadImage;
import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;
import cn.edu.ecnu.sophia.motionobservation.dao.StudentEnergyComparator;
import cn.edu.ecnu.sophia.motionobservation.dao.StudentNumberComparator;
import cn.edu.ecnu.sophia.motionobservation.model.PeClass;
import cn.edu.ecnu.sophia.motionobservation.model.Student;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

public class ClassExerciseFragment extends Fragment implements OnClickListener,
		 OnItemSelectedListener {

	// 与Service绑定的状态
	private boolean mIsBound = false;
	// 绑定的Service
	private SocketService mBoundService;

	// 实现定时刷新
	private static Timer timer = new Timer();
	private TimerTask task;

	// 用于更新学生列表的Handler
	private Handler updateStudentListHandler;

	// 控件
	private Spinner spn_order, spn_main_class;
	private Button btn_show_class_motion;
	private ArrayAdapter<String> order_adapter, class_adapter;

	// 班级id和班级名称数组
	private static int[] arr_classid;
	private static String[] arr_className;
	// 排序方式
	private static String[] arr_order = { "按学号排序", "按运动量排序" };

	// 包含学生信息的List,GridView及其Adapter
	private List<Student> studentListItems;
	private ClassStudentListAdapter studentListAdapter;
	private GridView gv_student_list;

	// 发送的登录请求信息
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";

	// 服务端返回的登录结果消息
	private String returnMessage = "";

	// 当前班级在班级列表中的索引值
	private int currentClassIndex = 0;
	
	// 当前班级
	private PeClass curPeClass;
	
	// 当前教师所带班级
	private PeClass[] peClasses;
	
	// 登录的教师
	private Teacher teacher;

	// 实时数据存储
	private Map<Integer, Double> classRealDataMap;
	
	// 存储头像
	private static String directory;
	private Handler myHttpHandler;
	private final static int DOWNLOAD_OK = 0;
	private SharedPreferences spAvatar;//以学号-文件名为键值对的头像映射文件

	public static OnShowMotionListener mCallback;
	
	public interface OnShowMotionListener {
		public void onClassSelected(PeClass curPeClass);
		public void onStudentClicked(Student student);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnShowMotionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement OnShowMotionListener");
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("ClassExerciseFragment OnDestroy");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("ClassExerciseFragment解除与Service的绑定");
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("ClassExerciseFragment onStop");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("ClassExerciseFragment解除与Service的绑定");
		}
		// 取消定时器
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.class_exercise_main, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		spAvatar = getActivity().getSharedPreferences("spAvatar", Context.MODE_PRIVATE);
		classRealDataMap  = new HashMap<Integer, Double>();

		MainActivity.setIsHome(false);
		getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).mkdirs();
		directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		myHttpHandler = new MyHttpHandler(Looper.getMainLooper());
		
		Bundle bundle = getArguments();
		teacher = (Teacher) bundle.getSerializable("teacher");
		if (teacher != null) {
			peClasses = teacher.getClasses();
			int len = peClasses.length;
			arr_className = new String[len];
			arr_classid = new int[len];
			for (int i = 0; i < len; i++) {
				arr_classid[i] = peClasses[i].getCid();
				arr_className[i] = peClasses[i].getCname();
			}
		}
		
		Intent intent = new Intent(getActivity(), SocketService.class);
		// 先打开Service, 然后将当前Activity与该Service绑定
		if(mBoundService == null || !mIsBound){
			System.out.println("将ClassExerciseFragment绑定到服务");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("完成将ClassExerciseFragment绑定到服务");
		}
		
		/** 初始化界面控件 */
		// 实例化控件
		spn_order = (Spinner) getActivity().findViewById(R.id.spn_order);
		spn_main_class = (Spinner) getActivity().findViewById(R.id.spn_cur_class);
		btn_show_class_motion = (Button) getActivity().findViewById(R.id.btn_show_class_cur_motion);
		gv_student_list = (GridView) getActivity().findViewById(R.id.gv_student_list);

		// 初始化两个下拉框
		order_adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arr_order);
		order_adapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spn_order.setAdapter(order_adapter);
		spn_order.setSelection(1);

		class_adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arr_className);
		class_adapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spn_main_class.setAdapter(class_adapter);
		spn_main_class.setSelection(0);

		// 初始化学生列表
		studentListItems = new ArrayList<Student>();
		studentListAdapter = new ClassStudentListAdapter(getActivity(), studentListItems);
		gv_student_list.setAdapter(studentListAdapter);

		// 设置事件侦听
		btn_show_class_motion.setOnClickListener(this);
		spn_order.setOnItemSelectedListener(this);
		spn_main_class.setOnItemSelectedListener(this);

		// 这里的Handler实例将配合下面的Timer实例，完成定时更新学生运动数据的功能
		updateStudentListHandler = new UpdateStudentListHandler(
				Looper.getMainLooper());

		task = new TimerTask() {
			@Override
			public void run() {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"class_realdata\",\"DAT\":{\"cid\":"
						+ arr_classid[currentClassIndex] + "}}";
				// 开始发送请求消息
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService
						.setUpdateClientHandler(updateStudentListHandler);
			}
		};
	}

	// 与Service的连接对象
	private ServiceConnection mConnection = new ServiceConnection() {
		// 当前Activity与Service意外失去连接后(如Service终止)调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("MainActivity解除与Service绑定");
		}

		// 当前Activity与Service绑定后调用(onBind()后触发)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("MainActivity与Service绑定成功");
			// 连接服务端,并请求发送班级请求数据
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			} else {
				mBoundService.startSendMessage(sendMessage);
			}
			if(timer == null)
				timer = new Timer();
			timer.schedule(task, 1000, 5000);
			// 设置Service获得服务端返回消息后的消息处理器
			mBoundService.setUpdateClientHandler(updateStudentListHandler);
		}
	};

	/**
	 * 更新学生列表的Handler
	 */
	private class UpdateStudentListHandler extends Handler {
		public UpdateStudentListHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj != null) { // 更新学生列表
				returnMessage = msg.obj.toString();
				System.out.println("客户端接收到的消息:" + returnMessage);
				String res = JSONParser.getResponseString(returnMessage);
				if ("class_students".equals(res)) {
					curPeClass = JSONParser
							.getClassStudents(returnMessage);
					if (curPeClass != null) {
						curPeClass.setCid(arr_classid[currentClassIndex]);
						curPeClass.setCname(arr_className[currentClassIndex]);
						Student[] students = curPeClass.getStudents();
						studentListItems.clear();
						if (students != null) {
							int num = students.length;
							// 更新班级学生信息
							for (int i = 0; i < num; i++) {
								students[i].setPeClassid(arr_classid[currentClassIndex]);
								studentListItems.add(students[i]);
							}
						}
					}
				} else if ("class_realdata".equals(res)) {
					// 获得班级实时数据
					PeClass peClass = JSONParser
							.getClassStudentRealdata(returnMessage);
					Student[] students = peClass.getStudents();
					int len = students.length;
					// 清空历史数据
					classRealDataMap.clear();
					for (int i = 0; i < len; i++) {
						classRealDataMap.put(students[i].getSid(), students[i].getEnergyConsumption());
					}
					
					/** 更新运动数据 */
					if(peClass.getCid() == arr_classid[currentClassIndex]){
						for (int i = 0; i < studentListItems.size(); i++) {
							studentListItems.get(i).setEnergyConsumption(
									classRealDataMap.get(studentListItems.
											get(i).getSid()));
						}
					}
					// 通知更新界面
					Message message = new Message();
					message.obj = null;
					updateStudentListHandler.sendMessage(message);
				} else if("class_avatar".equals(res)){
					Bundle bundle = JSONParser.getClassStudentAvatarsUrl(returnMessage);
					final String prefix = bundle.getString("prefix");
					final int[] studentIds = bundle.getIntArray("sids");
					String[] imgs = bundle.getStringArray("images");
					
					final Map<Integer,String> stuAvaUrlsMap = new HashMap<Integer, String>();
					Editor editor = spAvatar.edit();
					for(int i = 0; i < studentIds.length; i++) {
						stuAvaUrlsMap.put(studentIds[i], imgs[i]);
						editor.putString("" + studentIds[i], imgs[i]);
					}
					editor.commit();
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(1500);
								startDownloadAvatar(prefix, studentIds, stuAvaUrlsMap);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			}
			// 对当前学生排序
			if (spn_order.getSelectedItemPosition() == 0) {
				Collections.sort(studentListItems,
						new StudentNumberComparator());
			} else {
				Collections.sort(studentListItems,
						new StudentEnergyComparator());
			}
			// 刷新学生信息显示
			studentListAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 点击按钮事件处理
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		/** 查看班级统计按钮事件处理 */
		if (id == R.id.btn_show_class_cur_motion) {
			mCallback.onClassSelected(curPeClass);
		}
	}

	/**
	 * 下拉框选择事件处理
	 */
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		int vid = adapterView.getId();
		/** 班级下拉框选择事件处理 */
		if (vid == R.id.spn_cur_class) {
			currentClassIndex = position;
			//设置标题
			MainActivity.tv_details_title.setText(arr_className[position] + "课堂锻炼运动能量消耗情况(单位: 卡路里/5秒)");
			// 获取选择的班级id
			int cid = arr_classid[position];
			sendMessage = "{\"REQ\":\"class_students\",\"DAT\":{\"cid\":" + cid
					+ "}}";
			System.out.println("message:" + sendMessage);
			if(mBoundService != null){
				// 开始发送请求消息
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService.setUpdateClientHandler(updateStudentListHandler);
			}
			
			// 获取头像的http路径
			sendMessage = "{\"REQ\":\"class_avatar\",\"DAT\":{\"cid\":" + cid
					+ "}}";
			System.out.println("message:" + sendMessage);
			if (mBoundService != null) {
				// 开始发送请求消息
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService.setUpdateClientHandler(updateStudentListHandler);
			}
		}
		/** 排序方式下拉框选择事件处理 */
		if (vid == R.id.spn_order) {
			// 按学号排序
			if (position == 0) {
				Collections.sort(studentListItems,
						new StudentNumberComparator());
				studentListAdapter.notifyDataSetChanged();
			}
			// 按运动量排序
			if (position == 1) {
				Collections.sort(studentListItems, new StudentEnergyComparator());
				studentListAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}

	private int getListIndexBySid(List<Student> list, int sid){
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			Student student = list.get(i);
			if(student.getSid() == sid){
				index = i;
				break;
			}
		}
		return index;
	}
	
	public void startDownloadAvatar(String prefix,int[] studentIds, Map<Integer, String> urlMap){
		// 先判断网络连接
		ConnectivityManager connMgr = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			for (int i = 0; i < studentIds.length; i++) {
				String fileName = urlMap.get(studentIds[i]);
				int studentIndex = getListIndexBySid(studentListItems, studentIds[i]);
				Bitmap bmLocal = null;
				try {
					Student stu = studentListItems.get(studentIndex);
					if (stu.getBitmapAvatar() == null) {
						bmLocal = DownloadImage.readImageFromExternal(directory+"/"+fileName);
						stu.setBitmapAvatar(bmLocal);
					}
				} catch (FileNotFoundException e) {
					String url = prefix + fileName;
					System.out.println("正在从"+url+"下载头像");
					Bitmap bm = null;
					try {
						bm = DownloadImage.downloadImage(url);
					} catch (IOException e1) {
						System.out
						.println("Unable to retrieve web page. URL may be invalid.");
					}
					System.out.println("已完成从"+url+"下载头像");
					if(bm != null){
						studentListItems.get(studentIndex).setBitmapAvatar(bm);
						if (DownloadImage.isExternalStorageWritable()) {
							try {
								DownloadImage.saveBitmapToExternal(bm, directory, fileName);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				/*if(bmLocal != null){
					//DownloadImage.scaleCompress(bmLocal);
					studentListItems.get(i).setAvatar(bmLocal);
				} else {
					String url = prefix + fileName;
					System.out.println("正在从"+url+"下载头像");
					Bitmap bm = DownloadImage.downloadImage(url);
					System.out.println("已完成从"+url+"下载头像");
					if(bm != null){
						studentListItems.get(i).setAvatar(bm);
						if (DownloadImage.isExternalStorageWritable()) {
							DownloadImage.saveBitmapToExternal(bm, directory, fileName);
						}
					}
				}*/
			}
			Message message = myHttpHandler.obtainMessage();
			message.what = DOWNLOAD_OK;
			message.sendToTarget();
		} else {
			Toast.makeText(getActivity(), "No network connection available.", Toast.LENGTH_LONG);
		}
	}
	
	class MyHttpHandler extends Handler {
		public MyHttpHandler(){
			
		}
		public MyHttpHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			if (what == DOWNLOAD_OK) {
				studentListAdapter.notifyDataSetChanged();
			}
		}
	}
	
}
