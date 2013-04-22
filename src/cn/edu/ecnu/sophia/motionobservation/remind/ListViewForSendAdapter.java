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
	private Context context;     //����������
	private List<Map<String,Object>> remindItems;       //��ʾ��Ϣ����
	private LayoutInflater layoutInflater;      //��ͼ����
	private boolean[] remindChecked;        //��¼��Ϣѡ��״̬
	
	
	public final class RemindItemView{
		public TextView tv_number;    //�����������
		public TextView tv_remind;    //������Ϣ����
	}
	
	/**
	 * ����������
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
	 * ��ȡ������Ϣ����ֵ
	 */
	public int getCount() {
		return remindItems.size();
	}

	/**
	 * ��ȡ������Ϣ
	 * @param position
	 * @return remindItem
	 */
	public Object getItem(int position) {
		return remindItems.get(position);
	}

	/**
	 * ��ȡ������ϢId
	 * @param position
	 * @return position��itemId
	 */
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * �ж�������Ϣ�Ƿ�ѡ��
	 * @param checkedID ������Ϣ���         
	 * @return �����Ƿ�ѡ��״̬
	 */
	public boolean hasChecked(int checkedID) {
		// return hasChecked[checkedID]
		return (Boolean) remindItems.get(checkedID).get("checkbox");
	}
	/**
	 * �����б���
	 * @param position
	 *            ��ǰ���Ƶ��б����ڸ��ؼ�������λ��
	 * @param currentView
	 *            ��ǰ�б���Ĳ���
	 * @param parent
	 *            ��ǰ�б���ĸ�����
	 * @return ���ƺõĵ�ǰ�б���ʵ��
	 */
	public View getView(int position,View currentView,ViewGroup parent) {
		System.out.println("Adapter1_getView()");
		final int selectID = position;
		//�Զ�����ͼ
		RemindItemView remindItemView = null;
		if (currentView == null){
			remindItemView=new RemindItemView();
			//��ȡ�����б���activity_remind�����ļ�����ͼ
			currentView=layoutInflater.inflate(R.layout.sendremind_list_item, null);
			//��ȡ�ؼ�����
			remindItemView.tv_number= (TextView) currentView
					.findViewById(R.id.remind_content_number);
			remindItemView.tv_number = (TextView) currentView
					.findViewById(R.id.remind_content);
			//���ÿؼ�����currentView
			currentView.setTag(remindItemView);
		}else{
			remindItemView = (RemindItemView) currentView.getTag();
		}
		//���ÿؼ�����
		remindItemView.tv_number.setText((String) remindItems.get(
				position).get("content_number"));
		remindItemView.tv_number.setTextSize(25);
		remindItemView.tv_number.setText((String) remindItems.get(
				position).get("content_remain"));
		remindItemView.tv_number.setTextSize(25);
		return currentView;
	}
}
