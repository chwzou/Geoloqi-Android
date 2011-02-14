package com.geoloqi.android1;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Geoloqi extends Activity implements OnClickListener {
	public static final String TAG = "GeoloqiServiceDemo";
	private Button buttonStart, buttonStop, buttonUpdate;
	private TextView latLabel, lngLabel, numPointsLabel;
	protected LQLocationData db;
	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
		latLabel = (TextView) findViewById(R.id.textLatitude);
		lngLabel = (TextView) findViewById(R.id.textLongitude);
		numPointsLabel = (TextView) findViewById(R.id.textNumPointsInQueue);

		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		buttonUpdate.setOnClickListener(this);

		db = new LQLocationData(this);
		new Timer().schedule(new MyTimerTask(), 0, 1000);

		Log.d(TAG, "onClick: starting srvice");
		startService(new Intent(this, GeoloqiService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "Inflating menu!");
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	Intent preferences = new Intent(this, GeoloqiPreferences.class);
	    	startActivity(preferences);
	        return true;
	    case R.id.quit:
	        // quit();
	    	System.exit(0);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			Log.d(TAG, "onClick: starting srvice");
			startService(new Intent(this, GeoloqiService.class));
			break;
		case R.id.buttonStop:
			Log.d(TAG, "onClick: stopping srvice");
			stopService(new Intent(this, GeoloqiService.class));
			break;
		case R.id.buttonUpdate:
			Log.d(TAG, "onClick: update");
			new LQUpdateUI().execute();
			break;
		}
	}

	class LQUpdateUI extends AsyncTask<Void, Void, LQPoint> {

		// Doesn't have access to the UI thread
		@Override
		protected LQPoint doInBackground(Void... v) {
			return db.getLastLocation();
		}

		protected void onProgressUpdate() {

		}

		// Runs with the return value of doInBackground, has access to the UI thread
		@Override
		protected void onPostExecute(LQPoint point) {
			if(point== null)
				return;
				
			latLabel.setText("Lat: "+point.latitude);
			lngLabel.setText("Lng: "+point.longitude);
			numPointsLabel.setText("Points in Queue: "+db.numberOfUnsentPoints());
		}		
	}

	public class MyTimerTask extends TimerTask {
		private Runnable runnable = new Runnable() {
			public void run() {
				new LQUpdateUI().execute();
			}
		};

		@Override
		public void run() {
			handler.post(runnable);
		}
	}
}