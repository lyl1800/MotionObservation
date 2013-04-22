package cn.edu.ecnu.sophia.motionobservation;

import java.util.ArrayList;
import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.home.HomeFragment;
import cn.edu.ecnu.sophia.motionobservation.home.AnnouncementFragment;
import cn.edu.ecnu.sophia.motionobservation.home.SendAnnouncementFragment;
import cn.edu.ecnu.sophia.motionobservation.home.ShowAnnouncementFragment;
import cn.edu.ecnu.sophia.motionobservation.model.Announcement;
import cn.edu.ecnu.sophia.motionobservation.model.NavTitle;
import cn.edu.ecnu.sophia.motionobservation.model.PeClass;
import cn.edu.ecnu.sophia.motionobservation.model.Student;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;
import cn.edu.ecnu.sophia.motionobservation.motion.classes.CheckAttendanceFragment;
import cn.edu.ecnu.sophia.motionobservation.motion.classes.ClassCurMotionFragment;
import cn.edu.ecnu.sophia.motionobservation.motion.classes.ClassExerciseFragment;
import cn.edu.ecnu.sophia.motionobservation.motion.classes.StudentCurMotionFragment;
import cn.edu.ecnu.sophia.motionobservation.motion.daily.ClassDailyMotionFragment;
import cn.edu.ecnu.sophia.motionobservation.motion.daily.DailyExerciseFragment;
import cn.edu.ecnu.sophia.motionobservation.motion.daily.StudentDailyMotionFragment;
import cn.edu.ecnu.sophia.motionobservation.remind.RemindFragment;
import cn.edu.ecnu.sophia.motionobservation.remind.SendRemindFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ClassExerciseFragment.OnShowMotionListener,
		StudentCurMotionFragment.OnRemindBtnClickListener,
		AnnouncementFragment.OnAnnouncementListener,
		StudentDailyMotionFragment.OnRemindBtnClickListener,
		DailyExerciseFragment.OnDailyShowMotionListener,
		RemindFragment.OnRemindListener,
		CheckAttendanceFragment.OnCheckAttendanceListener {
	
	// 登录的教师
	private static Teacher teacher;
	public static TextView tv_details_title;
	
	private static boolean mIsHome = true;
	
	public static boolean isHome() {
		return mIsHome;
	}

	public static void setIsHome(boolean isHome) {
		mIsHome = isHome;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent in = getIntent();
		teacher = (Teacher) in.getSerializableExtra("loginTeacher");
		
		tv_details_title = (TextView) findViewById(R.id.tv_details_title);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	public static class TitlesFragment extends ListFragment {
		int mCurCheckPosition = 0;
		ListView listView;
		TitleListAdapter adapter;
		TextView tv_list_type_title;
		
		private int[] imgId = new int[]{R.drawable.home,R.drawable.single,R.drawable.multi,R.drawable.multi};
		private String[] text = new String[]{"首页","课堂锻炼","日常锻炼","发布管理"};

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			System.out.println("Fragment-->onCreateView");
			return super.onCreateView(inflater, container, savedInstanceState);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			System.out.println("Fragment-->onActivityCreted");
			
			listView = getListView();
			listView.setBackgroundColor(Color.rgb(96, 96, 96));
			
			tv_list_type_title = (TextView) getActivity().findViewById(R.id.tv_list_type_title);
			tv_list_type_title.setBackgroundColor(Color.DKGRAY);
			
			List<NavTitle> mDataList = new ArrayList<NavTitle>();
			for(int i = 0; i < text.length; i++){
				NavTitle navTitle = new NavTitle(imgId[i], text[i]);
				mDataList.add(navTitle);
			}
			
			adapter = new TitleListAdapter(getActivity(),mDataList);
			setListAdapter(adapter);

			if (savedInstanceState != null) {
				mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
				listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				showDetails(mCurCheckPosition);
			} else {
				adapter.setSelectIndex(0);
				showDetails(0);
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("curChoice", mCurCheckPosition);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			adapter.setSelectIndex(position);
			adapter.notifyDataSetInvalidated();
			showDetails(position);
		}

		void showDetails(int index) {
			mCurCheckPosition = index;
			getListView().setItemChecked(index, true);
			mIsHome = true;
			if (index == 0) {
				Fragment fragment = getFragmentManager().findFragmentById(
						R.id.details);
				if (fragment == null
						|| !(fragment instanceof HomeFragment)) {
					fragment = new HomeFragment();
					Bundle bundle = new Bundle();
					bundle.putSerializable("teacher", teacher);
					fragment.setArguments(bundle);
					}
					FragmentTransaction fTransaction = getFragmentManager()
							.beginTransaction();
					fTransaction.replace(R.id.details, fragment);
					fTransaction.addToBackStack(null);
					fTransaction.commit();
				} else if (index == 1) {
					Fragment fragment = getFragmentManager().findFragmentById(
							R.id.details);
					if (fragment == null
							|| !(fragment instanceof CheckAttendanceFragment)) {
						fragment = new CheckAttendanceFragment();
						Bundle bundle = new Bundle();
						bundle.putSerializable("teacher", teacher);
						fragment.setArguments(bundle);
					}
					FragmentTransaction fTransaction = getFragmentManager()
							.beginTransaction();
					fTransaction.replace(R.id.details, fragment);
					fTransaction.addToBackStack(null);
					fTransaction.commit();
				} else if (index == 2) {
					Fragment fragment = getFragmentManager().findFragmentById(
							R.id.details);
					if (fragment == null
							|| !(fragment instanceof DailyExerciseFragment)) {
						fragment = new DailyExerciseFragment();
					}
					FragmentTransaction fTransaction = getFragmentManager()
							.beginTransaction();
					fTransaction.replace(R.id.details, fragment);
					fTransaction.addToBackStack(null);
					fTransaction.commit();
			} else if (index == 3) {
				Fragment fragment = getFragmentManager().findFragmentById(
						R.id.details);
				if (fragment == null
						|| !(fragment instanceof SendAnnouncementFragment)) {
					fragment = new SendAnnouncementFragment();
				}
				FragmentTransaction fTransaction = getFragmentManager()
						.beginTransaction();
				fTransaction.replace(R.id.details, fragment);
				fTransaction.addToBackStack(null);
				fTransaction.commit();
			}
		}
	}

	public static class DetailsFragment extends Fragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		public static DetailsFragment newInstance(int index) {
			DetailsFragment details = new DetailsFragment();
			Bundle args = new Bundle();
			args.putInt("index", index);
			details.setArguments(args);
			return details;
		}

		public int getShownIndex() {
			return getArguments().getInt("index", 0);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}
	
	/**
	 * 按下返回键后，提示确认退出
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(mIsHome){
				new AlertDialog.Builder(this)
				.setTitle("确认退出吗？")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 点击“确认”后的操作
								MainActivity.this.finish();
							}
						})
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 点击“返回”后的操作,这里没有任何操作
							}
						}).show();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClassSelected(PeClass curPeClass) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if(fragment == null || !(fragment instanceof ClassCurMotionFragment)){
			fragment = new ClassCurMotionFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("curPeClass", curPeClass);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack so the user can navigate
		// back
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void onStudentClicked(Student student) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if(fragment == null || !(fragment instanceof StudentCurMotionFragment)){
			fragment = new StudentCurMotionFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("student", student);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onRemindBtnClicked(Student student) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if(fragment == null || !(fragment instanceof RemindFragment)){
			fragment = new RemindFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("student", student);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void onAnnoucementSelected(Teacher teacher,String[] contentArr) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if(fragment == null || !(fragment instanceof SendAnnouncementFragment)){
			fragment = new SendAnnouncementFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("teacher", teacher);
		args.putStringArray("contentArr", contentArr);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack so the user can navigate
		// back
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
		
	}

	@Override
	public void onPersonalAnnoucementSelected(Announcement announcement) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if(fragment == null || !(fragment instanceof ShowAnnouncementFragment)){
			fragment = new ShowAnnouncementFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("announcement", announcement);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void onDailyClassSelected(PeClass curPeClass) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if (fragment == null || !(fragment instanceof ClassDailyMotionFragment)) {
			fragment = new ClassDailyMotionFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("curPeClass", curPeClass);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack so the user can navigate
		// back
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void onDailyStudentClicked(Student student) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if (fragment == null || !(fragment instanceof StudentDailyMotionFragment)) {
			fragment = new StudentDailyMotionFragment();
		}
		Bundle args = new Bundle();
		args.putSerializable("student", student);
		fragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void onShoudongClick() {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if (fragment == null || !(fragment instanceof SendRemindFragment)) {
			fragment = new SendRemindFragment();
		}
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onStartExercise(Teacher teacher) {
		MainActivity.mIsHome = false;
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details);
		if (fragment == null || !(fragment instanceof ClassExerciseFragment)) {
			fragment = new ClassExerciseFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable("teacher", teacher);
			fragment.setArguments(bundle);
		}
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.details, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
}