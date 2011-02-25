package com.androhi.mytimetable;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import com.androhi.mytimetable.R;

public class MyTimetableActivity extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	
	private static final boolean DEBUG_FLAG = false;
	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EDIT_ID = Menu.FIRST + 2;
	private static final int CLEAR_ID = Menu.FIRST + 3;
	private static final String TAG = "MyTimetableActivity";
	
	private TableDbAdapter mDbHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DEBUG_FLAG) Log.d(TAG, "onCreate start.");
        setContentView(R.layout.main);
        
        if(DEBUG_FLAG) Log.d(TAG, "database create start.");
        mDbHelper = new TableDbAdapter(this);
        mDbHelper.open();
        if(DEBUG_FLAG) Log.d(TAG, "create listview start.");
        fillData();

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
    }
    
    public void fillData() {
    	Cursor timesCursor = mDbHelper.fetchAllTimetable();
    	startManagingCursor(timesCursor);
    	
    	String[] from = new String[]{TableDbAdapter.KEY_HOUR, TableDbAdapter.KEY_MINUTE};
    	int[] to   = new int[]{R.id.text1, R.id.text2};
    	
    	SimpleCursorAdapter times =
    			new SimpleCursorAdapter(this, R.layout.times_row, timesCursor, from, to);
    	setListAdapter(times);
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, EDIT_ID, 0, R.string.menu_edit);
		menu.add(0, CLEAR_ID, 0, R.string.menu_clear);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case EDIT_ID:
			//mDbHelper.deleteTimetable(info.id);
			//fillData();
			Intent i = new Intent(this, TimetableEdit.class);
			i.putExtra(TableDbAdapter.KEY_ROWID, info.id);
			startActivityForResult(i, ACTIVITY_EDIT);
			return true;
		case CLEAR_ID:
			String minute = TableDbAdapter.NO_DATA;
			mDbHelper.updateTimetableMinute(info.id, minute);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	//private void createTimetable() {
	//	Intent i = new Intent(this, TimetableEdit.class);
	//	startActivityForResult(i, ACTIVITY_CREATE);
	//}
	
	//@Override
	//public void onListItemClick(ListView l, View v, int position, long id) {
	//	super.onListItemClick(l, v, position, id);
	//	Intent i = new Intent(this, TimetableEdit.class);
	//	i.putExtra(TableDbAdapter.KEY_ROWID, id);
	//	startActivityForResult(i, ACTIVITY_EDIT);
	//}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}