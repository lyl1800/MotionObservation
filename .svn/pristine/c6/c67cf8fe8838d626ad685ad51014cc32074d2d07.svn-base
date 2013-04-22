package cn.edu.ecnu.sophia.motionobservation.motion.daily;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;
import cn.edu.ecnu.sophia.motionobservation.model.PeClass;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ClassDailyMotionFragment extends Fragment {
	
	// 与Service绑定的状态
	private boolean mIsBound;
	// 绑定的Service
	private SocketService mBoundService;
	// 主线程消息处理对象
	private Handler updateClassMotionHandler;

	private static final String[] chooseClass = { "本日", "本周", "本月"}; 
	// "自定义时段"
	private Spinner spinnerClass;
	private SpinnerAdapter adapterClass;

	private TextView tv_qualify_num, tv_disqualify_num;
	private LinearLayout layout_zxt;
	private Context context;

	private String titleClass[] = { "班级平均能量消耗" };
	private GraphicalView chartClass;
	// 点+样式
	private XYSeries seriesClass = new XYSeries(titleClass[0]);
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer rendererClass = new XYMultipleSeriesRenderer();
	private int max_y = 5, min_y = 0, realMax_y = 5;

	//
	private int addX = -1;
	private double addY;
	int[] xv = new int[400];
	int[] yv = new int[400];
	int[] yv1 = new int[400];
	private int color[] = { Color.RED };
	private PointStyle style = PointStyle.CIRCLE;

	// 当前班级
	private PeClass curClass;

	// 发送的登录请求信息
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";

	// 服务端返回的登录结果消息
	private String returnMessage = "";

	// 存储实时数据的队列
	private Queue<Double> qClassAvgRealdata;

	// 存储静态统计数据的数组
	private double[] sClassAvgData;

	// 剩余数据点集
	private int remainPoints = 0;
	// 定时器
	private Timer timer = new Timer();
	private boolean isTimerStart = false;

	public void onDestroy() {
		super.onDestroy();
		System.out.println("ClassDailyMotionFragment OnDestroy");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("ClassDailyMotionFragment解除与Service的绑定");
		}
		if (isTimerStart) {
			// 取消定时器
			timer.cancel();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("ClassDailyMotionFragment onStop");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("ClassDailyMotionFragment解除与Service的绑定");
		}
		if (isTimerStart) {
			// 取消定时器
			timer.cancel();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.class_daily_motion, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 实例化控件
		tv_qualify_num = (TextView) getActivity().findViewById(R.id.tv_qualify);
		tv_disqualify_num = (TextView) getActivity().findViewById(R.id.tv_disqualify);
		
		Bundle bundle = getArguments();
		if(bundle != null){
			curClass = (PeClass) bundle.getSerializable("curPeClass");
		}

		tv_qualify_num.setText("48人");
		tv_disqualify_num.setText("2人");

		context = getActivity().getApplicationContext();

		// 显示图表的linearlayout
		layout_zxt = (LinearLayout) getActivity().findViewById(R.id.layout_zxt);

		spinnerClass = (Spinner) getActivity().findViewById(R.id.spn_class_choosetime);

		// 将可选内容与ArrayAdapter连接起来
		adapterClass = new SpinnerAdapter(getActivity(),
				android.R.layout.simple_spinner_item, chooseClass);
		// 设置下拉列表风格
		adapterClass
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter添加到spinner中
		spinnerClass.setAdapter(adapterClass);
		// 添加spinner事件监听
		spinnerClass.setOnItemSelectedListener(new SpinnerSelectedListener());
		// 设置默认值
		spinnerClass.setVisibility(View.VISIBLE);

		// 这里的Handler实例将配合下面的Timer实例，完成定时更新班级运动数据的功能
		updateClassMotionHandler = new UpdateClassMotionHandler(
				Looper.getMainLooper());

		// 实例化存储实时数据的队列
		qClassAvgRealdata = new LinkedList<Double>();

		Intent intent = new Intent(getActivity(),
				SocketService.class);
		// 先打开Service, 然后将当前Activity与该Service绑定
		getActivity().startService(intent);
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		System.out.println("error");
		// 如果当前模式是实时模式,则启动定时器实时更新数据
		if (true) {
			spinnerClass.setSelection(0);
		}
		// 将点集添加到数据集中
		mDataset.addSeries(seriesClass);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		// 当前Activity与Service意外失去连接后(如Service终止)调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			System.out.println("ClassMotionActivity解除与Service绑定");
		}

		// 当前Activity与Service绑定后调用(onBind()后触发)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			System.out.println("ClassMotionActivity与Service绑定成功");
			// 连接服务端
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}
		}

	};

	/**
	 * 更新运动数据的Handler
	 */
	private class UpdateClassMotionHandler extends Handler {
		public UpdateClassMotionHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj != null) { // 更新班级列表
				returnMessage = msg.obj.toString();
				System.out.println("客户端接收到的消息:" + returnMessage);
				if (returnMessage.equals("updateChart")) {
					// 刷新图表
					updateChart();

				} else {
					String res = JSONParser.getResponseString(returnMessage);
					if ("curDay_class".equals(res)) {
						min_y = 0;
						max_y = 5;
						// 获得全班本日数据
						PeClass PeClass = JSONParser
								.getClassCurDayData(returnMessage);
						sClassAvgData = PeClass.getClassDayAvgEnergy();

						double sumClassAvgData = 0.0;
						for (int i = 0; i < sClassAvgData.length; i++) {
							sumClassAvgData += sClassAvgData[i];
						}

						int number = sClassAvgData.length;
						XYMultipleSeriesDataset mDataset = getLineDemoDataset(number);
						XYMultipleSeriesRenderer renderer = getLineDemoRenderer(
								color, style);
						setChartSettings(renderer, "X", "Y", 1, number, min_y,
								max_y + 20, Color.WHITE, Color.WHITE, "时间/小时");
						chartClass = ChartFactory.getLineChartView(context,
								mDataset, renderer);
						renderer.setPanEnabled(false);
						renderer.setXLabels(number);
						layout_zxt.removeAllViews();
						layout_zxt.addView(chartClass, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
					} else if ("curWeek_class".equals(res)) {
						min_y = 0;
						max_y = 5;
						// 获得班级本周数据
						PeClass class2 = JSONParser
								.getClassCurWeekData(returnMessage);
						sClassAvgData = class2.getClassWeekAvgEnergy();

						double sumClassAvgData = 0.0;
						for (int i = 0; i < sClassAvgData.length; i++) {
							sumClassAvgData += sClassAvgData[i];
						}

						int number = sClassAvgData.length;
						XYMultipleSeriesDataset mDataset = getLineDemoDataset(number);
						XYMultipleSeriesRenderer renderer = getLineDemoRenderer(
								color, style);
						setChartSettings(renderer, "X", "Y", 1, number, min_y,
								max_y + 20, Color.WHITE, Color.WHITE, "时间/天");
						renderer.setPanEnabled(false);
						renderer.setXLabels(number);
						chartClass = ChartFactory.getLineChartView(context,
								mDataset, renderer);
						layout_zxt.removeAllViews();
						layout_zxt.addView(chartClass, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
					} else if ("curMonth_class".equals(res)) {
						min_y = 0;
						max_y = 5;
						// 获得班级本月数据
						PeClass class3 = JSONParser
								.getClassCurMonthData(returnMessage);
						sClassAvgData = class3.getClassMonthAvgEnergy();

						double sumClassAvgData = 0.0;
						for (int i = 0; i < sClassAvgData.length; i++) {
							sumClassAvgData += sClassAvgData[i];
						}

						int number = sClassAvgData.length;
						XYMultipleSeriesDataset mDataset = getLineDemoDataset(number);
						XYMultipleSeriesRenderer renderer = getLineDemoRenderer(
								color, style);
						setChartSettings(renderer, "X", "Y", 1, number, min_y,
								max_y + 20, Color.WHITE, Color.WHITE, "时间/天");
						renderer.setPanEnabled(false);
						renderer.setXLabels(number);
						chartClass = ChartFactory.getLineChartView(context,
								mDataset, renderer);
						layout_zxt.removeAllViews();
						layout_zxt.addView(chartClass, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
					} else if ("customTime_student".equals(res)) {

					}

				}
			}
		}

		public XYMultipleSeriesRenderer getLineDemoRenderer(int color[],
				PointStyle style) {
			XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
			// int length = title.length;
			// for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color[0]);
			r.setPointStyle(style);
			r.setFillPoints(true);
			r.setLineWidth((float) 2.5);
			renderer.addSeriesRenderer(r);
			// }
			return renderer;
		}

		private XYMultipleSeriesDataset getLineDemoDataset(int number) {
			// max_y = 90;
			XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

			XYSeries series = new XYSeries(titleClass[0]); // 根据每条线的名称创建
			double vX[] = new double[number];
			double vY[] = new double[number];
			min_y = (int)sClassAvgData[0];
			for (int k = 0; k < number; k++) {// 每条线里有几个点
				vX[k] = k+1;
				vY[k] = sClassAvgData[k];
				if (max_y < vY[k]) {
					max_y = (int) Math.ceil(vY[k]);
				}
				if(min_y > vY[k]){
					min_y = (int) Math.floor(vY[k]);
				}
				series.add(vX[k], vY[k]);
			}
			dataset.addSeries(series);
			return dataset;
		}
	}

	class SpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			MainActivity.tv_details_title.setText(curClass.getCname()+chooseClass[position]+"锻炼能量消耗统计曲线图");
			if (chooseClass[position] == "本日") {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"curDay_class\",\"DAT\":{\"cid\":"
						+ curClass.getCid() + "}}";
				// 开始发送请求消息
				// 检查与服务端的连接，若断开则重连
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService.setUpdateClientHandler(updateClassMotionHandler);

			} else if (chooseClass[position] == "本周") {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"curWeek_class\",\"DAT\":{\"cid\":"
						+ curClass.getCid() + "}}";
				// 开始发送请求消息
				// 检查与服务端的连接，若断开则重连
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService.setUpdateClientHandler(updateClassMotionHandler);

			} else if (chooseClass[position] == "本月") {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"curMonth_class\",\"DAT\":{\"cid\":"
						+ curClass.getCid() + "}}";
				// 开始发送请求消息
				// 检查与服务端的连接，若断开则重连
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService.setUpdateClientHandler(updateClassMotionHandler);

			} else {

			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void onNothingSelected(AdapterView<?> arg0) {

	}

	private void updateChart() {

		if (!qClassAvgRealdata.isEmpty()) {
			addY = qClassAvgRealdata.poll();
			remainPoints--;
		}

		// 判断当前点集中到底有多少点,确定新产生点的X坐标
		int length = seriesClass.getItemCount();
		addX = length;

		// 将新产生的点首先加入到点集中
		seriesClass.add(addX, addY);

		for (int i = 0; i <= length; i++) {
			double temp_y = seriesClass.getY(i);
			if (realMax_y < temp_y) {
				realMax_y = (int) Math.ceil(temp_y);
			}
		}
		rendererClass.setYAxisMax((int) (realMax_y + 2));

		if (addX >= 60 && addX % 60 == 0) {
			rendererClass.setXAxisMin(addX);
			rendererClass.setXAxisMax(addX + 60);
		}

		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		chartClass.invalidate();
	}

	// 对本年、本月、本日、实时的renderer进行设置~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public XYMultipleSeriesRenderer getLineDemoRenderer(int color[],
			PointStyle style) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		int length = titleClass.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color[i]);
			r.setPointStyle(style);
			r.setFillPoints(true);
			r.setLineWidth((float) 2.5);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	// 对本年本月本日的样式进行设置~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String xTitle, String yTitle, double xMin, double xMax,
			double yMin, double yMax, int axesColor, int labelsColor,
			String XTitle)
	// renderer, "X", "Y", 0, 100, 0, 90, Color.WHITE,Color.WHITE
	{
		renderer.setChartTitle("");
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(Color.rgb(46, 139, 87));
		renderer.setLabelsColor(Color.BLACK);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.rgb(46, 139, 87));
		renderer.setXLabels(20);
		renderer.setYLabels(10);
		renderer.setXTitle(XTitle);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYTitle("能量消耗/卡路里");
		renderer.setAxisTitleTextSize(18);
		renderer.setLabelsTextSize(14);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPointSize(3);
		renderer.setLegendTextSize(18);
		renderer.setPanEnabled(true, false);
		renderer.setPanLimits(new double[] { 0, 100000, 0, 100000 });
		renderer.setZoomEnabled(false, false);
	}// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private class SpinnerAdapter extends ArrayAdapter<String> {
		Context context;
		String[] items = new String[] {};

		public SpinnerAdapter(final Context context,
				final int textViewResourceId, final String[] objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						android.R.layout.simple_dropdown_item_1line, parent,
						false);
			}

			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			tv.setText(items[position]);
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(26);
			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						android.R.layout.simple_spinner_item, parent, false);
			}

			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			tv.setText(items[position]);
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(24);
			return convertView;
		}
	}

}
