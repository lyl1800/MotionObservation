package cn.edu.ecnu.sophia.motionobservation.net;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.ecnu.sophia.motionobservation.dao.JSONParser;

import android.os.Handler;
import android.os.Message;

public class ClientHandler extends IoHandlerAdapter {
	private final static Logger logger = LoggerFactory
			.getLogger(ClientHandler.class);

	private Handler mHandler;

	public ClientHandler(Handler handler) {
		this.mHandler = handler;
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
		logger.info("Session已创建...");
		System.out.println("Session已创建...");
	}

	/**
	 * 这个方法在连接被打开时调用，它总是在sessionCreated()方法之后被调用。对于TCP来
	 * 说，它是在连接被建立之后调用，可以在这里执行一些认证操作、发送数据等。
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("Session已打开...");
	}

	/**
	 * 收到服务端返回的信息时调用
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		System.out.println("收到信息：" + message.toString());
		if (!JSONParser.isHeartBeat(message.toString())) {
			Message msg = new Message();
			msg.what = 2;
			msg.obj = message;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 当使用IoSession.write(Object)写的消息被发送后被调用
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("发送信息：" + message.toString());
	}

	/**
	 * 抛出异常时调用, 如果是IOException, MINA将会自动关闭连接
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.error("客户端发生异常...", cause);
		System.out.println("客户端发生异常...");
		SocketService.setSessionClosed(true);
		cause.printStackTrace(System.out);
	}

	/**
	 * 连接关闭时调用
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		SocketService.setSessionClosed(true);
		System.out.println("session已关闭");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		String heartbeat = "{\"REQ\":\"beat\",\"DAT\":{}}";
		session.write(heartbeat);
	}
}
