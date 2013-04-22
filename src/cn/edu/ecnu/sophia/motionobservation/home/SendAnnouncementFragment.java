package cn.edu.ecnu.sophia.motionobservation.home;

import android.content.ComponentName;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;

public class SendAnnouncementFragment extends Fragment {

	private Button btn_sendAnnouncement = null;
	private ListView list_historialannouncement;
	private ArrayList<String> listItems = new ArrayList<String>();
	private EditText txt_announcementEdite;
	private String[] content_remain = { "1. ��ץ��ʱ�����Ŀ���˶���",
			"2. ���һ��ʱ������˶�����δ��꣬�������칫�Ҽ���", "3. ����ǿ��������" };
	private ArrayAdapter<String> myArrayAdapter;

	// �󶨵�Service
	private SocketService mBoundService;
	// ��Service�󶨵�״̬
	private boolean mIsBound = false;

	// ������Ϣ
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	// ����˷��صĵ�¼�����Ϣ
	private String returnMessage = "";

	private Handler addAnnouncementHandler;

	private Teacher teacher;

	@Override
	public void onDestroy() {
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("SendAnnouncementActivity�����Service�İ�");
		}
		super.onDestroy();
		System.out.println("SendAnnouncementActivity OnDestroy");
	}

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.send_announcement, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		if (bundle != null) {
			content_remain = bundle.getStringArray("contentArr");
			teacher = (Teacher) bundle.getSerializable("teacher");
		}

		MainActivity.tv_details_title.setText("���͹���");
		
		addAnnouncementHandler = new SendAnnouncementHandler(getActivity().getMainLooper());

		// �༭��
		txt_announcementEdite = (EditText) getActivity().findViewById(R.id.txt_announcementEdite);

		// ���͹���
		btn_sendAnnouncement = (Button) getActivity().findViewById(R.id.btn_sendAnnouncement);
		btn_sendAnnouncement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// ��ʾ��Ʒ����
				String st = String.valueOf(listItems.size() + 1) + "."
						+ txt_announcementEdite.getText().toString(); // ��ȡ�༭���������Ϣ
				listItems.add(st);
				myArrayAdapter.notifyDataSetChanged();

				sendMessage = "{\"REQ\":\"saveNewAnnouncement\",\"DAT\":{\"tid\":"
						+ teacher.getTid()
						+ ",\"title\":\""
						+ new String("����֪ͨ")
						+ "\",\"type\":1,\"content\":\""
						+ txt_announcementEdite.getText().toString()
						+ "\"}}";
				
				// ��ʼ������Ϣ
				mBoundService.startSendMessage(sendMessage);
				// ����Service��÷���˷�����Ϣ�����Ϣ������
				mBoundService.setUpdateClientHandler(addAnnouncementHandler);
				
			}
		});
		// ------------------------------------------------------------------------------------------

		// ����adpter------------------------------------------------------------------------------------
		list_historialannouncement = (ListView) getActivity().findViewById(R.id.list_historialannouncement);
		for (int i = 0; i < content_remain.length; i++) {
			listItems.add((i + 1) + "." + content_remain[i]);
		}

		// �Ա������� �� adapter
		myArrayAdapter = new MyArrayAdapter(getActivity(),
				android.R.layout.simple_list_item_1, listItems);
		
		list_historialannouncement.setAdapter(myArrayAdapter);

		list_historialannouncement.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				String str = listItems.get(position).substring(2);
				txt_announcementEdite.setText(str);
			}
		});

		// �󶨷���
		Intent intent = new Intent(getActivity(),
				SocketService.class);
		// �ȴ�Service, Ȼ�󽫵�ǰActivity���Service��
		// startService(intent);
		if (mBoundService == null) {
			System.out.println("��SendAnnouncementActivity�󶨵�����");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("��ɽ�SendAnnouncementActivity�󶨵�����");
		}
	}

	// ��Service�����Ӷ���
	private ServiceConnection mConnection = new ServiceConnection() {
		// ��ǰActivity��Service����ʧȥ���Ӻ�(��Service��ֹ)����
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("SendAnnouncementActivity�����Service��");
		}

		// ��ǰActivity��Service�󶨺����(onBind()�󴥷�)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("SendAnnouncementActivity��Service�󶨳ɹ�");
			// ���ӷ����,�������Ͱ༶��������
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}
			// ����Service��÷���˷�����Ϣ�����Ϣ������
			mBoundService.setUpdateClientHandler(addAnnouncementHandler);
		}
	};

	/**
	 * ���¹����б��Handler
	 */
	private class SendAnnouncementHandler extends Handler {
		public SendAnnouncementHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj != null) {
				returnMessage = msg.obj.toString();
				System.out.println("�ͻ��˽��յ�����Ϣ:" + returnMessage);
				String res = JSONParser.getResponseString(returnMessage);
				if ("saveNewAnnouncement".equals(res)) {
					boolean flag = JSONParser.isNewAnnouncementSaved(returnMessage);
					if(flag){
						Toast.makeText(getActivity().getApplicationContext(), "�¹��淢���ɹ�",
								Toast.LENGTH_LONG).show();
						sendMessage = "{\"REQ\":\"getAnnouncement\",\"DAT\":{\"offset\":0,\"amount\":10}}";
						mBoundService.startSendMessage(sendMessage);
						mBoundService.setUpdateClientHandler(AnnouncementFragment.announcementHandler);
						getFragmentManager().popBackStack();
					}
				}
			}
		}
	}
	
	private class MyArrayAdapter extends ArrayAdapter<String>{
		Context context;
		ArrayList<String> items = new ArrayList<String>();
		
		public MyArrayAdapter(final Context context,
				final int textViewResourceId, final ArrayList<String> objects){
			super(context, textViewResourceId, objects);
			this.context = context;
			this.items = objects;
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						android.R.layout.simple_list_item_1, parent, false);
			}
			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			tv.setText(items.get(position));
			tv.setTextColor(Color.rgb(25, 25, 112));
			tv.setTextSize(24);
			return convertView;
		}
	}
}
