package cn.edu.ecnu.sophia.motionobservation.motion.daily;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.R.id;
import cn.edu.ecnu.sophia.motionobservation.R.layout;
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

public class DailyExerciseFragment extends Fragment implements OnClickListener,
		 OnItemSelectedListener {

	// 与Service绑定的状态
	private boolean mIsBound = false;
	// 绑定的Service
	private SocketService mBoundService;

	private Handler updateStudentListHandler;

	// 控件
	private Spinner spn_class_name, spn_class_type;
	private Button btn_show_class_motion;
	private ArrayAdapter<String> class_adapter, type_adapter;

	// 班级id和班级名称数组
	private static int[] arr_classid;
	private static String[] arr_className;
	// 班级类型
	private static String[] arr_class_type = { "行政班", "体育班" };

	// 包含学生信息的List,GridView及其Adapter
	private List<Student> studentListItems;
	private DailyStudentListAdapter studentListAdapter;
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
	
	// 存储头像
	private static String directory;
	private Handler myHttpHandler;
	private final static int DOWNLOAD_OK = 0;

	public static OnDailyShowMotionListener mCallback;
	
	public interface OnDailyShowMotionListener {
		public void onDailyClassSelected(PeClass curPeClass);
		public void onDailyStudentClicked(Student student);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnDailyShowMotionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement OnShowClassMotionListener");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("DailyExerciseFragment OnDestroy");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("DailyExerciseFragment解除与Service的绑定");
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("DailyExerciseFragment onStop");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("DailyExerciseFragment解除与Service的绑定");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.daily_exercise_main, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		MainActivity.setIsHome(true);
		getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).mkdirs();
		directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		myHttpHandler = new MyHttpHandler(Looper.getMainLooper());
		
		Intent in = getActivity().getIntent();
		teacher = (Teacher) in.getSerializableExtra("loginTeacher");
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
		// startService(intent);
		if(mBoundService == null){
			System.out.println("将MainActivity绑定到服务");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("完成将MainActivity绑定到服务");
		}
		
		/** 初始化界面控件 */
		// 实例化控件
		spn_class_type = (Spinner) getActivity().findViewById(R.id.spn_class_type);
		spn_class_name = (Spinner) getActivity().findViewById(R.id.spn_class_name);
		btn_show_class_motion = (Button) getActivity().findViewById(R.id.btn_show_class_daily_motion);
		gv_student_list = (GridView) getActivity().findViewById(R.id.gv_student_list);

		// 初始化两个下拉框
		type_adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arr_class_type);
		type_adapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spn_class_type.setAdapter(type_adapter);
		spn_class_type.setSelection(1);
		
		class_adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arr_className);
		class_adapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spn_class_name.setAdapter(class_adapter);
		spn_class_name.setSelection(0);

		// 初始化学生列表
		studentListItems = new ArrayList<Student>();
		studentListAdapter = new DailyStudentListAdapter(getActivity(), studentListItems);
		gv_student_list.setAdapter(studentListAdapter);

		// 设置事件侦听
		//btn_enter_remind.setOnClickListener(this);
		btn_show_class_motion.setOnClickListener(this);
		spn_class_name.setOnItemSelectedListener(this);

		// 这里的Handler实例将配合下面的Timer实例，完成定时更新学生运动数据的功能
		updateStudentListHandler = new UpdateStudentListHandler(
				Looper.getMainLooper());
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
				} else if("class_avatar".equals(res)){
					Bundle bundle = JSONParser.getClassStudentAvatarsUrl(returnMessage);
					final String prefix = bundle.getString("prefix");
					final int[] studentIds = bundle.getIntArray("sids");
					String[] imgs = bundle.getStringArray("images");
					
					final Map<Integer,String> stuAvaUrlsMap = new HashMap<Integer, String>();
					for(int i = 0; i < studentIds.length; i++) {
						stuAvaUrlsMap.put(studentIds[i], imgs[i]);
					}
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(1500);
								startDownloadAvatar(prefix, studentIds, stuAvaUrlsMap);
							} catch (IOException e) {
								System.out
								.println("Unable to retrieve web page. URL may be invalid.");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
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
		if (id == R.id.btn_show_class_daily_motion) {
			/*Intent intent = new Intent(getActivity(), ClassMotionActivity.class);
			intent.putExtra("curClass", peClasses[currentClassIndex]);
			startActivity(intent);*/
			mCallback.onDailyClassSelected(curPeClass);
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
		if (vid == R.id.spn_class_name) {
			currentClassIndex = position;
			//设置标题
			MainActivity.tv_details_title.setText(arr_className[position] + "日常锻炼运动能量消耗统计情况");
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

	public void startDownloadAvatar(String prefix,int[] studentIds, Map<Integer, String> urlMap) throws IOException{
		
		ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			for (int i = 0; i < studentIds.length; i++) {
				String fileName = urlMap.get(studentIds[i]);
				Bitmap bmLocal = DownloadImage.readImageFromExternal(directory+"/"+fileName);
				if(bmLocal != null){
					studentListItems.get(i).setBitmapAvatar(bmLocal);
				} else {
					String url = prefix + fileName;
					System.out.println("正在从"+url+"下载头像");
					Bitmap bm = DownloadImage.downloadImage(url);
					System.out.println("已完成从"+url+"下载头像");
					if(bm != null){
						studentListItems.get(i).setBitmapAvatar(bm);
						if (DownloadImage.isExternalStorageWritable()) {
							DownloadImage.saveBitmapToExternal(bm, directory, fileName);
						}
					}
				}
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
