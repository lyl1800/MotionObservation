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
	
	// 控件
	//private Button btn_delete_announcement;
	//private Button btn_new_announcement;
	private MyListView list_announcement;
	private TextView tv_list_empty;

	// 公告数据
	private List<Announcement> listItems;
	private AnnouncementListAdapter listadapter;

	// 绑定的Service
	private SocketService mBoundService;
	// 与Service绑定的状态
	private boolean mIsBound = false;

	// 发送信息
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	// 服务端返回的登录结果消息
	private String returnMessage = "";

	static Handler announcementHandler;
	private String[] contentArr;

	// 当前登录的教师
	private Teacher teacher;
	
	//定义接口
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
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("AnnouncementFragment解除与Service的绑定");
		}
	}*/
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("AnnouncementFragment OnStop");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("AnnouncementFragment解除与Service的绑定");
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
		
		// 获取当前教师
		Bundle bundle = getArguments();
		if (bundle != null) {
			teacher = (Teacher) bundle.getSerializable("teacher");
		}
		
		if (savedInstanceState != null) {
			teacher = (Teacher) savedInstanceState.getSerializable("teacher");
		}
		
		// 获取布局中的控件
		//btn_delete_announcement = (Button) getActivity().findViewById(R.id.btn_delete_announcement);
		//btn_new_announcement = (Button) getActivity().findViewById(R.id.btn_new_announcement);
		list_announcement = (MyListView) getActivity().findViewById(R.id.list_announcement);
		tv_list_empty = (TextView) getActivity().findViewById(R.id.tv_announcement_list_empty);

		// 获取公告信息
		listItems = new ArrayList<Announcement>();
		// 创建适配器
		listadapter = new AnnouncementListAdapter(context, listItems);
		list_announcement.setCacheColorHint(Color.TRANSPARENT);
		// 为列表设置适配器
		list_announcement.setAdapter(listadapter);
		// 为列表设置数据为空时的提示
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

		// 添加按钮的侦听事件
		//btn_delete_announcement.setOnClickListener(this);
		//btn_new_announcement.setOnClickListener(this);

		announcementHandler = new AnnouncementHandler(getActivity().getMainLooper());

		sendMessage = "{\"REQ\":\"getAnnouncement\",\"DAT\":{\"offset\":0,\"amount\":10}}";

		// 绑定服务
		Intent intent = new Intent(getActivity(),
				SocketService.class);
		// 先打开Service, 然后将当前Activity与该Service绑定
		if (mBoundService == null) {
			System.out.println("将AnnouncementFragment绑定到服务");
			getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			System.out.println("完成将AnnouncementFragment绑定到服务");
		} else {
			mBoundService.startSendMessage(sendMessage);
			// 设置Service获得服务端返回消息后的消息处理器
			mBoundService.setUpdateClientHandler(announcementHandler);
		}
	}
	
	// 与Service的连接对象
	private ServiceConnection mConnection = new ServiceConnection() {
		// 当前Activity与Service意外失去连接后(如Service终止)调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("AnnouncementFragment解除与Service绑定");
		}

		// 当前Activity与Service绑定后调用(onBind()后触发)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("AnnouncementFragment与Service绑定成功");
			// 连接服务端,并请求发送班级请求数据
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			} else {
				mBoundService.startSendMessage(sendMessage);
			}
			// 设置Service获得服务端返回消息后的消息处理器
			mBoundService.setUpdateClientHandler(announcementHandler);
		}
	};
	

	/**
	 * 更新公告列表的Handler
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
				System.out.println("客户端接收到的消息:" + returnMessage);
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
							"删除了" + count + "条公告", Toast.LENGTH_SHORT)
							.show();
				}
			}
			listadapter.notifyDataSetChanged();
		}
	}

	/**
	 * 返回公告信息列表
	 * 
	 * @return listItems 公告信息列表
	 */
	private void updateAnnouncementListItems(Announcement[] announcements) {
		listItems.clear();
		for (int i = 0; i < announcements.length; i++) {
			listItems.add(announcements[i]);
			contentArr[i] = announcements[i].getContent();
		}
	}

	/**
	 * 删除选择的公告
	 * 
	 * @return 删除的公告数目
	 */
	private void deleteAnnouncement() {
		// 获得总公告的数目
		int size = listItems.size();
		ArrayList<String> newsIdString = new ArrayList<String>();
		// 这里一定要从后往前删除（因为每删除一个列表项，ListView就会重新对列表项编号）
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
		// 从服务端删除
		sendMessage = "{\"REQ\":\"deleteAnnouncement\",\"DAT\":{\"news_id\":"
				+ idString + "}}";

		// 开始发送消息
		mBoundService.startSendMessage(sendMessage);
		// 设置Service获得服务端返回消息后的消息处理器
		mBoundService.setUpdateClientHandler(announcementHandler);
		
	}

	/**
	 * 确认删除公告
	 */
	private void deleteAnnouncementDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle("确认删除公告")
				.setMessage("是否要删除选中的公告?")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						deleteAnnouncement();
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Toast.makeText(getActivity(), "取消了删除公告",
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