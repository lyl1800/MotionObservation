package cn.edu.ecnu.sophia.motionobservation.motion.daily;

import java.text.DecimalFormat;
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
import cn.edu.ecnu.sophia.motionobservation.model.Student;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
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

public class StudentDailyMotionFragment extends Fragment {

	// 与Service绑定的状态
	private boolean mIsBound;
	// 绑定的Service
	private SocketService mBoundService;
	// 主线程消息处理对象
	private Handler updateStudentMotionHandler;
	// 自定义时段字符串变量
	private static final String[] choose = { "本日", "本周", "本月" };
	// 对“本年，本月，本日”的时间选项卡
	private Spinner spinner;
	// 时间选项卡的适配器
	private SpinnerAdapter adapter;

	private TextView tv_id, // 设备id
			tv_remain_power, // 剩余电量
			tv_step_number, // 步数
			tv_weigth, // 体重
			tv_current_student_energy,// 学生能量消耗
			tv_current_class_energy; // 班级平均能量消耗
	// 折线图
	private LinearLayout lineChart;
	// 画板
	private Context context;
	// 发送提醒的按钮
	//private Button btn_send_remind;
	// 折线图中两条线的标记
	private String title[] = { "个人能量消耗", "班级平均能量消耗" };
	// 画图
	private GraphicalView chart;
	// 两条折线图
	private XYSeries seriesPerson = new XYSeries(title[0]),
			seriesClass = new XYSeries(title[1]);
	// 折线图上点的数据集合
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	
	private int max_y = 5; // 当前数据中y轴最大值
	// 两条折线的颜色
	private int color[] = { Color.RED, Color.BLUE };
	// 折线拐点的样式
	private PointStyle style = PointStyle.CIRCLE;
	// 当前显示的学生
	private Student curStudent;
	// 发送的登录请求信息
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	// 服务端返回的登录结果消息
	private String returnMessage = "";
	
	// 存储静态统计数据的数组
	private double[] sStuData, sClassAvgData;
	private int[] sSteps;
	
	public static OnRemindBtnClickListener mCallback;
	
	public interface OnRemindBtnClickListener{
		public void onRemindBtnClicked(Student student);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnRemindBtnClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement OnRemindBtnClickListener");
		}
	}

	@Override
	/**
	 * Creates a Timer
	 */
	public void onDestroy() {
		super.onDestroy();
		System.out.println("StudentDailyMotionFragment OnDestroy");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentDailyMotionFragment解除与Service的绑定");
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("StudentDailyMotionFragment onStop");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentDailyMotionFragment解除与Service的绑定");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null) {
			curStudent = (Student) bundle.getSerializable("student");
		}
		context = getActivity();
		// 下面实例化各个控件
		lineChart = (LinearLayout) getActivity().findViewById(R.id.lineChart);
		tv_id = (TextView) getActivity().findViewById(R.id.tv_id);
		tv_remain_power = (TextView) getActivity().findViewById(
				R.id.tv_remain_power);
		tv_step_number = (TextView) getActivity().findViewById(
				R.id.tv_step_number);
		tv_weigth = (TextView) getActivity().findViewById(R.id.tv_weight);
		tv_current_student_energy = (TextView) getActivity().findViewById(
				R.id.tv_current_student_power);
		tv_current_class_energy = (TextView) getActivity().findViewById(
				R.id.tv_current_class_power);
		/*btn_send_remind = (Button) getActivity().findViewById(
				R.id.btn_send_remind);*/
		spinner = (Spinner) getActivity().findViewById(R.id.spn_choosetime);
		
		//发送提醒按钮的监听器
		/*btn_send_remind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onRemindBtnClicked(curStudent);
			}
		});*/
		// 将可选内容与ArrayAdapter连接起来
		adapter = new SpinnerAdapter(getActivity(),
				android.R.layout.simple_spinner_item, choose);
		// 设置弹出下拉列表风格
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// 将adapter添加到spinner中
		spinner.setAdapter(adapter);
		// 添加spinner事件监听
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		// 设置默认值
		spinner.setVisibility(View.VISIBLE);
		// 这里的Handler实例将配合下面的Timer实例，完成定时更新学生运动数据的功能
		updateStudentMotionHandler = new UpdateStudentMotionHandler(
				Looper.getMainLooper());

		Intent intent = new Intent(getActivity(), SocketService.class);
		// 先打开Service, 然后将当前Activity与该Service绑定
		getActivity().startService(intent);
		getActivity()
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// 如果当前模式是实时模式,则启动定时器实时更新数据
		if (true) {
			spinner.setSelection(0);
		}
		// 将点集添加到数据集中
		mDataset.addSeries(seriesPerson);
		mDataset.addSeries(seriesClass);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.student_daily_motion, container, false);
	}

	/**
	 * Service的连接对象
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		// 当前Activity与Service意外失去连接后(如Service终止)调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("StudentMotionActivity解除与Service绑定");
		}

		// 当前Activity与Service绑定后调用(onBind()后触发)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("StudentMotionActivity与Service绑定成功");
			// 连接服务端
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}

			// 获取学生详细信息
			sendMessage = "{\"REQ\":\"student_detail\",\"DAT\":{\"sid\":["
					+ curStudent.getSid() + "]}}";
			// 开始发送请求消息
			mBoundService.startSendMessage(sendMessage);
			// 设置Service获得服务端返回消息后的消息处理器
			mBoundService.setUpdateClientHandler(updateStudentMotionHandler);
		}
	};

	/**
	 * 更新运动数据的Handler
	 */
	private class UpdateStudentMotionHandler extends Handler {
		public UpdateStudentMotionHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DecimalFormat df = new DecimalFormat("0.00");
			if (msg.obj != null) {
				returnMessage = msg.obj.toString();
				System.out.println("客户端接收到的消息:" + returnMessage);
				if (returnMessage.equals("updateChart")) {
					
				} else {
					String res = JSONParser.getResponseString(returnMessage);
					if ("curDay_student".equals(res)) {
						max_y = 0;
						// 获得学生本日数据
						Student student = JSONParser
								.getStudentCurDayData(returnMessage);
						sSteps = student.getSteps();
						sStuData = student.getRealdata();
						sClassAvgData = student.getClassAvgEnergy();

						int sumSteps = 0;
						for (int i = 0; i < sSteps.length; i++) {
							sumSteps += sSteps[i];
						}

						double sumStuData = 0.0, sumClassAvgData = 0.0;
						for (int i = 0; i < sStuData.length; i++) {
							sumStuData += sStuData[i];
							sumClassAvgData += sClassAvgData[i];
						}

						// 更新图像
						tv_step_number.setText("" + sumSteps);
						tv_current_student_energy
								.setText(df.format(sumStuData));
						tv_current_class_energy.setText(df
								.format(sumClassAvgData));

						int number = sStuData.length;
						XYMultipleSeriesDataset mDataset = getLineDemoDataset(number);
						XYMultipleSeriesRenderer renderer = getLineDemoRenderer(
								color, style);
						setChartSettings(renderer, "X", "Y", 1, number, 0,
								max_y + 20, "时间/小时");
						chart = ChartFactory.getLineChartView(context,
								mDataset, renderer);

						renderer.setPanEnabled(false);
						renderer.setXLabels(number);

						lineChart.removeAllViews();
						lineChart.addView(chart, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
					} else if ("curWeek_student".equals(res)) {
						max_y = 0;
						// 获得学生本周数据
						Student student = JSONParser
								.getStudentCurWeekData(returnMessage);
						sSteps = student.getSteps();
						sStuData = student.getRealdata();
						sClassAvgData = student.getClassAvgEnergy();

						int sumSteps = 0;
						for (int i = 0; i < sSteps.length; i++) {
							sumSteps += sSteps[i];
						}

						double sumStuData = 0.0, sumClassAvgData = 0.0;
						for (int i = 0; i < sStuData.length; i++) {
							sumStuData += sStuData[i];
							sumClassAvgData += sClassAvgData[i];
						}

						// 更新图像
						tv_step_number.setText("" + sumSteps);
						tv_current_student_energy
								.setText(df.format(sumStuData));
						tv_current_class_energy.setText(df
								.format(sumClassAvgData));

						int number = sStuData.length;
						XYMultipleSeriesDataset mDataset = getLineDemoDataset(number);
						XYMultipleSeriesRenderer renderer = getLineDemoRenderer(
								color, style);
						setChartSettings(renderer, "X", "Y", 1, number, 0,
								max_y + 20, "时间/天");
						renderer.setPanEnabled(false);
						renderer.setXLabels(number);
						chart = ChartFactory.getLineChartView(context,
								mDataset, renderer);
						lineChart.removeAllViews();
						lineChart.addView(chart, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
					} else if ("curMonth_student".equals(res)) {
						max_y = 0;
						// 获得学生本月数据
						Student student = JSONParser
								.getStudentCurMonthData(returnMessage);
						sSteps = student.getSteps();
						sStuData = student.getRealdata();
						sClassAvgData = student.getClassAvgEnergy();

						int sumSteps = 0;
						for (int i = 0; i < sSteps.length; i++) {
							sumSteps += sSteps[i];
						}

						double sumStuData = 0.0, sumClassAvgData = 0.0;
						for (int i = 0; i < sStuData.length; i++) {
							sumStuData += sStuData[i];
							sumClassAvgData += sClassAvgData[i];
						}

						// 更新图像
						tv_step_number.setText("" + sumSteps);
						tv_current_student_energy
								.setText(df.format(sumStuData));
						tv_current_class_energy.setText(df
								.format(sumClassAvgData));

						int number = sStuData.length;
						XYMultipleSeriesDataset mDataset = getLineDemoDataset(number);
						XYMultipleSeriesRenderer renderer = getLineDemoRenderer(
								color, style);
						setChartSettings(renderer, "X", "Y", 1, number, 0,
								max_y + 20, "时间/天");
						renderer.setPanEnabled(false);
						renderer.setXLabels(number);

						chart = ChartFactory.getLineChartView(context,
								mDataset, renderer);
						lineChart.removeAllViews();
						lineChart.addView(chart, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
					} else if ("student_detail".equals(res)) {
						Student student = JSONParser
								.getStudentDetail(returnMessage);

						tv_id.setText(student.getDeviceId());
						tv_remain_power.setText(student.getRemainPower() + "%");
						tv_weigth.setText("" + student.getWeight());

					} else if ("customTime_student".equals(res)) {

					}
				}
			}
		}
	}

	/**
	 * 根据控件spinner选择的对象，进行不同的绘图
	 */
	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			MainActivity.tv_details_title.setText(curStudent.getSname() + "同学"
					+ choose[position] + "锻炼能量消耗统计曲线图");
			if (choose[position] == "本日") {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"curDay_student\",\"DAT\":{\"sid\":"
						+ curStudent.getSid() + ",\"cid\":"
						+ curStudent.getPeClassid() + "}}";
				// 开始发送请求消息
				// 检查与服务端的连接，若断开则重连
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService
						.setUpdateClientHandler(updateStudentMotionHandler);

			} else if (choose[position] == "本周") {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"curWeek_student\",\"DAT\":{\"sid\":"
						+ curStudent.getSid() + ",\"cid\":"
						+ curStudent.getPeClassid() + "}}";
				// 开始发送请求消息
				// 检查与服务端的连接，若断开则重连
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService
						.setUpdateClientHandler(updateStudentMotionHandler);

			} else if (choose[position] == "本月") {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"curMonth_student\",\"DAT\":{\"sid\":"
						+ curStudent.getSid() + ",\"cid\":"
						+ curStudent.getPeClassid() + "}}";
				// 开始发送请求消息
				// 检查与服务端的连接，若断开则重连
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// 设置Service获得服务端返回消息后的消息处理器
				mBoundService
						.setUpdateClientHandler(updateStudentMotionHandler);

			}  else {

			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	}

	/**
	 * 对本日本月本年的数据进行定义
	 * 
	 * @param number
	 *            柱状图的柱形个数
	 * @return XYMultipleSeriesDataset 进行数据设置之后的柱状图
	 */
	private XYMultipleSeriesDataset getLineDemoDataset(int number) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYSeries seriesPerson = new XYSeries(title[0]); // 根据每条线的名称创建
		// x轴的数据集合
		double vX[] = new double[number];
		// y轴的数据集合
		double vY[] = new double[number];
		for (int k = 0; k < number; k++) {
			vX[k] = k+1;
			vY[k] = sStuData[k];
			if (max_y < vY[k]) {
				max_y = (int) Math.ceil(vY[k]);
			}
			seriesPerson.add(vX[k], vY[k]);
		}
		dataset.addSeries(seriesPerson);
		XYSeries seriesClass = new XYSeries(title[1]); // 根据每条线的名称创建
		double vY1[] = new double[number];
		for (int k = 0; k < number; k++) // 每条线里有几个点
		{
			vY1[k] = sClassAvgData[k];
			if (max_y < vY1[k]) {
				max_y = (int) Math.ceil(vY1[k]);
			}
			seriesClass.add(vX[k], vY1[k]);
		}
		dataset.addSeries(seriesClass);
		return dataset;
	}

	/**
	 * 对本年、本月、本日、实时的renderer进行设置
	 * 
	 * @param color
	 * @param style
	 * @return XYMultipleSeriesRenderer 对样式进行设计的之后的柱状图
	 */
	public XYMultipleSeriesRenderer getLineDemoRenderer(int color[],
			PointStyle style) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		int length = title.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color[i]);
			r.setPointStyle(style);
			r.setFillPoints(true);
			r.setLineWidth((float) 2.5);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	/**
	 * 对图表的样式进行设置
	 * 
	 * @param renderer
	 *            图表的样式
	 * @param xTitle
	 *            x轴的命名
	 * @param yTitle
	 *            y轴的命名
	 * @param xMin
	 *            x轴点的最小个数值
	 * @param xMax
	 *            x轴点的最大个数值
	 * @param yMin
	 *            y轴点的最小个数值
	 * @param yMax
	 *            y轴点的最大个数值
	 * @param XTitle
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String xTitle, String yTitle, double xMin, double xMax,
			double yMin, double yMax, String XTitle) {
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
	}


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
