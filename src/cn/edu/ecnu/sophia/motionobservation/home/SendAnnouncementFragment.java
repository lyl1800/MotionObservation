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
	private String[] content_remain = { "1. 请抓紧时间完成目标运动量",
			"2. 最近一段时间你的运动量都未达标，请速来办公室见我", "3. 请多加强体育锻炼" };
	private ArrayAdapter<String> myArrayAdapter;

	// 绑定的Service
	private SocketService mBoundService;
	// 与Service绑定的状态
	private boolean mIsBound = false;

	// 发送信息
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	// 服务端返回的登录结果消息
	private String returnMessage = "";

	private Handler addAnnouncementHandler;

	private Teacher teacher;

	@Override
	public void onDestroy() {
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("SendAnnouncementActivity解除与Service的绑定");
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

		MainActivity.tv_details_title.setText("发送公告");
		
		addAnnouncementHandler = new SendAnnouncementHandler(getActivity().getMainLooper());

		// 编辑框
		txt_announcementEdite = (EditText) getActivity().findViewById(R.id.txt_announcementEdite);

		// 发送公告
		btn_sendAnnouncement = (Button) getActivity().findViewById(R.id.btn_sendAnnouncement);
		btn_sendAnnouncement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 显示物品详情
				String st = String.valueOf(listItems.size() + 1) + "."
						+ txt_announcementEdite.getText().toString(); // 提取编辑框里面的信息
				listItems.add(st);
				myArrayAdapter.notifyDataSetChanged();

				sendMessage = "{\"REQ\":\"saveNewAnnouncement\",\"DAT\":{\"tid\":"
						+ teacher.getTid()
						+ ",\"title\":\""
						+ new String("锻炼通知")
						+ "\",\"type\":1,\"content\":\""
						+ txt_announcementEdite.getText().toString()
						+ "\"}}";
				
				// 开始发送消息
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService.setUpdateClientHandler(addAnnouncementHandler);
				
			}
		});
		// ------------------------------------------------------------------------------------------

		// 设置adpter------------------------------------------------------------------------------------
		list_historialannouncement = (ListView) getActivity().findViewById(R.id.list_historialannouncement);
		for (int i = 0; i < content_remain.length; i++) {
			listItems.add((i + 1) + "." + content_remain[i]);
		}

		// 以保存提醒 的 adapter
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

		// 绑定服务
		Intent intent = new Intent(getActivity(),
				SocketService.class);
		// 先打开Service, 然后将当前Activity与该Service绑定
		// startService(intent);
		if (mBoundService == null) {
			System.out.println("将SendAnnouncementActivity绑定到服务");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("完成将SendAnnouncementActivity绑定到服务");
		}
	}

	// 与Service的连接对象
	private ServiceConnection mConnection = new ServiceConnection() {
		// 当前Activity与Service意外失去连接后(如Service终止)调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("SendAnnouncementActivity解除与Service绑定");
		}

		// 当前Activity与Service绑定后调用(onBind()后触发)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("SendAnnouncementActivity与Service绑定成功");
			// 连接服务端,并请求发送班级请求数据
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}
			// 设置Service获得服务端返回消息后的消息处理器
			mBoundService.setUpdateClientHandler(addAnnouncementHandler);
		}
	};

	/**
	 * 更新公告列表的Handler
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
				System.out.println("客户端接收到的消息:" + returnMessage);
				String res = JSONParser.getResponseString(returnMessage);
				if ("saveNewAnnouncement".equals(res)) {
					boolean flag = JSONParser.isNewAnnouncementSaved(returnMessage);
					if(flag){
						Toast.makeText(getActivity().getApplicationContext(), "新公告发布成功",
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
