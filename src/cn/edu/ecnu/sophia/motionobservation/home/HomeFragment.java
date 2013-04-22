package cn.edu.ecnu.sophia.motionobservation.home;

import java.util.ArrayList;
import java.util.List;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;

import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class HomeFragment extends Fragment {
	
	private ViewPager viewPager;
	private ViewGroup vg_dots;
	
	private static Context context;
	
	// 当前登录的教师
	private static Teacher teacher;

	// 首页图片数组
	private ImageView[] dotViews = null;
	private int[] pics = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
			R.drawable.pic4, R.drawable.pic5};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("teacher", teacher);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home, container, false);
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
		
		// 初始化首页图片
		initViewPager();

		MainActivity.setIsHome(true);
		MainActivity.tv_details_title.setText("首页");

		// 获取当前教师
		Bundle bundle = getArguments();
		if (bundle != null) {
			teacher = (Teacher) bundle.getSerializable("teacher");
		}
		
		if (savedInstanceState != null) {
			teacher = (Teacher) savedInstanceState.getSerializable("teacher");
		}
		
		// 引入公告Fragment
		Fragment announcementFragment = getFragmentManager().findFragmentById(
				R.id.lv_announcement_fragment);
		if (announcementFragment == null) {
			announcementFragment = new AnnouncementFragment();
			Bundle bundle1 = new Bundle();
			bundle1.putSerializable("teacher", teacher);
			announcementFragment.setArguments(bundle1);
			FragmentTransaction fTransaction = getChildFragmentManager()
			.beginTransaction();
			fTransaction.replace(R.id.lv_announcement_fragment, announcementFragment);
			fTransaction.addToBackStack(null);
			fTransaction.commit();
		}
		
		// 引入新闻Fragment
		Fragment newsFragment = getFragmentManager().findFragmentById(
				R.id.lv_news_fragment);
		if (newsFragment == null) {
			newsFragment = new NewsFragment();
			FragmentTransaction fTransaction = getChildFragmentManager()
			.beginTransaction();
			fTransaction.replace(R.id.lv_news_fragment, newsFragment);
			fTransaction.addToBackStack(null);
			fTransaction.commit();
		}
	}

	private void initViewPager() {
		viewPager = (ViewPager) getActivity().findViewById(R.id.viewPager);
		vg_dots = (ViewGroup) getActivity().findViewById(R.id.dots);
		
		final List<View> viewsList = new ArrayList<View>();
		
		//设置ViewPager图片源
		for (int i = 0; i < pics.length; i++) {
			ImageView imageView = new ImageView(context);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setImageResource(pics[i]);
			//imageView.setLayoutParams(new LayoutParams(100, 70));
			viewsList.add(imageView);
		}
		
		// 设置图片指示器源
		dotViews = new ImageView[viewsList.size()];
		for (int i = 0; i < dotViews.length; i++) {
			ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(new LayoutParams(15, 15));
			imageView.setPadding(15, 5, 15, 5);
			dotViews[i] = imageView;
			if (i == 0) {
				dotViews[i].setBackgroundResource(R.drawable.banner_dian_focus);
			} else {
				dotViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
			}
			vg_dots.addView(dotViews[i]);
		}
		
		// 设置ViewPager数据及事件侦听
		viewPager.setAdapter(new MyPagerAdapter(viewsList));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < dotViews.length; i++) {
					if (i == position) {
						dotViews[i].setBackgroundResource(R.drawable.banner_dian_focus);
						//((ImageView)(viewsList.get(i))).setAlpha(200);
					} else {
						dotViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
						//((ImageView)(viewsList.get(i))).setAlpha(30);
					}
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
		viewPager.setCurrentItem(0);
		
	}
	
	private final class MyPagerAdapter extends PagerAdapter {
		private List<View> views = null;

		public MyPagerAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(views.get(position), 0);
			return views.get(position);
		}

		@Override
		public boolean isViewFromObject(View container, Object object) {
			return container == object;
		}

	}
}
