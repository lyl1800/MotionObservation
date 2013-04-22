package cn.edu.ecnu.sophia.motionobservation;

import cn.edu.ecnu.sophia.motionobservation.login.LoginActivity;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

public class WelcomeActivity extends Activity {

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		Handler handler=new Handler();
		handler.postDelayed(new hrun(), 3000);
	}
	private class hrun implements Runnable{

		public void run() {
			Intent intent=new Intent();
			intent.setClass(WelcomeActivity.this, LoginActivity.class);
			WelcomeActivity.this.startActivity(intent);
		}
		
	}

}
