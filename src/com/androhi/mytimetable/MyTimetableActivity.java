package com.androhi.mytimetable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.admob.android.ads.AdView;

public class MyTimetableActivity extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	
	private static final boolean DEBUG_FLAG = true;
	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EDIT_ID = Menu.FIRST + 2;
	private static final int CLEAR_ID = Menu.FIRST + 3;
	private static final String TAG = "MyTimetableActivity";
	private static final float FLICK_WIDTH = 100;
	
	private SubTableDbAdapter mDbHelper;
	private Long mRowId;
	private String mTitle1;
	private String mTitle2;
	private boolean mMoveFlag;
	private float mStartPosX;
	private float mEndPosX;
	private int mDataType;
	private TextView mTypeText;
	private Resources mRes;
	private String[] mArrayType;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DEBUG_FLAG) Log.d(TAG, "onCreate start.");
        setContentView(R.layout.main);
        
        mDataType = 0; //データタイプ初期化
        mRes = getResources();
		mArrayType = mRes.getStringArray(R.array.array_type);
		        
        if(DEBUG_FLAG) Log.d(TAG, "database create start.");
        mDbHelper = new SubTableDbAdapter(this);
        mDbHelper.open();
        if(DEBUG_FLAG) Log.d(TAG, "create listview start.");

        TextView title1Label = (TextView) findViewById(R.id.title1_main);
        TextView title2Label = (TextView) findViewById(R.id.title2_main);
        Button confirmMainButton = (Button) findViewById(R.id.confirm_main);
        mTypeText = (TextView) findViewById(R.id.dataType_main);
        RelativeLayout layoutHeader = (RelativeLayout) findViewById(R.id.layout_header);
        
    	boolean newRequest = false;
		mRowId = savedInstanceState != null ? savedInstanceState.getLong(SubTableDbAdapter.KEY_SUB_ROWID) : null;
        if(mRowId == null) {
			Bundle extras = getIntent().getExtras();
			if(extras != null) {
				mRowId = extras.getLong(SubTableDbAdapter.KEY_SUB_ROWID);
				newRequest = extras.getBoolean(SubTableDbAdapter.EXT_SUB_REQUEST);
				mTitle1 = extras.getString(MainTableDbAdapter.KEY_MAIN_TITLE1);
				mTitle2 = extras.getString(MainTableDbAdapter.KEY_MAIN_TITLE2);
				//title1Label.setText("水戸駅");
				//title2Label.setText("常磐線");
			}
        }
        
        title1Label.setText(mTitle1);
        title2Label.setText(mTitle2);
        String typeText = getDataTypeText(mDataType);
        if(typeText != null) {
        	mTypeText.setText(typeText);
        }
        
        if(newRequest) {
        	mDbHelper.createTimeTable(mRowId);
        }
        
        fillSubData();

        //広告表示のテスト
        //AdManager.setTestDevices(new String[] {
        //		AdManager.TEST_EMULATOR,
        //		"100063a85d06",
        //});
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
        
        registerForContextMenu(getListView());
        
		confirmMainButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
		layoutHeader.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(0);
			}
		});
    }
    
    public void fillSubData() {
    	if(mRowId != null) {
    		Cursor timesCursor = mDbHelper.fetchAllTimetable(mRowId, mDataType);
    		startManagingCursor(timesCursor);
    	
    		String[] from = new String[]{SubTableDbAdapter.KEY_SUB_HOUR, SubTableDbAdapter.KEY_SUB_MINUTE};
    		int[] to   = new int[]{R.id.text1, R.id.text2};
    	
    		SimpleCursorAdapter times =
    				new SimpleCursorAdapter(this, R.layout.times_row, timesCursor, from, to);
    		setListAdapter(times);
    	}
    }
    
    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //	super.onCreateOptionsMenu(menu);
    //	menu.add(0, INSERT_ID, 0, R.string.menu_insert);
    //	return true;
    //}
    
    //@Override
    //public boolean onMenuItemSelected(int featureId, MenuItem item) {
    //	switch(item.getItemId()) {
    //	case INSERT_ID:
    //		createTimetable();
    //		return true;
    //	}
    	
    //	return super.onMenuItemSelected(featureId, item);
    //}

	//@Override
	//public void onCreateContextMenu(ContextMenu menu, View v,
	//		ContextMenuInfo menuInfo) {
	//	super.onCreateContextMenu(menu, v, menuInfo);
	//	menu.add(0, EDIT_ID, 0, R.string.menu_edit);
	//	menu.add(0, CLEAR_ID, 0, R.string.menu_clear);
	//}
	
	//@Override
	//public boolean onContextItemSelected(MenuItem item) {
	//	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	//	int pos = info.position + 1;

	//	switch(item.getItemId()) {
	//	case EDIT_ID:
	//		//mDbHelper.deleteTimetable(info.id);
	//		//fillData();
	//		Intent i = new Intent(this, TimetableEdit.class);
	//		i.putExtra(SubTableDbAdapter.KEY_SUB_ROWID, mRowId);
	//		i.putExtra(SubTableDbAdapter.KEY_SUB_HOUR, pos);
	//		startActivityForResult(i, ACTIVITY_EDIT);
	//		return true;
	//	case CLEAR_ID:
	//		String minute = SubTableDbAdapter.NO_DATA;
	//		mDbHelper.updateTimetableMinute(mRowId, pos, minute);
	//		fillSubData();
	//		return true;
	//	}
	//	return super.onContextItemSelected(item);
	//}
	
	//private void createTimetable() {
	//	Intent i = new Intent(this, TimetableEdit.class);
	//	startActivityForResult(i, ACTIVITY_CREATE);
	//}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		long pos = position + 1;
		Intent i = new Intent(this, TimetableEdit.class);
		i.putExtra(SubTableDbAdapter.KEY_SUB_ROWID, mRowId);
		//i.putExtra(SubTableDbAdapter.KEY_SUB_HOUR, position+1);
		i.putExtra(SubTableDbAdapter.KEY_SUB_HOUR, pos);
		i.putExtra(SubTableDbAdapter.KEY_SUB_TYPE, mDataType);
		startActivityForResult(i, ACTIVITY_EDIT);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillSubData();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	//アニメーションだけ後で実装する
	/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			mMoveFlag = true;
			break;
		case MotionEvent.ACTION_DOWN:
			mStartPosX = event.getX();
			break;
		case MotionEvent.ACTION_UP:
			mEndPosX = event.getX();
			break;
		}
		
		int oldType = mDataType;
		
		if(mMoveFlag) {
			if(mEndPosX - mStartPosX > FLICK_WIDTH) {
				switch(mDataType) {
				case 0:
					break;
				case 1:
					mDataType = 0;
					break;
				case 2:
					mDataType = 1;
					break;
				case 3:
					mDataType = 2;
					break;
				}
				mMoveFlag = false;
			} else if(mStartPosX - mEndPosX > FLICK_WIDTH) {
				switch(mDataType) {
				case 0:
					mDataType = 1;
					break;
				case 1:
					mDataType = 2;
					break;
				case 2:
					mDataType = 3;
					break;
				case 3:
					break;
				}
				mMoveFlag = false;
			}
		}
		
		if(mDataType != oldType) {
			float fromAlpha = 1f;
			float toAlpha = 0f;
			AlphaAnimation animation = new AlphaAnimation(fromAlpha, toAlpha);
			animation.setDuration(2000);
			animation.setFillBefore(true);
			animation.setAnimationListener(new DisplayText());
			mTypeText.startAnimation(animation);
		}

		return super.onTouchEvent(event);
	}
	
	private class DisplayText implements AnimationListener {
		public void onAnimationStart(Animation animation) { }
		public void onAnimationRepeat(Animation animation) { }
		public void onAnimationEnd(Animation animation) {
			fillSubData();
		}
	}
	*/
	
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("区分の選択");
		//builder.setView(inputView);
		builder.setSingleChoiceItems(mArrayType, mDataType, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String dataTypeText = null;
				switch(which) {
				case 0:
					mDataType = 0;
					break;
				case 1:
					mDataType = 1;
					break;
				case 2:
					mDataType = 2;
					break;
				case 3:
					mDataType = 3;
					break;
				}
				if(mDataType >= 0 && mDataType <= 3) {
					fillSubData();
					dataTypeText = getDataTypeText(mDataType);
					if(dataTypeText != null) {
						mTypeText.setText(dataTypeText);
					}
				}
				dialog.cancel();
			}
		});
		builder.setNegativeButton(R.string.label_dlg_cl, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	
	return builder.create();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}
	
	private String getDataTypeText(int type) {
		String dataTypeText = null;
		if(type >= 0 && type <= 3) {
			dataTypeText = "（" + mArrayType[type] + "）";
		}
		return dataTypeText;
	}
}