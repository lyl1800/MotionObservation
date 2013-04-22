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

	// ��Service�󶨵�״̬
	private boolean mIsBound;
	// �󶨵�Service
	private SocketService mBoundService;
	// ���߳���Ϣ�������
	private Handler updateStudentMotionHandler;

	private TextView tv_id, // �豸id
			tv_remain_power, // ʣ�����
			tv_step_number, // ����
			tv_weigth, // ����
			tv_current_student_energy,// ѧ����������
			tv_current_class_energy; // �༶ƽ����������
	// ����ͼ
	private LinearLayout lineChart;
	// ����
	private Context context;
	// �������ѵİ�ť
	private Button btn_send_remind;
	// ����ͼ�������ߵı��
	private String title[] = { "������������", "�༶ƽ����������" };
	// ��ͼ
	private GraphicalView chart;
	// ��������ͼ
	private XYSeries seriesPerson = new XYSeries(title[0]),
			seriesClass = new XYSeries(title[1]);
	// ����ͼ�ϵ�����ݼ���
	private XYMultipleSeriesDataset mDataset;
	// ����ͼ�ϵ����ʽ����
	private XYMultipleSeriesRenderer renderer;
	private double addX = -1, // ���µ�x�������
			addYPerson, // ���µ�y��ĸ�������
			addYClass; // ���µ�y��İ༶ƽ������
	private int realMax_y = 5; // ��ǰʵʱ�����е����ֵ
	// �������ߵ���ɫ
	private int color[] = { Color.RED, Color.BLUE };
	// ���߹յ����ʽ
	private PointStyle style = PointStyle.CIRCLE;
	// ��ǰ��ʾ��ѧ��
	private Student curStudent;
	// ���͵ĵ�¼������Ϣ
	private String sendMessage = "{\"REQ\":\"beat\",\"DAT\":{}}";
	// ����˷��صĵ�¼�����Ϣ
	private String returnMessage = "";
	// �洢ʵʱ���ݵĶ���
	private Queue<Double> qStudentRealdata, qClassAvgRealdata;
	private Queue<Integer> qSteps;
	// ʣ�����ݵ㼯
	private int remainPoints, newAddPoints;
	// ��ʱ��
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
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentCurMotionFragment�����Service�İ�");
		}
		// ȡ����ʱ��
		if (isTimerStart) {
			timer.cancel();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("StudentCurMotionFragment onStop");
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentCurMotionFragment�����Service�İ�");
		}
		// ȡ����ʱ��
		if (isTimerStart) {
			timer.cancel();
			timer = null;
		}
	}

	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			// ��ʣ���������3��,�������˷�������
			if (remainPoints < 3) {
				// �������ݣ�������ʵʱ��ȡ��
				sendMessage = "{\"REQ\":\"student_realdata\",\"DAT\":{\"sid\":"
						+ curStudent.getSid() + ",\"cid\":"
						+ curStudent.getPeClassid() + "}}";
				// ��ʼ����������Ϣ
				// ��������˵����ӣ����Ͽ�������
				if (mBoundService.getSessionClosed()) {
					mBoundService.startConnect();
				}
				mBoundService.startSendMessage(sendMessage);
				// ����Service��÷���˷�����Ϣ�����Ϣ������
				mBoundService
						.setUpdateClientHandler(updateStudentMotionHandler);

			}
			// ֪ͨ���̸߳���ͼ��
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
		MainActivity.tv_details_title.setText(curStudent.getSname()+"ͬѧ���ö�����������ͳ������ͼ(��λ����·��/��)");
		context = getActivity();
		
		// ʵ���������ؼ�
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
		 * �������Ѱ�ť�ļ�����
		 */
		btn_send_remind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onRemindBtnClicked(curStudent);
			}
		});

		// �����Handlerʵ������������Timerʵ������ɶ�ʱ����ѧ���˶����ݵĹ���
		updateStudentMotionHandler = new UpdateStudentMotionHandler(
				Looper.getMainLooper());
		
		// ʵ�����洢ʵʱ���ݵĶ���
		qStudentRealdata = new LinkedList<Double>();
		qClassAvgRealdata = new LinkedList<Double>();
		qSteps = new LinkedList<Integer>();

		Intent intent = new Intent(getActivity(), SocketService.class);
		// �ȴ�Service, Ȼ�󽫵�ǰActivity���Service��
		getActivity().startService(intent);
		getActivity()
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// ���㼯��ӵ����ݼ���
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
	 * Service�����Ӷ���
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		// ��ǰActivity��Service����ʧȥ���Ӻ�(��Service��ֹ)����
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("StudentMotionActivity�����Service��");
		}

		// ��ǰActivity��Service�󶨺����(onBind()�󴥷�)
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("StudentMotionActivity��Service�󶨳ɹ�");
			// ���ӷ����
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}

			// ��ȡѧ����ϸ��Ϣ
			sendMessage = "{\"REQ\":\"student_detail\",\"DAT\":{\"sid\":["
					+ curStudent.getSid() + "]}}";
			// ��ʼ����������Ϣ
			mBoundService.startSendMessage(sendMessage);
			// ����Service��÷���˷�����Ϣ�����Ϣ������
			mBoundService.setUpdateClientHandler(updateStudentMotionHandler);
		}
	};

	/**
	 * �����˶����ݵ�Handler
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
				System.out.println("�ͻ��˽��յ�����Ϣ:" + returnMessage);
				if (returnMessage.equals("updateChart")) {
					// ˢ��ͼ��
					updateChart();
				} else {
					String res = JSONParser.getResponseString(returnMessage);
					if ("student_realdata".equals(res)) {
						// ��ð༶ʵʱ����
						Student student = JSONParser
								.getStudentRealdata(returnMessage);
						int[] steps = student.getSteps();
						double[] studentRealdata = student.getRealdata();
						double[] classAvgRealdata = student.getClassAvgEnergy();

						// ��������������ʣ�����
						newAddPoints = studentRealdata.length;
						remainPoints += newAddPoints;

						// ���µõ���������ӵ����ݶ�����
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
		// ���¶������ߵ���ʽ�����Եȵȵ����ã�renderer�൱��һ��������ͼ������Ⱦ�ľ��
		renderer = getLineDemoRenderer(color, style);

		// ���ú�ͼ�����ʽ
		setChartSettings(renderer, "X", "Y", 0, 60, 0, realMax_y + 2,
				"ʱ��/��");

		// ����ͼ��
		chart = ChartFactory.getLineChartView(context, mDataset,
				renderer);

		lineChart.removeAllViews();

		// ��ͼ����ӵ�������ȥ
		lineChart.addView(chart, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	/**
	 * �Ա��ꡢ���¡����ա�ʵʱ��renderer��������
	 * 
	 * @param color
	 * @param style
	 * @return XYMultipleSeriesRenderer ����ʽ������Ƶ�֮�����״ͼ
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
	 * ��ͼ�����ʽ��������
	 * 
	 * @param renderer
	 *            ͼ�����ʽ
	 * @param xTitle
	 *            x�������
	 * @param yTitle
	 *            y�������
	 * @param xMin
	 *            x������С����ֵ
	 * @param xMax
	 *            x����������ֵ
	 * @param yMin
	 *            y������С����ֵ
	 * @param yMax
	 *            y����������ֵ
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
		renderer.setYTitle("��������/��·��");
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
	 * ʵʱͼ��Ļ���
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
		// �жϵ�ǰ�㼯�е����ж��ٵ�,ȷ���²������X����
		int length = seriesPerson.getItemCount();
		addX = length;
		// ���²����ĵ����ȼ��뵽�㼯��
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
		// ��ͼ���£�û����һ�������߲�����ֶ�̬
		// ����ڷ�UI���߳��У���Ҫ����postInvalidate()
		chart.invalidate();
	}
}
