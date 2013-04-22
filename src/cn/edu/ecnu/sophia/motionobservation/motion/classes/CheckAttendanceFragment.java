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

	//���ں�ȱ��ѧ���б�
	private List<Student> readyStudentListItems = new ArrayList<Student>();
	private List<Student> notReadyStudentListItems = new ArrayList<Student>();
	private StudentNameListAdapter readyStudentListAdapter, notReadyStudentListAdapter;
	
	private PeClass[] peClasses;
	// �༶id�Ͱ༶��������
	private static int[] arr_classid;
	private static String[] arr_className;
	// ���͵ĵ�¼������Ϣ
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	
	// ��Service�󶨵�״̬
	private boolean mIsBound = false;
	// �󶨵�Service
	private SocketService mBoundService;

	// ���ڸ���ѧ���б��Handler
	private Handler updateStudentListHandler;

	// ����˷��صĵ�¼�����Ϣ
	private String returnMessage = "";

	// ��ǰ�༶�ڰ༶�б��е�����ֵ
	private int currentClassIndex = 0;

	// ��ǰ�༶
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

		MainActivity.tv_details_title.setText("���ڼ��");
		// �����Handlerʵ������������Timerʵ������ɶ�ʱ����ѧ���˶����ݵĹ���
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
		// �ȴ�Service, Ȼ�󽫵�ǰActivity���Service��
		if (mBoundService == null || !mIsBound) {
			System.out.println("��ClassExerciseFragment�󶨵�����");
			getActivity().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);
			System.out.println("��ɽ�ClassExerciseFragment�󶨵�����");
		}

		// ʵ��listview
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
		// ����ѡ������ArrayAdapter��������
		name_list = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arr_className);
		// ���������б�ķ��
		name_list
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// ��adapter2 ��ӵ�spinner��
		spn_class_name.setAdapter(name_list);
		// ����¼����� �иĶ�������
		// spinner2.setOnItemClickListener((OnItemClickListener) new
		// SpinnerXMLSelectedListener());
		// ����Ĭ��ֵ
		spn_class_name.setVisibility(View.VISIBLE);
		// �����¼�
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
				// ���߻���
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
		// ���ü���
		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onStartExercise(teacher);
			}
		});
	}
	
	// ��Service�����Ӷ���
	private ServiceConnection mConnection = new ServiceConnection() {
		// ��ǰActivity��Service����ʧȥ���Ӻ�(��Service��ֹ)����
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("MainActivity�����Service��");
		}

		// ��ǰActivity��Service�󶨺����(onBind()�󴥷�)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("MainActivity��Service�󶨳ɹ�");
			// ���ӷ����,�������Ͱ༶��������
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			} else {
				mBoundService.startSendMessage(sendMessage);
			}
			// ����Service��÷���˷�����Ϣ�����Ϣ������
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
				if (msg.obj != null) { // ����ѧ���б�
					returnMessage = msg.obj.toString();
					System.out.println("�ͻ��˽��յ�����Ϣ:" + returnMessage);
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
								// ���°༶ѧ����Ϣ
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
		//���ñ���
		MainActivity.tv_details_title.setText(arr_className[position] + "���ö����˶������������(��λ: ��·��/5��)");
		// ��ȡѡ��İ༶id
		int cid = arr_classid[position];
		sendMessage = "{\"REQ\":\"class_students\",\"DAT\":{\"cid\":" + cid
				+ "}}";
		System.out.println("message:" + sendMessage);
		if(mBoundService != null){
			// ��ʼ����������Ϣ
			mBoundService.startSendMessage(sendMessage);
			// ����Service��÷���˷�����Ϣ�����Ϣ������
			mBoundService.setUpdateClientHandler(updateStudentListHandler);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	
}