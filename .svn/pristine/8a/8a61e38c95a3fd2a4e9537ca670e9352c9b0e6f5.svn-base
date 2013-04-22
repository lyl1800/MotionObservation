package cn.edu.ecnu.sophia.motionobservation.remind;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;

public class SendRemindFragment extends Fragment {
	// checkBox id: cb_remind_list_item
	// textView id: remind_content
	private Button btn_send_message = null;
	private ListView list_saved_remind;     //已保存提醒
	private ListView list_history_remind;    //历史提醒 
	private ListViewForSendAdapter listViewForSendAdapter;    //发送提醒适配器
	private ArrayList<String> listItems_for_save = new ArrayList<String>();    //提醒信息列表项
	private ArrayList<String> listItems_for_history = new ArrayList<String>();
	private EditText txt_box;    //编辑框
	private String[] content_remain = { "1. 请抓紧时间完成目标运动量",
			                            "2. 最近一段时间你的运动量都未达标，请速来办公室见我",
			                            "3. 请多加强体育锻炼" };  //已保存提醒
	ArrayAdapter<String> myArrayAdapter1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.send_remind, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MainActivity.tv_details_title.setText("发送提醒");
		// 编辑框    
		txt_box = (EditText) getActivity().findViewById(R.id.txt_box);

		// 发送提醒按钮
		btn_send_message = (Button) getActivity().findViewById(R.id.send_btn);
		btn_send_message.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				// 发送提醒后显示信息
				String st = new String("");
				st = st + String.valueOf(listItems_for_save.size() + 1) + "."
						+ txt_box.getText().toString(); // 提取编辑框中信息，保存到历史提醒栏中
				listItems_for_save.add(st);
				myArrayAdapter1.notifyDataSetChanged();
				Toast tos = Toast.makeText(getActivity(), "提醒发送成功",
						Toast.LENGTH_LONG);  //显示发送成功提醒字样

				tos.show();

			}
		});

		// 设置适配器
		list_saved_remind = (ListView) getActivity().findViewById(R.id.list_saved_remind);
		list_history_remind = (ListView) getActivity().findViewById(R.id.list_history_remind);
		for (int i = 0; i < content_remain.length; i++) {
			listItems_for_save.add(content_remain[i]);
		}

		// 已保存提醒  adapter
		ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, listItems_for_save);
		list_saved_remind.setAdapter(myArrayAdapter);

		// 历史提醒  adapter
		myArrayAdapter1 = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, listItems_for_history);
		list_history_remind.setAdapter(myArrayAdapter1);

		list_saved_remind.setOnItemClickListener(new OnItemClickListener() {

			/**
			 * 点击获取已保存提醒栏信息，至编辑框中编辑
			 */
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (listItems_for_save.get(arg2).equals(content_remain[0])) {
					String str = new String("");
					str = str
							+ content_remain[0].subSequence(2,
									content_remain[0].length());
					txt_box.setText(str);

				}
				if (listItems_for_save.get(arg2).equals(content_remain[1])) {
					String str = new String("");
					str = str
							+ content_remain[1].subSequence(2,
									content_remain[1].length());
					txt_box.setText(str);
				}
				if (listItems_for_save.get(arg2).equals(content_remain[2])) {
					String str = new String("");
					str = str
							+ content_remain[2].subSequence(2,
									content_remain[2].length());
					txt_box.setText(str);
				}
			}
		});
	}
	
}
