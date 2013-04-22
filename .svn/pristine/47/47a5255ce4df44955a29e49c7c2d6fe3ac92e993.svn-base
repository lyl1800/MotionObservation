package cn.edu.ecnu.sophia.motionobservation.motion.classes;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StudentCurMotionFragment extends Fragment {

	// 与Service绑定的状态
	private boolean mIsBound;
	// 绑定的Service
	private SocketService mBoundService;
	// 主线程消息处理对象
	private Handler updateStudentMotionHandler;

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
	private Button btn_send_remind;
	// 折线图中两条线的标记
	private String title[] = { "个人能量消耗", "班级平均能量消耗" };
	// 画图
	private GraphicalView chart;
	// 两条折线图
	private XYSeries seriesPerson = new XYSeries(title[0]),
			seriesClass = new XYSeries(title[1]);
	// 折线图上点的数据集合
	private XYMultipleSeriesDataset mDataset;
	// 折线图上点的样式集合
	private XYMultipleSeriesRenderer renderer;
	private double addX = -1, // 更新的x轴的数据
			addYPerson, // 更新的y轴的个人数据
			addYClass; // 更新的y轴的班级平均数据
	private int realMax_y = 5; // 当前实时数据中的最大值
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
	// 存储实时数据的队列
	private Queue<Double> qStudentRealdata, qClassAvgRealdata;
	private Queue<Integer> qSteps;
	// 剩余数据点集
	private int remainPoints, newAddPoints;
	// 定时器
	private Timer timer = new Timer();
	private boolean isTimerStart = false;
	
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
	public void onDestroy() {
		super.onDestroy();
		System.out.println("StudentCurMotionFragment OnDestroy");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentCurMotionFragment解除与Service的绑定");
		}
		// 取消定时器
		if (isTimerStart) {
			timer.cancel();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("StudentCurMotionFragment onStop");
		// 销毁时解除绑定
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentCurMotionFragment解除与Service的绑定");
		}
		// 取消定时器
		if (isTimerStart) {
			timer.cancel();
			timer = null;
		}
	}

	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			// 若剩余点数不足3个,则向服务端发送请求
			if (remainPoints < 3) {
				// 更新数据（从网络实时获取）
				sendMessage = "{\"REQ\":\"student_realdata\",\"DAT\":{\"sid\":"
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

			}
			// 通知主线程更新图表
			Message message = new Message();
			message.obj = "updateChart";
			updateStudentMotionHandler.sendMessage(message);
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null) {
			curStudent = (Student) bundle.getSerializable("student");
		}
		MainActivity.tv_details_title.setText(curStudent.getSname()+"同学课堂锻炼能量消耗统计曲线图(单位：卡路里/秒)");
		context = getActivity();
		
		// 实例化各个控件
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
		btn_send_remind = (Button) getActivity().findViewById(
				R.id.btn_send_remind);
		/**
		 * 发送提醒按钮的监听器
		 */
		btn_send_remind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onRemindBtnClicked(curStudent);
			}
		});

		// 这里的Handler实例将配合下面的Timer实例，完成定时更新学生运动数据的功能
		updateStudentMotionHandler = new UpdateStudentMotionHandler(
				Looper.getMainLooper());
		
		// 实例化存储实时数据的队列
		qStudentRealdata = new LinkedList<Double>();
		qClassAvgRealdata = new LinkedList<Double>();
		qSteps = new LinkedList<Integer>();

		Intent intent = new Intent(getActivity(), SocketService.class);
		// 先打开Service, 然后将当前Activity与该Service绑定
		getActivity().startService(intent);
		getActivity()
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// 将点集添加到数据集中
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(seriesPerson);
		mDataset.addSeries(seriesClass);
		
		initChartSetting();
		timer.schedule(task, 1000, 1000);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.student_cur_motion, container, false);
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
			//DecimalFormat df = new DecimalFormat("0.00");
			if (msg.obj != null) {
				returnMessage = msg.obj.toString();
				System.out.println("客户端接收到的消息:" + returnMessage);
				if (returnMessage.equals("updateChart")) {
					// 刷新图表
					updateChart();
				} else {
					String res = JSONParser.getResponseString(returnMessage);
					if ("student_realdata".equals(res)) {
						// 获得班级实时数据
						Student student = JSONParser
								.getStudentRealdata(returnMessage);
						int[] steps = student.getSteps();
						double[] studentRealdata = student.getRealdata();
						double[] classAvgRealdata = student.getClassAvgEnergy();

						// 更新新增点数和剩余点数
						newAddPoints = studentRealdata.length;
						remainPoints += newAddPoints;

						// 将新得到的数据添加到数据队列中
						for (int i = 0; i < newAddPoints; i++) {
							qStudentRealdata.offer(studentRealdata[i]);
							qClassAvgRealdata.offer(classAvgRealdata[i]);
							qSteps.offer(steps[i]);
						}
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

	private void initChartSetting(){
		// 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		renderer = getLineDemoRenderer(color, style);

		// 设置好图表的样式
		setChartSettings(renderer, "X", "Y", 0, 60, 0, realMax_y + 2,
				"时间/秒");

		// 生成图表
		chart = ChartFactory.getLineChartView(context, mDataset,
				renderer);

		lineChart.removeAllViews();

		// 将图表添加到布局中去
		lineChart.addView(chart, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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

	/**
	 * 实时图表的绘制
	 */
	private void updateChart() {

		if (!qStudentRealdata.isEmpty()) {
			addYPerson = qStudentRealdata.poll();
			addYClass = qClassAvgRealdata.poll();
			tv_step_number.setText("" + qSteps.poll());
			tv_current_student_energy.setText("" + addYPerson);
			tv_current_class_energy.setText("" + addYClass);
			remainPoints--;
		}
		// 判断当前点集中到底有多少点,确定新产生点的X坐标
		int length = seriesPerson.getItemCount();
		addX = length;
		// 将新产生的点首先加入到点集中
		seriesPerson.add(addX, addYPerson);
		seriesClass.add(addX, addYClass);
		for (int i = 0; i <= length; i++) {
			double temp_y = seriesPerson.getY(i);
			double temp_y1 = seriesClass.getY(i);
			if (realMax_y < temp_y) {
				realMax_y = (int) Math.ceil(temp_y);
			}
			if (realMax_y < temp_y1) {
				realMax_y = (int) Math.ceil(temp_y1);
			}
		}
		renderer.setYAxisMax((int) (realMax_y + 2));
		if (addX >= 60 && addX % 60 == 0) {
			renderer.setXAxisMin(addX);
			renderer.setXAxisMax(addX + 60);
		}
		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()
		chart.invalidate();
	}
}
