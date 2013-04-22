package cn.edu.ecnu.sophia.motionobservation.net;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import cn.edu.ecnu.sophia.motionobservation.login.LoginActivity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

public class SocketService extends Service {

	public final static int REQUEST_CONNECTION = 0;
	public final static int SEND_MESSAGE = 1;
	public final static int HANDLE_MESSAGE = 2;
	
	// 启动的服务Id
	private int startServiceId = -1;
	
	// 接收客户端交互信息的对象
	private final IBinder mBinder = new LocalBinder();

	private ServiceHandler mServiceHandler = null;
	private Looper mServiceLooper;

	// 连接器
	private static IoConnector connector;
	// 连接Session对象
	private static IoSession session = null;
	private static boolean mIsSessionClosed = true;
	
	// 更新客户的Handler
	private Handler updateClientHandler;

	/**
	 * 供客户端访问该服务的类
	 */
	public class LocalBinder extends Binder {
		// 返回本地服务的实例
		public SocketService getService() {
			return SocketService.this;
		}
	}
	
	@Override
	public void onCreate() {
		System.out.println("创建服务...");
		// 开启子线程以进行联网操作
		HandlerThread handlerThread = new HandlerThread("ServiceThread",
				Process.THREAD_PRIORITY_BACKGROUND);
		handlerThread.start();

		// 获得子线程的消息循环
		mServiceLooper = handlerThread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		// 创建与服务端的连接对象
		connector = new NioSocketConnector();
		// 设置读取数据的缓冲区大小
		connector.getSessionConfig().setReadBufferSize(Config.BUFFER_SIZE);
		// 设置连接闲置时间
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,
				Config.IDLE_TIME);
		
		// 设置编码过滤器
		PrefixedStringCodecFactory psc = new PrefixedStringCodecFactory(
				Charset.forName("UTF-8"));
		// 设置前导字符长度
		psc.setDecoderPrefixLength(Config.PREFIX_LENGTH);
		psc.setEncoderPrefixLength(Config.PREFIX_LENGTH);
		// 设置最大数据长度
		psc.setDecoderMaxDataLength(Config.MAX_DATA_LENGTH);
		psc.setEncoderMaxDataLength(Config.MAX_DATA_LENGTH);
		connector.getFilterChain()
				.addLast("code", new ProtocolCodecFilter(psc));
		
		// 添加业务逻辑处理类
		final ClientHandler clientHandler = new ClientHandler(mServiceHandler);
		connector.setHandler(clientHandler);
	}

	// 客户端使用bindService()与服务端进行绑定后调用
	@Override
	public IBinder onBind(Intent intent) {
		//startService(intent);
		return mBinder;
	}

	// 使用startService(Intent)启动服务时调用
	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("启动服务...");
		// 设置当前启动的服务ID
		this.startServiceId = startId;
		return START_STICKY;
	}*/

	// 客户端使用bindService()与服务端进行绑定后调用(当onUnbind()返回true时)
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	// 所有连接到该服务的客户端都通过unbindService解除绑定后调用
	@Override
	public boolean onUnbind(Intent intent) {
		//stopSelf();
		return true;
	}

	// 服务未与任何客户端进行绑定或者使用stopSelf()/stopService()停止服务
	// 时,服务销毁
	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("停止服务...");
		if(session != null || session.isClosing()){
			session.close(true);
		}
		mIsSessionClosed = true;
		//stopSelf();
		Toast.makeText(this, "服务已停止", Toast.LENGTH_SHORT).show();
	}

	// 请求与服务端建立连接
	public void startConnect() {
		Message msg = mServiceHandler.obtainMessage();
		msg.what = REQUEST_CONNECTION;
		msg.sendToTarget();
	}
	
	// 向服务端发送请求消息
	public void startSendMessage(String msg) {
		Message message = mServiceHandler.obtainMessage();
		message.what = SEND_MESSAGE;
		message.obj = msg;
		message.sendToTarget();
	}
	
	// 设置当前绑定的Activity主线程的Handler
	public void setUpdateClientHandler(Handler handler){
		updateClientHandler = handler;
	}
	
	public int getStartServiceId(){
		return this.startServiceId;
	}
	
	/** 
	 * @return 返回session的关闭状态
	 */
	public boolean getSessionClosed() {
		return mIsSessionClosed;
	}
	
	public static void setSessionClosed(boolean isClosed) {
		mIsSessionClosed = isClosed;
	}
	
	// 执行与服务端的网络通讯
	class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
				if(msg.what == REQUEST_CONNECTION) {// 创建与服务端的连接
					System.out.println("请求与服务端创建连接...");
					// 创建连接
					ConnectFuture future = connector
							.connect(new InetSocketAddress(Config.HOST,
									Config.PORT));
					// 等待连接创建完成
					future.awaitUninterruptibly();
					try{
						session = future.getSession();
						mIsSessionClosed = false;
						System.out.println("成功与服务端创建连接");
					}catch (Exception e) {
						e.printStackTrace(System.out);
						LoginActivity.cancelDialog();
						mIsSessionClosed = true;
						Toast.makeText(SocketService.this, "连接服务端出错", Toast.LENGTH_LONG).show();
					}
				}else if (msg.what == SEND_MESSAGE) {// 向服务端发送消息
					//若断开连接需重新请求与服务端建立连接
					if(mIsSessionClosed){
						startConnect();
					}
					System.out.println("开始向服务端发送消息...");
					try {
						session.write(msg.obj);
					}catch (Exception e) {
						e.printStackTrace(System.out);
					}
				}else if(msg.what == HANDLE_MESSAGE) { // 处理服务端返回消息
					System.out.println("从服务端返回的消息:" + msg.obj.toString());
					// 接收到消息后更新UI
					Message message = updateClientHandler.obtainMessage();
					message.obj = msg.obj;
					message.sendToTarget();
				}
		}
	}

}
