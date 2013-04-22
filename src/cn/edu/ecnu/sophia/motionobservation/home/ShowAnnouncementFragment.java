package cn.edu.ecnu.sophia.motionobservation.home;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.model.Announcement;
import android.os.Bundle;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.*;

public class ShowAnnouncementFragment extends Fragment {
	private Announcement announcement = null;
	private TextView tv_title;
	private TextView tv_publisher;
	private TextView tv_publishTime;
	private TextView tv_content;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.show_announcement, container, false);
	}
	public void onActivityCreated(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
		
		
		// 实例化控件
		tv_title = (TextView)getActivity().findViewById(R.id.tv_announcement_title);
		tv_publisher = (TextView) getActivity().findViewById(R.id.tv_announcement_publisher);
		tv_publishTime = (TextView) getActivity().findViewById(R.id.tv_announcement_publishTime);
		tv_content = (TextView)getActivity().findViewById(R.id.tv_announcement_content);
		
		Bundle bundle = getArguments();
		if(bundle != null){
			announcement = (Announcement) bundle.getSerializable("announcement");
			tv_title.setText(announcement.getTitle());
			tv_publisher.setText(announcement.getPublisher());
			System.out.println(announcement.getPublisher());
			tv_publishTime.setText(announcement.getPublishTime());
			tv_content.setText(announcement.getContent());
		}

		MainActivity.tv_details_title.setText("公告详情");
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	class ReturnListenner implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			intent.setClass(getActivity(),
					HomeFragment.class);
			//ShowAnnouncementActivity.this.startActivity(intent);
			getActivity().finish();
		}

	}

}
