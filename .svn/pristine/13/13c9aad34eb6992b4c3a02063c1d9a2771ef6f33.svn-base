package cn.edu.ecnu.sophia.motionobservation.remind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.ecnu.sophia.motionobservation.R;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewAdapter extends BaseAdapter implements Filterable {
	private Context context; // 运行上下文

	private final Object mLock = new Object();
	private MyFilter myFilter;// 过滤器
	private ArrayList<Map<String, Object>> mOriginalValues; //所有原信息元素
	private List<Map<String, Object>> listItems; // 列表信息集合
	private LayoutInflater listContainer; // 视图容器
	ListItemView listItemView;

	public final class ListItemView { // 自定义控件集合
		public TextView tv_number;   //学生学号
		public TextView tv_name;     //学生姓名
		public TextView tv_sex;     //性别
		public TextView tv_yundongliang;    //运动量
		public TextView tv_nengliangxiaohao;     //能量消耗
		public TextView tv_wanchenqingkuang;     //能量消耗		
		public Button btn_zidong;   //自动提醒按钮
		public Button btn_shoudong;   //手动提醒按钮
	}

	/**
	 * 列表视图适配器
	 * @param context
	 * @param listItems
	 */
	public ListViewAdapter(Context context, List<Map<String, Object>> listItems) {
		this.context = context;
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.listItems = listItems;
	}

	/**
	 * 获取适配器数据总数
	 * @return count of items
	 */
	public int getCount() {
		return listItems.size();
	}

	/**
	 * 适配器中获取相应数据
	 * @param arg0
	 * @return item
	 */
	public Object getItem(int arg0) {
		return listItems.get(arg0);
	}

	/**
	 * get the row id of the item
	 * @param arg0
	 * @return row id
	 */
	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * ListView Item设置
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return view
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.e("method", "getView");
		final int selectID = position;
		// 自定义视图
		listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取list_item布局文件的视图
			convertView = listContainer
					.inflate(R.layout.remind_list_item, null);
			// 获取控件对象
			listItemView.tv_number = (TextView) convertView
					.findViewById(R.id.number);
			listItemView.tv_name = (TextView) convertView.findViewById(R.id.name);
			listItemView.tv_sex = (TextView) convertView.findViewById(R.id.sex);
			listItemView.tv_yundongliang = (TextView) convertView
					.findViewById(R.id.yundongliang);
			listItemView.tv_nengliangxiaohao = (TextView) convertView
					.findViewById(R.id.nengliangxiaohao);
			listItemView.tv_wanchenqingkuang = (TextView) convertView
					.findViewById(R.id.wanchenqingkuang);
			listItemView.btn_zidong = (Button) convertView
					.findViewById(R.id.zidong);
			listItemView.btn_shoudong = (Button) convertView
					.findViewById(R.id.shoudong);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置文字
		listItemView.tv_number.setText((String) listItems.get(position).get(
				"number"));
		listItemView.tv_name.setText((String) listItems.get(position).get("name"));
		listItemView.tv_sex.setText((String) listItems.get(position).get("sex"));
		listItemView.tv_yundongliang.setText((String) listItems.get(position).get(
				"yundongliang"));
		listItemView.tv_nengliangxiaohao.setText((String) listItems.get(position).get(
				"nengliangxiaohao"));
		listItemView.tv_wanchenqingkuang.setText((String) listItems.get(position).get(
				"wanchenqingkuang"));
		listItemView.btn_zidong.setText("自动提醒");
		listItemView.btn_shoudong.setText("手动提醒");
		listItemView.btn_shoudong.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RemindFragment.mCallback.onShoudongClick();
			}
		});
		listItemView.btn_zidong.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//提示用户自动提醒发送成功
				Toast tos = Toast.makeText(context,
						"已提醒" + listItems.get(selectID).get("name") + "同学",
						Toast.LENGTH_LONG);
				tos.show();
				listItems.remove(selectID);
				notifyDataSetChanged();
			}
		});
		return convertView;
	}

	/**
	 * 设置过滤器
	 */
	@Override
	public Filter getFilter() {
		if (myFilter == null) {
			myFilter = new MyFilter();
		}
		return myFilter;
	}

	class MyFilter extends Filter {

		/**
		 * Holds the results of a filtering operation. 
		 * results: values and the number of the values
		 * @param prefix
		 * @return results
		 */
		protected FilterResults performFiltering(CharSequence prefix) {
			// 持有过滤操作完成之后的数据。该数据包括过滤操作之后的数据值以及数量。 
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					// 将list的用户集合转换给该原始数据的ArrayList
					mOriginalValues = new ArrayList<Map<String, Object>>(
							listItems);
				}
			}
			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
							mOriginalValues);
					results.values = list;      //数据值
					results.count = list.size();    //数据数量
				}
			} else {
				// 做正式的筛选
				String prefixString = prefix.toString().toLowerCase();

				// 声明一个临时集合对象后将原始数据赋给这个临时变量
				final ArrayList<Map<String, Object>> values = mOriginalValues;
				final int count = values.size();

				// 新的集合对象
				final ArrayList<Map<String, Object>> newValues = new ArrayList<Map<String, Object>>(
						count);

				for (int i = 0; i < count; i++) {
					// 如果姓名的前缀相符或者学号值相符就添加到新的集合
					final Map<String, Object> value = (Map<String, Object>) values
							.get(i);
					if (value.get("name").toString().toLowerCase()
							.startsWith(prefixString)
							|| value.get("number").toString()
									.startsWith(prefixString))
						newValues.add(value);
				}
				// 将该新集合数据赋给FilterResults对象
				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		/**
		 * Invoked in the UI thread to publish the filtering results in the user interface
		 * @param constraint used to filter the data
		 * @param results the results of filtering operation
		 */
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// 将与适配器相关联的List进行重赋值
			listItems = (List<Map<String, Object>>) results.values;
			
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}