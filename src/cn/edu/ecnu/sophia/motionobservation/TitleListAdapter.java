package cn.edu.ecnu.sophia.motionobservation;

import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.model.NavTitle;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleListAdapter extends BaseAdapter {

	private List<NavTitle> mDataList;
	private LayoutInflater mInflater; // ÊÓÍ¼ÈÝÆ÷
	
	private int mSelectIndex = -1;
	public void setSelectIndex(int selectIndex){
		this.mSelectIndex = selectIndex;
	}
	
	public TitleListAdapter(Context context, List<NavTitle> mDataList){
		this.mInflater = LayoutInflater.from(context);
		this.mDataList = mDataList;
	}
	
	final class ListItemView{
		private ImageView imageView;
		private TextView textView;
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			convertView = mInflater.inflate(R.layout.nav_list_item, null);
			
			listItemView.imageView = (ImageView) convertView.findViewById(R.id.imgView_nav_list);
			listItemView.textView = (TextView) convertView.findViewById(R.id.tv_nav_list);
			
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		listItemView.imageView.setImageResource(mDataList.get(position).getImgId());
		listItemView.textView.setText(mDataList.get(position).getText());
		//listItemView.textView.setTextSize(24);
		
		if(position == mSelectIndex){
			convertView.setBackgroundColor(Color.BLACK);
		} else {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return convertView;
	}

}
