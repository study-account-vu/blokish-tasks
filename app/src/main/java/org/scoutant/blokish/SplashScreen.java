package org.scoutant.blokish;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

/** Shows the startup screen briefly before opening the main game activity. */
public class SplashScreen extends Activity {
	private static final long DELAY = 1500;

	/**
	 * Displays the splash layout and schedules entry into the game.
	 * @param savedInstanceState previously saved activity state, if any
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splashscreen);
		long delai = DELAY;
		try {
			FileInputStream fis;
			fis = openFileInput("moves.txt");
			BufferedReader reader = new BufferedReader( new InputStreamReader(fis));
			if (reader.readLine()!=null && reader.readLine()!=null) {
				delai = DELAY / 3;
			}
		} catch (Exception e) {
		}
		handler.sendEmptyMessageDelayed(0, delai);
	}

	/** Delayed transition handler that launches the main activity. */
	private Handler handler = new Handler() {
		/**
		 * Launches the main game activity after the configured delay.
		 * @param msg message delivered by the handler
		 */
		@Override
		public void handleMessage(Message msg) {
			startActivityForResult(new Intent(SplashScreen.this, UI.class), 0);
			super.handleMessage(msg);
		}
	};
	
	/**
	 * Finishes the splash activity after the main activity returns.
	 * @param requestCode request code associated with the activity launch
	 * @param resultCode result code returned by the main activity
	 * @param data optional result data
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	};
	
}