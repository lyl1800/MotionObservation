package cn.edu.ecnu.sophia.motionobservation.home;

import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.model.Announcement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class AnnouncementListAdapter extends BaseAdapter {

	private Context context; // ����������
	private List<Announcement> listItems; // ������Ϣ����
	private LayoutInflater listContainer; // ��ͼ����
	
	OnPersonalAnnouncementListener mCallback;
	public interface OnPersonalAnnouncementListener {
		public void onPersonalAnnoucementSelected(Object object);
	}
	
	public final class ListItemView { // �Զ���Ŀؼ�����
		public CheckBox chk_announcement;
		public TextView tv_announcement_content;
		//public TextView tv_announcement_author;
		//public TextView tv_announcement_time;
	}

	public AnnouncementListAdapter(Context context,
			List<Announcement> listItems) {
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
			listItemView.chk_announcement = (CheckBox) convertView
					.findViewById(R.id.chk_announcement_list_item);
			listItemView.tv_announcement_content = (TextView) convertView
					.findViewById(R.id.tv_announcement_content);
			/*listItemView.tv_announcement_author = (TextView) convertView
					.findViewById(R.id.tv_announcement_author);
			listItemView.tv_announcement_time = (TextView) convertView
					.findViewById(R.id.tv_announcement_time);*/
			// ���ÿؼ�����convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// ���ÿؼ�������
		listItemView.chk_announcement.setChecked(((Boolean) listItems.get(
				position).isChecked()).booleanValue());
		listItemView.tv_announcement_content.setText((String) listItems.get(
				position).getTitle());
		/*listItemView.tv_announcement_author.setText((String) listItems.get(
				position).getPublisher());
		listItemView.tv_announcement_time.setText((String) listItems.get(
				position).getPublishTime());*/

		// ע���ѡ��״̬ѡ���¼�������
		listItemView.chk_announcement
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
		listItemView.tv_announcement_content
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AnnouncementFragment.mCallback.onPersonalAnnoucementSelected(listItems.get(selectID));
					}
				});

		return convertView;
	}

}
