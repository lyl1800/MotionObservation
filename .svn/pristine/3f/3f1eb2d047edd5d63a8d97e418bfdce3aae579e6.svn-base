package cn.edu.ecnu.sophia.motionobservation.motion.classes;

import java.util.ArrayList;
import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;
import cn.edu.ecnu.sophia.motionobservation.model.PeClass;
import cn.edu.ecnu.sophia.motionobservation.model.Student;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class CheckAttendanceFragment extends Fragment implements OnItemSelectedListener {
	private Spinner spn_class_name;
	private ArrayAdapter<String> name_list;
	private Button btnDetect;
	private Button btnStart;

	//出勤和缺勤学生列表
	private List<Student> readyStudentListItems = new ArrayList<Student>();
	private List<Student> notReadyStudentListItems = new ArrayList<Student>();
	private StudentNameListAdapter readyStudentListAdapter, notReadyStudentListAdapter;
	
	private PeClass[] peClasses;
	// 班级id和班级名称数组
	private static int[] arr_classid;
	private static String[] arr_className;
	// 发送的登录请求信息
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	
	// 与Service绑定的状态
	private boolean mIsBound = false;
	// 绑定的Service
	private SocketService mBoundService;

	// 用于更新学生列表的Handler
	private Handler updateStudentListHandler;

	// 服务端返回的登录结果消息
	private String returnMessage = "";

	// 当前班级在班级列表中的索引值
	private int currentClassIndex = 0;

	// 当前班级
	private PeClass curPeClass;
	
	public static OnCheckAttendanceListener mCallback;
	
	public interface OnCheckAttendanceListener {
		public void onStartExercise(Teacher teacher);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnCheckAttendanceListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement OnCheckAttendanceListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		    return inflater.inflate(R.layout.check_attendance_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		MainActivity.tv_details_title.setText("出勤检测");
		// 这里的Handler实例将配合下面的Timer实例，完成定时更新学生运动数据的功能
		updateStudentListHandler = new UpdateStudentListHandler(
				Looper.getMainLooper());
		Bundle bundle = getArguments();
		final Teacher teacher = (Teacher) bundle.getSerializable("teacher");

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
		if (mBoundService == null || !mIsBound) {
			System.out.println("将ClassExerciseFragment绑定到服务");
			getActivity().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);
			System.out.println("完成将ClassExerciseFragment绑定到服务");
		}

		// 实现listview
		ListView lv1 = (ListView) getActivity().findViewById(R.id.yes);
		lv1.setEmptyView(getActivity().findViewById(R.id.tv_ready_empty_hint));
		readyStudentListAdapter = new StudentNameListAdapter(getActivity(),
				readyStudentListItems);
		lv1.setAdapter(readyStudentListAdapter);

		ListView lv2 = (ListView) getActivity().findViewById(R.id.no);
		lv2.setEmptyView(getActivity()
				.findViewById(R.id.tv_notready_empty_hint));
		notReadyStudentListAdapter = new StudentNameListAdapter(getActivity(),
				notReadyStudentListItems);
		lv2.setAdapter(notReadyStudentListAdapter);

		spn_class_name = (Spinner) getActivity()
				.findViewById(R.id.choose_class);
		// 将可选内容与ArrayAdapter连接起来
		name_list = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arr_className);
		// 设置下拉列表的风格
		name_list
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// 将adapter2 添加到spinner中
		spn_class_name.setAdapter(name_list);
		// 添加事件监听 有改动。。。
		// spinner2.setOnItemClickListener((OnItemClickListener) new
		// SpinnerXMLSelectedListener());
		// 设置默认值
		spn_class_name.setVisibility(View.VISIBLE);
		// 设置事件
		spn_class_name.setOnItemSelectedListener(this);

		btnDetect = (Button) getActivity().findViewById(R.id.btn_detect);

		btnDetect.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeAttendance();
				readyStudentListAdapter.notifyDataSetChanged();
				notReadyStudentListAdapter.notifyDataSetChanged();
			}
			
			void changeAttendance(){
				int notReadyCount = (int) Math.round((notReadyStudentListItems.size()*0.1));
				// 两者互换
				readyStudentListItems.addAll(notReadyStudentListItems);
				notReadyStudentListItems.clear();
				for (int i = 0; i < notReadyCount; i++) {
					int index = (int) (Math.random()*readyStudentListItems.size());
					Student student = readyStudentListItems.get(index);
					notReadyStudentListItems.add(student);
					readyStudentListItems.remove(index);
				}
			}
		});

		btnStart = (Button) getActivity().findViewById(R.id.btn_start);
		// 设置监听
		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onStartExercise(teacher);
			}
		});
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
							readyStudentListItems.clear();
							notReadyStudentListItems.clear();
							if (students != null) {
								int num = students.length;
								// 更新班级学生信息
								for (int i = 0; i < num; i++) {
									students[i].setPeClassid(arr_classid[currentClassIndex]);
									notReadyStudentListItems.add(students[i]);
								}
							}
						}
						readyStudentListAdapter.notifyDataSetChanged();
						notReadyStudentListAdapter.notifyDataSetChanged();
					}
				}
			}
		}
		
	
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,int position, long id) {
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
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	
}