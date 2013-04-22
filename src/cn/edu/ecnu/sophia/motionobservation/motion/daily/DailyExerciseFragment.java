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

	// ��Service�󶨵�״̬
	private boolean mIsBound = false;
	// �󶨵�Service
	private SocketService mBoundService;

	private Handler updateStudentListHandler;

	// �ؼ�
	private Spinner spn_class_name, spn_class_type;
	private Button btn_show_class_motion;
	private ArrayAdapter<String> class_adapter, type_adapter;

	// �༶id�Ͱ༶��������
	private static int[] arr_classid;
	private static String[] arr_className;
	// �༶����
	private static String[] arr_class_type = { "������", "������" };

	// ����ѧ����Ϣ��List,GridView����Adapter
	private List<Student> studentListItems;
	private DailyStudentListAdapter studentListAdapter;
	private GridView gv_student_list;

	// ���͵ĵ�¼������Ϣ
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";

	// ����˷��صĵ�¼�����Ϣ
	private String returnMessage = "";

	// ��ǰ�༶�ڰ༶�б��е�����ֵ
	private int currentClassIndex = 0;

	// ��ǰ�༶
	private PeClass curPeClass;
	
	// ��ǰ��ʦ�����༶
	private PeClass[] peClasses;
	
	// ��¼�Ľ�ʦ
	private Teacher teacher;
	
	// �洢ͷ��
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
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("DailyExerciseFragment�����Service�İ�");
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("DailyExerciseFragment onStop");
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("DailyExerciseFragment�����Service�İ�");
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
		// �ȴ�Service, Ȼ�󽫵�ǰActivity���Service��
		// startService(intent);
		if(mBoundService == null){
			System.out.println("��MainActivity�󶨵�����");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("��ɽ�MainActivity�󶨵�����");
		}
		
		/** ��ʼ������ؼ� */
		// ʵ�����ؼ�
		spn_class_type = (Spinner) getActivity().findViewById(R.id.spn_class_type);
		spn_class_name = (Spinner) getActivity().findViewById(R.id.spn_class_name);
		btn_show_class_motion = (Button) getActivity().findViewById(R.id.btn_show_class_daily_motion);
		gv_student_list = (GridView) getActivity().findViewById(R.id.gv_student_list);

		// ��ʼ������������
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

		// ��ʼ��ѧ���б�
		studentListItems = new ArrayList<Student>();
		studentListAdapter = new DailyStudentListAdapter(getActivity(), studentListItems);
		gv_student_list.setAdapter(studentListAdapter);

		// �����¼�����
		//btn_enter_remind.setOnClickListener(this);
		btn_show_class_motion.setOnClickListener(this);
		spn_class_name.setOnItemSelectedListener(this);

		// �����Handlerʵ������������Timerʵ������ɶ�ʱ����ѧ���˶����ݵĹ���
		updateStudentListHandler = new UpdateStudentListHandler(
				Looper.getMainLooper());
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

	/**
	 * ����ѧ���б��Handler
	 */
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
						studentListItems.clear();
						if (students != null) {
							int num = students.length;
							// ���°༶ѧ����Ϣ
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
			// ˢ��ѧ����Ϣ��ʾ
			studentListAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * �����ť�¼�����
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		/** �鿴�༶ͳ�ư�ť�¼����� */
		if (id == R.id.btn_show_class_daily_motion) {
			/*Intent intent = new Intent(getActivity(), ClassMotionActivity.class);
			intent.putExtra("curClass", peClasses[currentClassIndex]);
			startActivity(intent);*/
			mCallback.onDailyClassSelected(curPeClass);
		}
	}

	/**
	 * ������ѡ���¼�����
	 */
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		int vid = adapterView.getId();
		/** �༶������ѡ���¼����� */
		if (vid == R.id.spn_class_name) {
			currentClassIndex = position;
			//���ñ���
			MainActivity.tv_details_title.setText(arr_className[position] + "�ճ������˶���������ͳ�����");
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
			
			// ��ȡͷ���http·��
			sendMessage = "{\"REQ\":\"class_avatar\",\"DAT\":{\"cid\":" + cid
					+ "}}";
			System.out.println("message:" + sendMessage);
			if (mBoundService != null) {
				// ��ʼ����������Ϣ
				mBoundService.startSendMessage(sendMessage);
				// ����Service��÷���˷�����Ϣ�����Ϣ������
				mBoundService.setUpdateClientHandler(updateStudentListHandler);
			}
		}
		/** ����ʽ������ѡ���¼����� */
		if (vid == R.id.spn_order) {
			// ��ѧ������
			if (position == 0) {
				Collections.sort(studentListItems,
						new StudentNumberComparator());
				studentListAdapter.notifyDataSetChanged();
			}
			// ���˶�������
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
					System.out.println("���ڴ�"+url+"����ͷ��");
					Bitmap bm = DownloadImage.downloadImage(url);
					System.out.println("����ɴ�"+url+"����ͷ��");
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
