package cn.edu.ecnu.sophia.motionobservation.home;

import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.model.News;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class NewsListAdapter extends BaseAdapter {

	private Context context; // ����������
	private List<News> listItems; // ������Ϣ����
	private LayoutInflater listContainer; // ��ͼ����
	
	OnPersonalAnnouncementListener mCallback;
	public interface OnPersonalAnnouncementListener {
		public void onPersonalAnnoucementSelected(Object object);
	}
	
	public final class ListItemView { // �Զ���Ŀؼ�����
		public CheckBox chk_news;
		public TextView tv_news_content;
		//public TextView tv_announcement_author;
		//public TextView tv_announcement_time;
	}

	public NewsListAdapter(Context context,
			List<News> listItems) {
		this.context = context;
		this.listContainer = LayoutInflater.from(this.context);
		this.listItems = listItems;
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * �жϹ����Ƿ�ѡ��
	 * 
	 * @param checkedID
	 *            �������
	 * @return �����Ƿ�ѡ��״̬
	 */
	public boolean hasChecked(int checkedID) {
		// return hasChecked[checkedID];
		return (Boolean) listItems.get(checkedID).isChecked();
	}

	/**
	 * �����б���
	 * 
	 * @param position
	 *            ��ǰ���Ƶ��б����ڸ��ؼ�������λ��
	 * @param convertView
	 *            ��ǰ�б���Ĳ���
	 * @param parent
	 *            ��ǰ�б���ĸ�����
	 * @return ���ƺõĵ�ǰ�б���ʵ��
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int selectID = position;
		// �Զ�����ͼ
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// ��ȡ�����б���announcement_list_item�����ļ�����ͼ
			convertView = listContainer.inflate(
					R.layout.announcement_list_item, null);
			// ��ȡ�ؼ�����
			listItemView.chk_news = (CheckBox) convertView
					.findViewById(R.id.chk_announcement_list_item);
			listItemView.tv_news_content = (TextView) convertView
					.findViewById(R.id.tv_announcement_content);
			// ���ÿؼ�����convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// ���ÿؼ�������
		listItemView.chk_news.setChecked(((Boolean) listItems.get(
				position).isChecked()).booleanValue());
		listItemView.tv_news_content.setText((String) listItems.get(
				position).getTitle());

		// ע���ѡ��״̬ѡ���¼�������
		listItemView.chk_news
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// �л������ѡ��״̬
						// checkedChange(selectID);
						listItems.get(selectID).setChecked(isChecked);
					}
				});
		
		// ע�����������ݲ鿴��ϸ������Ϣ���¼�������
		listItemView.tv_news_content
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//AnnouncementFragment.mCallback.onPersonalAnnoucementSelected(listItems.get(selectID));
					}
				});

		return convertView;
	}

}
