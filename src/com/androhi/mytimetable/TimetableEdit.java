package com.androhi.mytimetable;


import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admob.android.ads.AdView;

public class TimetableEdit extends Activity {

	private TextView mHourLabel;
	private TextView mMinuteLabel;
	private int[] minuteList;
	private Long mRowId;
	private Long mHour;
	private SubTableDbAdapter mDbHelper;
	private Resources mRes;
	private int mType;
	
	private static final int BUTTON_ON = 1;
	private static final int BUTTON_OFF = 0;
	
	private static final int[] buttonIdList = {
		R.id.btn00,
		R.id.btn01, R.id.btn02, R.id.btn03, R.id.btn04, R.id.btn05,
		R.id.btn06, R.id.btn07, R.id.btn08, R.id.btn09, R.id.btn10,
		R.id.btn11, R.id.btn12, R.id.btn13, R.id.btn14, R.id.btn15,
		R.id.btn16, R.id.btn17, R.id.btn18, R.id.btn19, R.id.btn20,
		R.id.btn21, R.id.btn22, R.id.btn23, R.id.btn24, R.id.btn25,
		R.id.btn26, R.id.btn27, R.id.btn28, R.id.btn29, R.id.btn30,
		R.id.btn31, R.id.btn32, R.id.btn33, R.id.btn34, R.id.btn35,
		R.id.btn36, R.id.btn37, R.id.btn38, R.id.btn39, R.id.btn40,
		R.id.btn41, R.id.btn42, R.id.btn43, R.id.btn44, R.id.btn45,
		R.id.btn46, R.id.btn47, R.id.btn48, R.id.btn49, R.id.btn50,
		R.id.btn51, R.id.btn52, R.id.btn53, R.id.btn54, R.id.btn55,
		R.id.btn56, R.id.btn57, R.id.btn58, R.id.btn59, 
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new SubTableDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.time_edit);
		
		mHourLabel = (TextView) findViewById(R.id.hour);
		mMinuteLabel = (TextView) findViewById(R.id.label_set_time);
		Button confirmButton = (Button) findViewById(R.id.confirm);
		
		mRes = getResources();
		
		minuteList = new int[60];
		mRowId = savedInstanceState != null ? savedInstanceState.getLong(SubTableDbAdapter.KEY_SUB_ROWID) : null;
		mHour = savedInstanceState != null ? savedInstanceState.getLong(SubTableDbAdapter.KEY_SUB_HOUR) : null;
		if(mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(SubTableDbAdapter.KEY_SUB_ROWID) : null;
			//mHour = extras != null ? extras.getLong(SubTableDbAdapter.KEY_SUB_HOUR) : null;
			mHour = extras.getLong(SubTableDbAdapter.KEY_SUB_HOUR);
			mType = extras.getInt(SubTableDbAdapter.KEY_SUB_TYPE);
		}
		
		if(mHour > 0) {
			;
		} else {
			;
		}
		
		populateFields();
		
		AdView adView = new AdView(this);
		adView.setVisibility(android.view.View.VISIBLE);
		adView.requestFreshAd();
		LayoutParams adLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		adView.setLayoutParams(adLayoutParams);

		LinearLayout linearlayout = new LinearLayout(this);
		linearlayout.addView(adView);
		linearlayout.setGravity(Gravity.BOTTOM);

		LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		addContentView(linearlayout, layoutParams);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}
	
	private void populateFields() {
		if(mRowId != null) {
			Cursor time = mDbHelper.fetchTimetable(mRowId, mHour, mType);
			startManagingCursor(time);
			mHourLabel.setText(time.getString(
					time.getColumnIndexOrThrow(SubTableDbAdapter.KEY_SUB_HOUR)));
			String minuteString = time.getString(
					time.getColumnIndexOrThrow(SubTableDbAdapter.KEY_SUB_MINUTE));
			
			String[] minuteData = minuteString.split(" ");
			for(int ii = 0; ii<minuteData.length; ii++) {
				if(isDigit(minuteData[ii])) {
					int index = Integer.parseInt(minuteData[ii]);
					minuteList[index] = 1;
				}
			}
			
			for(int ii = 0; ii<60; ii++) {
				Button btn = (Button) findViewById(buttonIdList[ii]);
				if(minuteList[ii] == 1) {
					btn.setTextColor(mRes.getColor(R.color.blue));
				} else {
					btn.setTextColor(mRes.getColor(R.color.black));
				}
			}
			
			String minute = getMinuteData();
			mMinuteLabel.setText(minute);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SubTableDbAdapter.KEY_SUB_ROWID, mRowId);
		outState.putLong(SubTableDbAdapter.KEY_SUB_HOUR, mHour);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveState();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	private boolean isDigit(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private void saveState() {
		String minute = null;
		long hour = 0;
		minute = getMinuteData();
		hour = Long.parseLong(mHourLabel.getText().toString());
		boolean rslt = mDbHelper.updateTimetableMinute(mRowId, hour, minute, mType);
		if(rslt) {
			;
		}
	}
	
	private String getMinuteData() {
		String min = null;
		for(int ii = 0, cnt = 0; ii<60; ii++) {
			if(minuteList[ii] == 1) {
				String data = changeIntToString(ii);
				if(cnt == 0) {
					min = data;
				} else {
					min += "  " + data;
				}
				cnt++;
			}
		}
		if(min == null) {
			min = SubTableDbAdapter.NO_DATA;
		}
		return min;
	}
	
	private String changeIntToString(int num) {
		String str = null;
		if((num >= 0) && (num <=9)) {
			str = "0" + Integer.toString(num);
		} else {
			str = Integer.toString(num);
		}
		return str;
	}

	public void setMinuteOnOff(View view) {
		int color = 0;
		int selectId = view.getId();
		Button btn = (Button) findViewById(selectId);
		int buttonNo = Integer.parseInt(btn.getText().toString());
		if(minuteList[buttonNo] == BUTTON_OFF) {
			minuteList[buttonNo] = BUTTON_ON;
			color = mRes.getColor(R.color.blue);
		} else {
			minuteList[buttonNo] = BUTTON_OFF;
			color = mRes.getColor(R.color.black);
		}
		btn.setTextColor(color);
		String minute = getMinuteData();
		mMinuteLabel.setText(minute);
	}
}
