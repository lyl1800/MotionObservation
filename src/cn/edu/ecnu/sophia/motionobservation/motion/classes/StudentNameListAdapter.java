package cn.edu.ecnu.sophia.motionobservation.motion.classes;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.home.AnnouncementFragment;
import cn.edu.ecnu.sophia.motionobservation.home.AnnouncementListAdapter.ListItemView;
import cn.edu.ecnu.sophia.motionobservation.home.AnnouncementListAdapter.OnPersonalAnnouncementListener;
import cn.edu.ecnu.sophia.motionobservation.model.Announcement;
import cn.edu.ecnu.sophia.motionobservation.model.Student;

public class StudentNameListAdapter extends BaseAdapter {
	private Context context; // 运行上下文
	private List<Student> listItems; // 公告信息集合
	private LayoutInflater listContainer; // 视图容器
	
	public final class ListItemView { // 自定义的控件集合
		public TextView tv_sname;
	}

	public StudentNameListAdapter(Context context,
			List<Student> listItems) {
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
		// 自定义视图
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取公告列表项announcement_list_item布局文件的视图
			convertView = listContainer.inflate(
					R.layout.student_name_list_item, null);
			// 获取控件对象
			listItemView.tv_sname = (TextView) convertView
					.findViewById(R.id.tv_sname);
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置控件的内容
		listItemView.tv_sname.setText(listItems.get(
				position).getSname());

		return convertView;
	}


}
