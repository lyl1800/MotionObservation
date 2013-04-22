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

	private Context context; // 运行上下文
	private List<News> listItems; // 公告信息集合
	private LayoutInflater listContainer; // 视图容器
	
	OnPersonalAnnouncementListener mCallback;
	public interface OnPersonalAnnouncementListener {
		public void onPersonalAnnoucementSelected(Object object);
	}
	
	public final class ListItemView { // 自定义的控件集合
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
	 * 判断公告是否选择
	 * 
	 * @param checkedID
	 *            公告序号
	 * @return 返回是否选中状态
	 */
	public boolean hasChecked(int checkedID) {
		// return hasChecked[checkedID];
		return (Boolean) listItems.get(checkedID).isChecked();
	}

	/**
	 * 绘制列表项
	 * 
	 * @param position
	 *            当前绘制的列表项在父控件所处的位置
	 * @param convertView
	 *            当前列表项的布局
	 * @param parent
	 *            当前列表项的父容器
	 * @return 绘制好的当前列表项实例
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int selectID = position;
		// 自定义视图
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取公告列表项announcement_list_item布局文件的视图
			convertView = listContainer.inflate(
					R.layout.announcement_list_item, null);
			// 获取控件对象
			listItemView.chk_news = (CheckBox) convertView
					.findViewById(R.id.chk_announcement_list_item);
			listItemView.tv_news_content = (TextView) convertView
					.findViewById(R.id.tv_announcement_content);
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置控件的内容
		listItemView.chk_news.setChecked(((Boolean) listItems.get(
				position).isChecked()).booleanValue());
		listItemView.tv_news_content.setText((String) listItems.get(
				position).getTitle());

		// 注册多选框状态选择事件并处理
		listItemView.chk_news
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 切换公告的选中状态
						// checkedChange(selectID);
						listItems.get(selectID).setChecked(isChecked);
					}
				});
		
		// 注册点击公告内容查看详细公告信息的事件并处理
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
