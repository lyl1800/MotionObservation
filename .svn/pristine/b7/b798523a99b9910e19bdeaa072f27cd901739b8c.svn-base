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

	// 更新UI的Handler
	private MyHandler myHandler;

	private Button btn_login, btn_cancel;
	private EditText txt_username, txt_password;

	// 与Service绑定的状态
	private boolean mIsBound;
	
	// 绑定的Service
	private SocketService mBoundService;

	// 登录教师输入的工号和密码
	private String tno = "";
	private String tpwd = "";
	
	// 发送的登录请求信息
	private String sendMessage = "";

	// 服务端返回的登录结果消息
	private String returnMessage = "";
	
	// 登录进度进度框
	private static ProgressDialog progressDialog;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("LoginActivity OnDestroy");
		// Activity销毁时与服务端解除绑定
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
			System.out.println("LoginActivity解除与Service的绑定");
			//mBoundService.stopSelf(); // 停止服务
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		myHandler = new MyHandler(getMainLooper());
		// 获取控件实例
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		txt_username = (EditText) findViewById(R.id.txt_username);
		txt_password = (EditText) findViewById(R.id.txt_password);

		// 事件监听
		btn_login.setOnClickListener(new LoginBtnClickListener());

		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mBoundService != null){
					mBoundService.stopSelf(); // 停止服务
				}
				LoginActivity.this.finish();
			}
		});
	}

	// 登录按钮事件处理
	private class LoginBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 先检查用户输入的合法性
			if(checkInput()){
				// 获取用户输入的用户名和密码
				tno = txt_username.getText().toString().trim();
				tpwd = txt_password.getText().toString().trim();
				tno = "20120004";
				tpwd = "123456";
				
				// 对密码进行二次MD5加密
				String encryptPwd = MD5(MD5(tpwd));
				sendMessage = "{\"REQ\":\"login_teacher\",\"DAT\":{\"tno\":\""
						+ tno + "\",\"tpwd\":\"" + encryptPwd + "\"}}";
				System.out.println("message:" + sendMessage);
				
				// 显示登录进度
				progressDialog = new ProgressDialog(LoginActivity.this);
				progressDialog.setTitle("登录");
				progressDialog.setMessage("正在登录");
				progressDialog.show();
				
				Intent intent = new Intent(LoginActivity.this, SocketService.class);
				
				if (mBoundService == null) {
					// 启动服务
					startService(intent);
					// 将当前Activity与Service绑定
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					// 若与服务端的会话关闭，则重新与服务端建立连接
					if(mBoundService.getSessionClosed()){
						mBoundService.startConnect();
					}
					// 开始发送登录请求消息
					mBoundService.startSendMessage(sendMessage);
					// 设置Service获得服务端返回消息后的消息处理器
					mBoundService.setUpdateClientHandler(myHandler);
				}
			}
		}
	}

	// 与Service的连接对象
	private ServiceConnection mConnection = new ServiceConnection() {
		// 当前Activity与Service意外失去连接后(如Service崩溃或被kill终止时)调用
		// 客户端解除绑定Service并不调用此方法
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
			mIsBound = false;
			System.out.println("LoginActivity与Service的连接意外终止");
		}

		// 当前Activity与Service绑定后调用(onBind()后触发)
		// 接收Service返回的IBinder对象
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((SocketService.LocalBinder) service).getService();
			mIsBound = true;
			System.out.println("LoginActivity与Service绑定成功");
			// 若是连接未开启，需要请求创建与网络服务端的连接
			if (mBoundService.getSessionClosed()) {
				mBoundService.startConnect();
			}
			// 开始发送登录请求消息
			mBoundService.startSendMessage(sendMessage);
			// 设置Service获得服务端返回消息后的消息处理器
			mBoundService.setUpdateClientHandler(myHandler);
		}
	};
	
	public static void cancelDialog(){
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.cancel();
		}
	}

	/**
	 * 检查用户输入的合法性
	 * @return
	 * 			输入是否合法的标志
	 */
	private boolean checkInput() {
		if (!TextUtils.isEmpty(txt_username.getText())) {
			if(TextUtils.isEmpty(txt_password.getText())){
				Toast.makeText(this, "请输入密码!", Toast.LENGTH_SHORT).show();
				txt_password.selectAll();
				txt_password.requestFocus();
				return false;
			}
			return true;
		} else {
			Toast.makeText(this, "请输入用户名!", Toast.LENGTH_SHORT).show();
			txt_password.selectAll();
			txt_username.requestFocus();
			return false;
		}
	}
	
	// 用于处理服务端返回的登录结果
	class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// 接收服务端的反馈信息并进行登录判定
			if (msg.obj != null) {
				progressDialog.cancel();
				returnMessage = msg.obj.toString();
				System.out.println("客户端接收到的消息:" + returnMessage);
				Teacher teacher = JSONParser.getLoginTeacher(returnMessage);
				if(teacher != null){
					teacher.setTno(tno);
					if(teacher.loginResult == 0){
						Toast.makeText(LoginActivity.this, "登录成功",
								Toast.LENGTH_SHORT).show();
						btn_login.setBackgroundResource(R.drawable.login_btn2);
						Intent intent = new Intent();
						intent.putExtra("loginTeacher", teacher);
						intent.setClass(LoginActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					} else if (teacher.loginResult == 1) {
						Toast.makeText(LoginActivity.this, "该用户不存在, 请重试!",
								Toast.LENGTH_SHORT).show();
						txt_username.setText("");
						txt_password.setText("");
						txt_username.requestFocus();
					} else if (teacher.loginResult == 2) {
						Toast.makeText(LoginActivity.this, "密码错误!",
								Toast.LENGTH_SHORT).show();
						txt_password.setText("");
						txt_password.requestFocus();
					}
				}
			}
		}
	}

	/**
	 * MD5加密
	 * 
	 * @param s
	 *            待加密的字符串
	 * @return MD5加密后的密文
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
