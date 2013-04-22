package cn.edu.ecnu.sophia.motionobservation.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.model.MyListView;
import cn.edu.ecnu.sophia.motionobservation.model.News;
import cn.edu.ecnu.sophia.motionobservation.model.MyListView.OnRefreshListener;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NewsFragment extends Fragment {
private Context context;
	
	// �ؼ�
	private MyListView list_news;
	private TextView tv_list_empty;

	// ��������
	private List<News> listItems;
	private NewsListAdapter listadapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.news, container, false);
	}
	
	final Handler refreshListViewHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			listadapter.notifyDataSetChanged();
			list_news.onRefreshComplete();
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
	
		list_news = (MyListView) getActivity().findViewById(R.id.list_news);
		tv_list_empty = (TextView) getActivity().findViewById(R.id.tv_news_list_empty);

		// ��ȡ������Ϣ
		listItems = new ArrayList<News>();
		for (int i = 0; i < 10; i++) {
			News news = new News();
			news.setTitle("��ѧ��ѧҵ��չָ�����Ļ����");
			listItems.add(news);
		}
		// ����������
		listadapter = new NewsListAdapter(context, listItems);
		list_news.setCacheColorHint(Color.TRANSPARENT);
		// Ϊ�б�����������
		list_news.setAdapter(listadapter);
		// Ϊ�б���������Ϊ��ʱ����ʾ
		list_news.setEmptyView(tv_list_empty);
		
		list_news.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				News news = new News();
				news.setTitle("test refresh");
				news.setContent("test Content");
				news.setPublisher("test");
				news.setPublishTime(new Date().toLocaleString());
				listItems.add(0, news);
				refreshListViewHandler.obtainMessage();
				refreshListViewHandler.sendEmptyMessage(0);
			}
		});
	}

}
