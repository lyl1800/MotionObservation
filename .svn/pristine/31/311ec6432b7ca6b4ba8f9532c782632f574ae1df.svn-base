package cn.edu.ecnu.sophia.motionobservation.motion.daily;

import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.R.drawable;
import cn.edu.ecnu.sophia.motionobservation.R.id;
import cn.edu.ecnu.sophia.motionobservation.R.layout;
import cn.edu.ecnu.sophia.motionobservation.model.Student;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class DailyStudentListAdapter extends BaseAdapter {

	private Context context; // 上下文
	private List<Student> listItems; // 学生信息集合
	private LayoutInflater listContainer; // 视图容器

	public final class ListItemView { // 自定义的控件集合
		public ImageButton imgBtn_student_head_portrait;
		public TextView tv_student_name;
		public TextView tv_student_number;
	}

	public DailyStudentListAdapter(Context context, List<Student> listItems){
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
	 * 绘制每个学生信息项
	 * 
	 * @param position
	 *            当前绘制的项在父控件所处的位置
	 * @param convertView
	 *            当前项的布局
	 * @param parent
	 *            当前项的父容器
	 * @return 绘制好的当前项实例
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int index = position;
		// 自定义视图
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取学生信息项announcement_list_item布局文件的视图
			convertView = listContainer.inflate(
					R.layout.daily_student_list, null);
			// 获取控件对象
			listItemView.imgBtn_student_head_portrait = (ImageButton) convertView
					.findViewById(R.id.imgBtn_student_head_portrait);
			listItemView.tv_student_name = (TextView) convertView
					.findViewById(R.id.tv_student_name);
			listItemView.tv_student_number = (TextView) convertView
					.findViewById(R.id.tv_student_number);
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		// 首先获取数据集中的内容
		Bitmap bm = listItems.get(position).getBitmapAvatar();
		String sname = listItems.get(position).getSname();
		final String sno = listItems.get(position).getSno();
		
		// 设置控件的内容
		if(bm != null){
			listItemView.imgBtn_student_head_portrait.setImageBitmap(bm);
		} else {
			listItemView.imgBtn_student_head_portrait.setImageResource(R.drawable.head_online);
		}
		listItemView.tv_student_name.setText(sname);
		listItemView.tv_student_number.setText(sno);
		
		
		// 注册点击学生头像按钮的事件并处理
		listItemView.imgBtn_student_head_portrait.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DailyExerciseFragment.mCallback.onDailyStudentClicked(listItems.get(index));
			}
		});
		
		return convertView;
	}

}
