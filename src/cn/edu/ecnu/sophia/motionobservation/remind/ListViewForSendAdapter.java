package cn.edu.ecnu.sophia.motionobservation.remind;

import java.util.List;
import java.util.Map;

import cn.edu.ecnu.sophia.motionobservation.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ListViewForSendAdapter extends BaseAdapter {
	private Context context;     //运行上下文
	private List<Map<String,Object>> remindItems;       //提示消息集合
	private LayoutInflater layoutInflater;      //视图容器
	private boolean[] remindChecked;        //记录消息选中状态
	
	
	public final class RemindItemView{
		public TextView tv_number;    //提醒内容序号
		public TextView tv_remind;    //提醒信息内容
	}
	
	/**
	 * 设置适配器
	 * @param context
	 * @param RemindItems
	 */
	public ListViewForSendAdapter(Context context,
			List<Map<String,Object>> RemindItems){
		this.context=context;
		this.layoutInflater=LayoutInflater.from(context);
		this.remindItems=RemindItems;
		remindChecked=new boolean[getCount()];
	}
	
	/**
	 * 获取提醒信息数量值
	 */
	public int getCount() {
		return remindItems.size();
	}

	/**
	 * 获取提醒信息
	 * @param position
	 * @return remindItem
	 */
	public Object getItem(int position) {
		return remindItems.get(position);
	}

	/**
	 * 获取提醒信息Id
	 * @param position
	 * @return position：itemId
	 */
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * 判断提醒信息是否选择
	 * @param checkedID 提醒信息序号         
	 * @return 返回是否选中状态
	 */
	public boolean hasChecked(int checkedID) {
		// return hasChecked[checkedID]
		return (Boolean) remindItems.get(checkedID).get("checkbox");
	}
	/**
	 * 绘制列表项
	 * @param position
	 *            当前绘制的列表项在父控件所处的位置
	 * @param currentView
	 *            当前列表项的布局
	 * @param parent
	 *            当前列表项的父容器
	 * @return 绘制好的当前列表项实例
	 */
	public View getView(int position,View currentView,ViewGroup parent) {
		System.out.println("Adapter1_getView()");
		final int selectID = position;
		//自定义视图
		RemindItemView remindItemView = null;
		if (currentView == null){
			remindItemView=new RemindItemView();
			//获取提醒列表项activity_remind布局文件的视图
			currentView=layoutInflater.inflate(R.layout.sendremind_list_item, null);
			//获取控件对象
			remindItemView.tv_number= (TextView) currentView
					.findViewById(R.id.remind_content_number);
			remindItemView.tv_number = (TextView) currentView
					.findViewById(R.id.remind_content);
			//设置控件集到currentView
			currentView.setTag(remindItemView);
		}else{
			remindItemView = (RemindItemView) currentView.getTag();
		}
		//设置控件内容
		remindItemView.tv_number.setText((String) remindItems.get(
				position).get("content_number"));
		remindItemView.tv_number.setTextSize(25);
		remindItemView.tv_number.setText((String) remindItems.get(
				position).get("content_remain"));
		remindItemView.tv_number.setTextSize(25);
		return currentView;
	}
}
