package cn.edu.ecnu.sophia.motionobservation.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;
import cn.edu.ecnu.sophia.motionobservation.model.Announcement;
import cn.edu.ecnu.sophia.motionobservation.model.MyListView;
import cn.edu.ecnu.sophia.motionobservation.model.MyListView.OnRefreshListener;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;

public class AnnouncementFragment extends Fragment {
	
	private Context context;
	
	// �ؼ�
	//private Button btn_delete_announcement;
	//private Button btn_new_announcement;
	private MyListView list_announcement;
	private TextView tv_list_empty;

	// ��������
	private List<Announcement> listItems;
	private AnnouncementListAdapter listadapter;

	// �󶨵�Service
	private SocketService mBoundService;
	// ��Service�󶨵�״̬
	private boolean mIsBound = false;

	// ������Ϣ
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	// ����˷��صĵ�¼�����Ϣ
	private String returnMessage = "";

	static Handler announcementHandler;
	private String[] contentArr;

	// ��ǰ��¼�Ľ�ʦ
	private Teacher teacher;
	
	//����ӿ�
	public static OnAnnouncementListener mCallback;
	public static interface OnAnnouncementListener {
		public void onAnnoucementSelected(Teacher teacher,String[] contentArr);
		public void onPersonalAnnoucementSelected(Announcement announcement);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("teacher", teacher);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnAnnouncementListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement OnAnnouncementListener");
		}
	}

	/*@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("AnnouncementFragment OnDestroy");
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("AnnouncementFragment�����Service�İ�");
		}
	}*/
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("AnnouncementFragment OnStop");
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("AnnouncementFragment�����Service�İ�");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.announcement, container, false);
	}
	
	final Handler refreshListViewHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			listadapter.notifyDataSetChanged();
			list_announcement.onRefreshComplete();
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
		
		// ��ȡ��ǰ��ʦ
		Bundle bundle = getArguments();
		if (bundle != null) {
			teacher = (Teacher) bundle.getSerializable("teacher");
		}
		
		if (savedInstanceState != null) {
			teacher = (Teacher) savedInstanceState.getSerializable("teacher");
		}
		
		// ��ȡ�����еĿؼ�
		//btn_delete_announcement = (Button) getActivity().findViewById(R.id.btn_delete_announcement);
		//btn_new_announcement = (Button) getActivity().findViewById(R.id.btn_new_announcement);
		list_announcement = (MyListView) getActivity().findViewById(R.id.list_announcement);
		tv_list_empty = (TextView) getActivity().findViewById(R.id.tv_announcement_list_empty);

		// ��ȡ������Ϣ
		listItems = new ArrayList<Announcement>();
		// ����������
		listadapter = new AnnouncementListAdapter(context, listItems);
		list_announcement.setCacheColorHint(Color.TRANSPARENT);
		// Ϊ�б�����������
		list_announcement.setAdapter(listadapter);
		// Ϊ�б���������Ϊ��ʱ����ʾ
		list_announcement.setEmptyView(tv_list_empty);
		
		list_announcement.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				Announcement announcement = new Announcement();
				announcement.setTitle("test refresh");
				announcement.setContent("test Content");
				announcement.setPublisher("test");
				announcement.setPublishTime(new Date().toLocaleString());
				listItems.add(0, announcement);
				refreshListViewHandler.obtainMessage();
				refreshListViewHandler.sendEmptyMessage(0);
			}
		});

		// ��Ӱ�ť�������¼�
		//btn_delete_announcement.setOnClickListener(this);
		//btn_new_announcement.setOnClickListener(this);

		announcementHandler = new AnnouncementHandler(getActivity().getMainLooper());

		sendMessage = "{\"REQ\":\"getAnnouncement\",\"DAT\":{\"offset\":0,\"amount\":10}}";

		// �󶨷���
		Intent intent = new Intent(getActivity(),
				SocketService.class);
		// �ȴ�Service, Ȼ�󽫵�ǰActivity���Service��
		if (mBoundService == null) {
			System.out.println("��AnnouncementFragment�󶨵�����");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("��ɽ�AnnouncementFragment�󶨵�����");
		} else {
			mBoundService.startSendMessage(sendMessage);
			// ����Service��÷���˷�����Ϣ�����Ϣ������
			mBoundService.setUpdateClientHandler(announcementHandler);
		}
	}
	
	// ��Service�����Ӷ���
	private ServiceConnection mConnection = new ServiceConnection() {
		// ��ǰActivity��Service����ʧȥ���Ӻ�(��Service��ֹ)����
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("AnnouncementFragment�����Service��");
		}

		// ��ǰActivity��Service�󶨺����(onBind()�󴥷�)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("AnnouncementFragment��Service�󶨳ɹ�");
			// ���ӷ����,�������Ͱ༶��������
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			} else {
				mBoundService.startSendMessage(sendMessage);
			}
			// ����Service��÷���˷�����Ϣ�����Ϣ������
			mBoundService.setUpdateClientHandler(announcementHandler);
		}
	};
	

	/**
	 * ���¹����б��Handler
	 */
	private class AnnouncementHandler extends Handler {
		public AnnouncementHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj != null) {
				returnMessage = msg.obj.toString();
				System.out.println("�ͻ��˽��յ�����Ϣ:" + returnMessage);
				String res = JSONParser.getResponseString(returnMessage);
				if ("getAnnouncement".equals(res)) {
					Announcement[] announcements = JSONParser
							.getAnnouncement(returnMessage);
					if (announcements != null) {
						contentArr = new String[announcements.length];
						updateAnnouncementListItems(announcements);
					}
				} else if ("deleteAnnouncement".equals(res)) {
					int count = JSONParser.getDeleteAnnouncementCount(returnMessage);
					Toast.makeText(getActivity(),
							"ɾ����" + count + "������", Toast.LENGTH_SHORT)
							.show();
				}
			}
			listadapter.notifyDataSetChanged();
		}
	}

	/**
	 * ���ع�����Ϣ�б�
	 * 
	 * @return listItems ������Ϣ�б�
	 */
	private void updateAnnouncementListItems(Announcement[] announcements) {
		listItems.clear();
		for (int i = 0; i < announcements.length; i++) {
			listItems.add(announcements[i]);
			contentArr[i] = announcements[i].getContent();
		}
	}

	/**
	 * ɾ��ѡ��Ĺ���
	 * 
	 * @return ɾ���Ĺ�����Ŀ
	 */
	private void deleteAnnouncement() {
		// ����ܹ������Ŀ
		int size = listItems.size();
		ArrayList<String> newsIdString = new ArrayList<String>();
		// ����һ��Ҫ�Ӻ���ǰɾ������Ϊÿɾ��һ���б��ListView�ͻ����¶��б����ţ�
		for (int i = size - 1; i >= 0; i--) {
			if (listadapter.hasChecked(i)) {
				newsIdString.add("" + listItems.get(i).getNewsId());
				listItems.remove(i);
			}
		}
		listadapter.notifyDataSetChanged();
		
		String idString = "[";
		int len = newsIdString.size();
		for (int j = 0; j < len; j++) {
			if(j<len-1){
				idString += newsIdString.get(j) + ",";
			} else {
				idString += newsIdString.get(j) + "]";
			}
		}
		// �ӷ����ɾ��
		sendMessage = "{\"REQ\":\"deleteAnnouncement\",\"DAT\":{\"news_id\":"
				+ idString + "}}";

		// ��ʼ������Ϣ
		mBoundService.startSendMessage(sendMessage);
		// ����Service��÷���˷�����Ϣ�����Ϣ������
		mBoundService.setUpdateClientHandler(announcementHandler);
		
	}

	/**
	 * ȷ��ɾ������
	 */
	private void deleteAnnouncementDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle("ȷ��ɾ������")
				.setMessage("�Ƿ�Ҫɾ��ѡ�еĹ���?")
				.setPositiveButton("��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						deleteAnnouncement();
					}
				})
				.setNegativeButton("��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Toast.makeText(getActivity(), "ȡ����ɾ������",
								Toast.LENGTH_SHORT).show();
					}
				}).show();
	}
	
	/*@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_delete_announcement:
			deleteAnnouncementDialog();
			break;
		case R.id.btn_new_announcement:
			mCallback.onAnnoucementSelected(teacher,contentArr);
			break;
		}
	}*/
}