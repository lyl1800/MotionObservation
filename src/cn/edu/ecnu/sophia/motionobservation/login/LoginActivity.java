package cn.edu.ecnu.sophia.motionobservation.login;

import java.security.MessageDigest;

import cn.edu.ecnu.sophia.motionobservation.MainActivity;
import cn.edu.ecnu.sophia.motionobservation.R;
import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;
import cn.edu.ecnu.sophia.motionobservation.model.Teacher;
import cn.edu.ecnu.sophia.motionobservation.net.SocketService;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	// ����UI��Handler
	private MyHandler myHandler;

	private Button btn_login, btn_cancel;
	private EditText txt_username, txt_password;

	// ��Service�󶨵�״̬
	private boolean mIsBound;
	
	// �󶨵�Service
	private SocketService mBoundService;

	// ��¼��ʦ����Ĺ��ź�����
	private String tno = "";
	private String tpwd = "";
	
	// ���͵ĵ�¼������Ϣ
	private String sendMessage = "";

	// ����˷��صĵ�¼�����Ϣ
	private String returnMessage = "";
	
	// ��¼���Ƚ��ȿ�
	private static ProgressDialog progressDialog;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("LoginActivity OnDestroy");
		// Activity����ʱ�����˽����
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
			System.out.println("LoginActivity�����Service�İ�");
			//mBoundService.stopSelf(); // ֹͣ����
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		myHandler = new MyHandler(getMainLooper());
		// ��ȡ�ؼ�ʵ��
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		txt_username = (EditText) findViewById(R.id.txt_username);
		txt_password = (EditText) findViewById(R.id.txt_password);

		// �¼�����
		btn_login.setOnClickListener(new LoginBtnClickListener());

		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mBoundService != null){
					mBoundService.stopSelf(); // ֹͣ����
				}
				LoginActivity.this.finish();
			}
		});
	}

	// ��¼��ť�¼�����
	private class LoginBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// �ȼ���û�����ĺϷ���
			if(checkInput()){
				// ��ȡ�û�������û���������
				tno = txt_username.getText().toString().trim();
				tpwd = txt_password.getText().toString().trim();
				tno = "20120004";
				tpwd = "123456";
				
				// ��������ж���MD5����
				String encryptPwd = MD5(MD5(tpwd));
				sendMessage = "{\"REQ\":\"login_teacher\",\"DAT\":{\"tno\":\""
						+ tno + "\",\"tpwd\":\"" + encryptPwd + "\"}}";
				System.out.println("message:" + sendMessage);
				
				// ��ʾ��¼����
				progressDialog = new ProgressDialog(LoginActivity.this);
				progressDialog.setTitle("��¼");
				progressDialog.setMessage("���ڵ�¼");
				progressDialog.show();
				
				Intent intent = new Intent(LoginActivity.this, SocketService.class);
				
				if (mBoundService == null) {
					// ��������
					startService(intent);
					// ����ǰActivity��Service��
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					// �������˵ĻỰ�رգ������������˽�������
					if(mBoundService.getSessionClosed()){
						mBoundService.startConnect();
					}
					// ��ʼ���͵�¼������Ϣ
					mBoundService.startSendMessage(sendMessage);
					// ����Service��÷���˷�����Ϣ�����Ϣ������
					mBoundService.setUpdateClientHandler(myHandler);
				}
			}
		}
	}

	// ��Service�����Ӷ���
	private ServiceConnection mConnection = new ServiceConnection() {
		// ��ǰActivity��Service����ʧȥ���Ӻ�(��Service������kill��ֹʱ)����
		// �ͻ��˽����Service�������ô˷���
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("LoginActivity��Service������������ֹ");
		}

		// ��ǰActivity��Service�󶨺����(onBind()�󴥷�)
		// ����Service���ص�IBinder����
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("LoginActivity��Service�󶨳ɹ�");
			// ��������δ��������Ҫ���󴴽����������˵�����
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}
			// ��ʼ���͵�¼������Ϣ
			mBoundService.startSendMessage(sendMessage);
			// ����Service��÷���˷�����Ϣ�����Ϣ������
			mBoundService.setUpdateClientHandler(myHandler);
		}
	};
	
	public static void cancelDialog(){
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.cancel();
		}
	}

	/**
	 * ����û�����ĺϷ���
	 * @return
	 * 			�����Ƿ�Ϸ��ı�־
	 */
	private boolean checkInput() {
		if (!TextUtils.isEmpty(txt_username.getText())) {
			if(TextUtils.isEmpty(txt_password.getText())){
				Toast.makeText(this, "����������!", Toast.LENGTH_SHORT).show();
				txt_password.selectAll();
				txt_password.requestFocus();
				return false;
			}
			return true;
		} else {
			Toast.makeText(this, "�������û���!", Toast.LENGTH_SHORT).show();
			txt_password.selectAll();
			txt_username.requestFocus();
			return false;
		}
	}
	
	// ���ڴ������˷��صĵ�¼���
	class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// ���շ���˵ķ�����Ϣ�����е�¼�ж�
			if (msg.obj != null) {
				progressDialog.cancel();
				returnMessage = msg.obj.toString();
				System.out.println("�ͻ��˽��յ�����Ϣ:" + returnMessage);
				Teacher teacher = JSONParser.getLoginTeacher(returnMessage);
				if(teacher != null){
					teacher.setTno(tno);
					if(teacher.loginResult == 0){
						Toast.makeText(LoginActivity.this, "��¼�ɹ�",
								Toast.LENGTH_SHORT).show();
						btn_login.setBackgroundResource(R.drawable.login_btn2);
						Intent intent = new Intent();
						intent.putExtra("loginTeacher", teacher);
						intent.setClass(LoginActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					} else if (teacher.loginResult == 1) {
						Toast.makeText(LoginActivity.this, "���û�������, ������!",
								Toast.LENGTH_SHORT).show();
						txt_username.setText("");
						txt_password.setText("");
						txt_username.requestFocus();
					} else if (teacher.loginResult == 2) {
						Toast.makeText(LoginActivity.this, "�������!",
								Toast.LENGTH_SHORT).show();
						txt_password.setText("");
						txt_password.requestFocus();
					}
				}
			}
		}
	}

	/**
	 * MD5����
	 * 
	 * @param s
	 *            �����ܵ��ַ���
	 * @return MD5���ܺ������
	 */
	private String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

}
