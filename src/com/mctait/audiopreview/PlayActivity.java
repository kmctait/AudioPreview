package com.mctait.audiopreview;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.Mp3File;

// TODO: lines to break up display?
// TODO: specify folder to read music from (absolute path vs MUSIC.default directory)

public class PlayActivity extends Activity {

	Timer timer;
	TimerTask timerTask;
	final Handler handler = new Handler();
	
    MediaPlayer mp = new MediaPlayer();
    boolean isPlaying = false;
    
    TextView textView;
    WebView webView;
    ImageView imageView;
    Button button;
    
    private static final int RESULT_SETTINGS = 1;
    int DURATION = 60000;
    
    File[] files;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
		files = new File("/storage/3064-3134/Music/").listFiles();
		
		textView = (TextView)findViewById(R.id.textView1);
		imageView = (ImageView) findViewById(R.id.imageView1);
		
		addListenerToPlayButton();
	}
	
	public void addListenerToPlayButton() {

		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				if(mp.isPlaying()){
					mp.pause();
					webView.loadUrl("file:///android_asset/myhtml2.html");
					textView.setText("");
					stoptimertask(arg0);
					imageView.setImageResource(R.drawable.play1);
				}
				else {
					startTimer();
					webView = (WebView)findViewById(R.id.webView1);
					webView.loadUrl("file:///android_asset/myhtml.html");
					imageView.setImageResource(R.drawable.pause1);
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		mp.stop();
		mp.release();
		stoptimertask(this.getCurrentFocus());
	    super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void stoptimertask(View v) {
	    //stop the timer, if it's not already null
	    if (timer != null) {
	        timer.cancel();
	        timer = null;
	    }
	}
	
	public void startTimer() {
		timer = new Timer();
		initializeTimerTask();
		timer.schedule(timerTask, 1, DURATION);
	}
	
	public void initializeTimerTask() {
		timerTask = new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run(){
						mp.reset();
					    try {
					    	File file = files[getRandomNumber(0, files.length)];
					    	textView.setText("");
					        mp.setDataSource(file.getPath());
					        mp.prepare();
					        mp.start();
					        writeSongDetailsToScreen(file);
					        
					    } catch (Exception e) {
					        e.printStackTrace();
					        startTimer();
					    }
					}
				});
			}
		};
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
			break;
		}
	}
	
	private void showUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String prefDuration = sharedPrefs.getString("pref_duration", "60");
		DURATION = Integer.valueOf(prefDuration)*1000;
	}
	
	protected int getRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max-min)+1) + min;
	}
	
	protected void writeSongDetailsToScreen(File file) {
		try{
			Mp3File mp3 = new Mp3File(file.getPath());
			if(mp3.hasId3v1Tag()) {
				ID3v1 tag = mp3.getId3v1Tag();
				if(tag.getTitle() != null && tag.getTitle() != "") {
					textView.append(tag.getTitle());
					textView.append("\n");
					textView.append(tag.getArtist());
				} else
					textView.append(tag.getTrack());
			} else {
				textView.append(file.getName());
			}
		} catch(Exception e){
			textView.setText(file.getName());			
		}
	}

}

