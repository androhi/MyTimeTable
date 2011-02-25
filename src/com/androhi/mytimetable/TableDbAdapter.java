package com.androhi.mytimetable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import com.androhi.mytimetable.R;

public class TableDbAdapter {
	
	// SimpleCursorAdapterを使うには "_id" カラムが必須。
	public static final String KEY_ROWID = "_id";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_MINUTE = "minute";
	public static final String NO_DATA = "未設定";
	
	private static final boolean DEBUG_FLAG = false;
	private static final String TAG = "TableDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static final String DATABASE_CREATE =
		"create table tmtb (_id integer primary key autoincrement," +
		"hour integer default 0, minute text default '未設定');";
	
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "tmtb";
	private static final int DATABASE_VERSION = 2;
	
	private final Context mCtx;
		
	private static final long[] DEFAULT_HOUR = {
		 1,  2,  3,  4,  5,  6,  7,  8,  9, 10,
		11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
		21, 22, 23, 24
	};
		
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.beginTransaction();
			try {
				SQLiteStatement stmt;
				db.execSQL(DATABASE_CREATE);
				stmt = db.compileStatement("insert into tmtb values (?,?,?);");
				
				for(int ii = 0; ii<24; ii++) {
					stmt.bindLong(2, DEFAULT_HOUR[ii]);
					stmt.bindString(3, NO_DATA);
					stmt.executeInsert();
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if(DEBUG_FLAG) Log.d(TAG, "Transaction end.");
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS timetable");
			onCreate(db);
		}
	}
	
	public TableDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}
	
	public TableDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		if(DEBUG_FLAG) Log.d(TAG,"コンストラクタ終了");
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
		
	public boolean deleteTimetable(long rowId) {
		
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public Cursor fetchAllTimetable() {
		
		return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID,KEY_HOUR,KEY_MINUTE}, null, null, null, null, null);
	}
	
	public Cursor fetchTimetable(long rowId) throws SQLException {
		Cursor mCursor =
				mDb.query(true, DATABASE_TABLE, new String[]{KEY_ROWID,KEY_HOUR,KEY_MINUTE}, KEY_ROWID + "=" + rowId, null,
					null, null, null, null);
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public boolean updateTimetable(long rowId, long hour, String minute) {
		ContentValues args = new ContentValues();
		args.put(KEY_HOUR, hour);
		args.put(KEY_MINUTE, minute);
		
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateTimetableMinute(long rowId, String minute) {
		ContentValues args = new ContentValues();
		args.put(KEY_MINUTE, minute);
		
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

}
