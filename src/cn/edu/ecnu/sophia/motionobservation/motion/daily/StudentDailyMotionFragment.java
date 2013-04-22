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

	// ��Service�󶨵�״̬
	private boolean mIsBound;
	// �󶨵�Service
	private SocketService mBoundService;
	// ���߳���Ϣ�������
	private Handler updateStudentMotionHandler;
	// �Զ���ʱ���ַ�������
	private static final String[] choose = { "����", "����", "����" };
	// �ԡ����꣬���£����ա���ʱ��ѡ�
	private Spinner spinner;
	// ʱ��ѡ���������
	private SpinnerAdapter adapter;

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
	//private Button btn_send_remind;
	// ����ͼ�������ߵı��
	private String title[] = { "������������", "�༶ƽ����������" };
	// ��ͼ
	private GraphicalView chart;
	// ��������ͼ
	private XYSeries seriesPerson = new XYSeries(title[0]),
			seriesClass = new XYSeries(title[1]);
	// ����ͼ�ϵ�����ݼ���
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	
	private int max_y = 5; // ��ǰ������y�����ֵ
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
	
	// �洢��̬ͳ�����ݵ�����
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
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentDailyMotionFragment�����Service�İ�");
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		System.out.println("StudentDailyMotionFragment onStop");
		// ����ʱ�����
		if (mIsBound) {
			getActivity().unbindService(mConnection);
			mIsBound = false;
			System.out.println("StudentDailyMotionFragment�����Service�İ�");
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
		// ����ʵ���������ؼ�
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
		
		//�������Ѱ�ť�ļ�����
		/*btn_send_remind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onRemindBtnClicked(curStudent);
			}
		});*/
		// ����ѡ������ArrayAdapter��������
		adapter = new SpinnerAdapter(getActivity(),
				android.R.layout.simple_spinner_item, choose);
		// ���õ��������б���
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// ��adapter��ӵ�spinner��
		spinner.setAdapter(adapter);
		// ���spinner�¼�����
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		// ����Ĭ��ֵ
		spinner.setVisibility(View.VISIBLE);
		// �����Handlerʵ������������Timerʵ������ɶ�ʱ����ѧ���˶����ݵĹ���
		updateStudentMotionHandler = new UpdateStudentMotionHandler(
				Looper.getMainLooper());

		Intent intent = new Intent(getActivity(), SocketService.class);
		// �ȴ�Service, Ȼ�󽫵�ǰActivity���Service��
		getActivity().startService(intent);
		getActivity()
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// �����ǰģʽ��ʵʱģʽ,��������ʱ��ʵʱ��������
		if (true) {
			spinner.setSelection(0);
		}
		// ���㼯��ӵ����ݼ���
		mDataset.addSeries(seriesPerson);
		mDataset.addSeries(seriesClass);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.student_daily_motion, container, false);
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
			DecimalFormat df = new DecimalFormat("0.00");
			if (msg.obj != null) {
				returnMessage = msg.obj.toString();
				System.out.println("�ͻ��˽��յ�����Ϣ:" + returnMessage);
				if (returnMessage.equals("updateChart")) {
					
				} else {
					String res = JSONParser.getResponseString(returnMessage);
					if ("curDay_student".equals(res)) {
						max_y = 0;
						// ���ѧ����������
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

						// ����ͼ��
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
								max_y + 20, "ʱ��/Сʱ");
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
						// ���ѧ����������
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

						// ����ͼ��
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
								max_y + 20, "ʱ��/��");
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
						// ���ѧ����������
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

						// ����ͼ��
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
								max_y + 20, "ʱ��/��");
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
	 * ���ݿؼ�spinnerѡ��Ķ��󣬽��в�ͬ�Ļ�ͼ
	 */
	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			MainActivity.tv_details_title.setText(curStudent.getSname() + "ͬѧ"
					+ choose[position] + "������������ͳ������ͼ");
			if (choose[position] == "����") {
				// �������ݣ�������ʵʱ��ȡ��
				sendMessage = "{\"REQ\":\"curDay_student\",\"DAT\":{\"sid\":"
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

			} else if (choose[position] == "����") {
				// �������ݣ�������ʵʱ��ȡ��
				sendMessage = "{\"REQ\":\"curWeek_student\",\"DAT\":{\"sid\":"
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

			} else if (choose[position] == "����") {
				// �������ݣ�������ʵʱ��ȡ��
				sendMessage = "{\"REQ\":\"curMonth_student\",\"DAT\":{\"sid\":"
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

			}  else {

			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	}

	/**
	 * �Ա��ձ��±�������ݽ��ж���
	 * 
	 * @param number
	 *            ��״ͼ�����θ���
	 * @return XYMultipleSeriesDataset ������������֮�����״ͼ
	 */
	private XYMultipleSeriesDataset getLineDemoDataset(int number) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYSeries seriesPerson = new XYSeries(title[0]); // ����ÿ���ߵ����ƴ���
		// x������ݼ���
		double vX[] = new double[number];
		// y������ݼ���
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
		XYSeries seriesClass = new XYSeries(title[1]); // ����ÿ���ߵ����ƴ���
		double vY1[] = new double[number];
		for (int k = 0; k < number; k++) // ÿ�������м�����
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
