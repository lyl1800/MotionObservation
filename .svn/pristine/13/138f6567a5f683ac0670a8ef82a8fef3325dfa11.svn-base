package cn.edu.ecnu.sophia.motionobservation.remind;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.model.PeClass;
import cn.edu.ecnu.sophia.motionobservation.model.Student;

public class RemindFragment extends Fragment {

	private ListView list_detail;  //列表视图
	private TextView tv_list_empty;  //列表数据为空
	private ListViewAdapter listViewAdapter;     
	private List<Map<String, Object>> listItems;  //设置列表项

	private SearchView sv_filter = null;    //学生姓名检索
	private Button btn_remindAll = null;    //全部自动提醒按钮
	
	private PeClass peClass;
	private Student curStudent;
	
	//定义接口
	public static OnRemindListener mCallback;
	public interface OnRemindListener {
		public void onShoudongClick();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnRemindListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement OnAnnouncementListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.remind, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		if (bundle != null) {
			curStudent = (Student) bundle.getSerializable("student");
		}
		MainActivity.tv_details_title.setText("提醒");
		
		// 实例化控件
		sv_filter = (SearchView) getActivity().findViewById(R.id.searchName);
		btn_remindAll = (Button) getActivity().findViewById(R.id.remindAll_btn);
		tv_list_empty = (TextView) getActivity().findViewById(R.id.tv_remind_list_empty);
		btn_remindAll.setText("全部提醒");
		
		btn_remindAll.setOnClickListener(new View.OnClickListener() {
			/**
			 * the result after click the btn_remindAll
			 * @return null
			 */
			public void onClick(View view) {
				Toast tos = Toast.makeText(getActivity(), "已提醒全部的"
						+ listItems.size() + "名同学", Toast.LENGTH_LONG);
				tos.show();
				listItems.removeAll(listItems);
				listViewAdapter.notifyDataSetChanged();
			}
		});

		//实例化控件
		list_detail = (ListView) getActivity().findViewById(R.id.detailList);    //find the listView
		listItems = getListItems();     //初始化列表信息
		listViewAdapter = new ListViewAdapter(getActivity(), listItems);   // 创建适配器
		list_detail.setAdapter(listViewAdapter);    //获取适配器数据
		list_detail.setEmptyView(tv_list_empty);    //适配器为空

		sv_filter.setOnQueryTextListener(new OnQueryTextListener() {
			/**
			 * Called when the query text is changed by the user
			 * @param newText the new content of the query text field
			 * @return false or false
			 */
			public boolean onQueryTextChange(String newText) {
				Filter filter = listViewAdapter.getFilter();
				filter.filter(newText);
				return false;
			}
			/**Called when the user submits the query. 
			 * @param query the query text that is to be submitted
			 * @return true or false
             */
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
		});
	}

	/**
	 * 初始化列表信息：number学号、name姓名、yundongliang运动量、weiwancheng未完成
	 * @return listItems
	 */
	private List<Map<String, Object>> getListItems() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();  //创建list对象
		if(curStudent != null){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("number", curStudent.getSno());
			map.put("name", curStudent.getSname());
			int steps = (int)(Math.random()*1000);
			map.put("yundongliang", steps + "步");
			map.put("weiwancheng", "" +(1000 - steps) + "步");
			listItems.add(map);
		} else if(peClass != null) {
			for (int i = 0; i < peClass.getStudents().length; i++) {
				Student student = peClass.getStudents()[i];
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("number", student.getSno());
				map.put("name", student.getSname());
				int steps = (int)(Math.random()*1000);
				map.put("yundongliang", steps + "步");
				map.put("weiwancheng", "" + (1000 - steps) + "步");
				listItems.add(map);
			}
		}
		return listItems;
	}

}