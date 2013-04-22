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
	private Context context; // ����������

	private final Object mLock = new Object();
	private MyFilter myFilter;// ������
	private ArrayList<Map<String, Object>> mOriginalValues; //����ԭ��ϢԪ��
	private List<Map<String, Object>> listItems; // �б���Ϣ����
	private LayoutInflater listContainer; // ��ͼ����
	ListItemView listItemView;

	public final class ListItemView { // �Զ���ؼ�����
		public TextView tv_number;   //ѧ��ѧ��
		public TextView tv_name;     //ѧ������
		public TextView tv_sex;     //�Ա�
		public TextView tv_yundongliang;    //�˶���
		public TextView tv_nengliangxiaohao;     //��������
		public TextView tv_wanchenqingkuang;     //��������		
		public Button btn_zidong;   //�Զ����Ѱ�ť
		public Button btn_shoudong;   //�ֶ����Ѱ�ť
	}

	/**
	 * �б���ͼ������
	 * @param context
	 * @param listItems
	 */
	public ListViewAdapter(Context context, List<Map<String, Object>> listItems) {
		this.context = context;
		listContainer = LayoutInflater.from(context); // ������ͼ����������������
		this.listItems = listItems;
	}

	/**
	 * ��ȡ��������������
	 * @return count of items
	 */
	public int getCount() {
		return listItems.size();
	}

	/**
	 * �������л�ȡ��Ӧ����
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
	 * ListView Item����
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return view
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.e("method", "getView");
		final int selectID = position;
		// �Զ�����ͼ
		listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// ��ȡlist_item�����ļ�����ͼ
			convertView = listContainer
					.inflate(R.layout.remind_list_item, null);
			// ��ȡ�ؼ�����
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

			// ���ÿؼ�����convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// ��������
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
		listItemView.btn_zidong.setText("�Զ�����");
		listItemView.btn_shoudong.setText("�ֶ�����");
		listItemView.btn_shoudong.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RemindFragment.mCallback.onShoudongClick();
			}
		});
		listItemView.btn_zidong.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//��ʾ�û��Զ����ѷ��ͳɹ�
				Toast tos = Toast.makeText(context,
						"������" + listItems.get(selectID).get("name") + "ͬѧ",
						Toast.LENGTH_LONG);
				tos.show();
				listItems.remove(selectID);
				notifyDataSetChanged();
			}
		});
		return convertView;
	}

	/**
	 * ���ù�����
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
			// ���й��˲������֮������ݡ������ݰ������˲���֮�������ֵ�Լ������� 
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					// ��list���û�����ת������ԭʼ���ݵ�ArrayList
					mOriginalValues = new ArrayList<Map<String, Object>>(
							listItems);
				}
			}
			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
							mOriginalValues);
					results.values = list;      //����ֵ
					results.count = list.size();    //��������
				}
			} else {
				// ����ʽ��ɸѡ
				String prefixString = prefix.toString().toLowerCase();

				// ����һ����ʱ���϶����ԭʼ���ݸ��������ʱ����
				final ArrayList<Map<String, Object>> values = mOriginalValues;
				final int count = values.size();

				// �µļ��϶���
				final ArrayList<Map<String, Object>> newValues = new ArrayList<Map<String, Object>>(
						count);

				for (int i = 0; i < count; i++) {
					// ���������ǰ׺�������ѧ��ֵ�������ӵ��µļ���
					final Map<String, Object> value = (Map<String, Object>) values
							.get(i);
					if (value.get("name").toString().toLowerCase()
							.startsWith(prefixString)
							|| value.get("number").toString()
									.startsWith(prefixString))
						newValues.add(value);
				}
				// �����¼������ݸ���FilterResults����
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
			// �����������������List�����ظ�ֵ
			listItems = (List<Map<String, Object>>) results.values;
			
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}