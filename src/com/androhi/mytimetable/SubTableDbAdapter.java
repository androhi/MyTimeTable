package com.androhi.mytimetable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class SubTableDbAdapter {
	
	// SimpleCursorAdapterを使うには "_id" カラムが必須。
	public static final String KEY_SUB_ROWID = "_id";
	public static final String KEY_SUB_HOUR = "hour";
	public static final String KEY_SUB_MINUTE = "minute";
	public static final String KEY_SUB_TYPE = "dtype";
	public static final String EXT_SUB_REQUEST = "request";
	public static final String NO_DATA = "未設定";
	
	private static final boolean DEBUG_FLAG = true;
	private static final String TAG = "SubTableDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private long mMainId;
	
	private static final String SUB_DATABASE_CREATE =
		"create table TimeTableValues (_id integer, hour integer, minute text not null, dtype integer);";
	
	private static final String DATABASE_NAME = "data";
	private static final String SUB_DATABASE_TABLE = "TimeTableValues";
	private static final int DATABASE_VERSION = 3;
	
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
			db.execSQL(SUB_DATABASE_CREATE);
			Log.d(TAG, "Database create end.");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS TimeTableValues");
			onCreate(db);
		}
	}
	
	public SubTableDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}
	
	public SubTableDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		if(DEBUG_FLAG) Log.d(TAG,"コンストラクタ終了");
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
		
    public void createTimeTable(long mainId) {
		mDb.beginTransaction();
		try {
			SQLiteStatement stmt;
			// サブDB作成
			stmt = mDb.compileStatement("insert into TimeTableValues values (?,?,?,?);");
			for(int ii = 0; ii<4; ii++) {
				for(int jj = 0; jj<24; jj++) {
					stmt.bindLong(1, mainId);
					stmt.bindLong(2, DEFAULT_HOUR[jj]);
					stmt.bindString(3, NO_DATA);
					stmt.bindLong(4, ii);
					stmt.executeInsert();
				}
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
			if(DEBUG_FLAG) Log.d(TAG, "Transaction end.");
		}
    }

    public boolean deleteTimetable(long mainId) {
		
		return mDb.delete(SUB_DATABASE_TABLE, KEY_SUB_ROWID + "=" + mainId, null) > 0;
	}
	
	public Cursor fetchAllTimetable(long mainId, int type) {
		//Cursor mCursor = mDb.query(true, SUB_DATABASE_TABLE, new String[]{KEY_SUB_ROWID,KEY_SUB_HOUR,KEY_SUB_MINUTE},
		//					KEY_SUB_ROWID + "=" + mainId, null, null, null, null, null);
		Cursor mCursor = mDb.rawQuery("select * from TimeTableValues where _id = " + mainId + " and dtype = " + type, null);
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor fetchTimetable(long mainId, long hour, int type) throws SQLException {
		//Cursor mCursor =
		//		mDb.query(true, SUB_DATABASE_TABLE, new String[]{KEY_SUB_ROWID,KEY_SUB_HOUR,KEY_SUB_MINUTE},
		//				KEY_SUB_ROWID + "=" + mainId + " and " + KEY_SUB_HOUR + "=" + hour, null, null, null, null, null);
		Cursor mCursor = mDb.rawQuery("select * from TimeTableValues where _id = " + mainId + " and hour = " + hour + " and dtype = " + type, null);
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//public boolean updateTimetable(long mainId, long hour, String minute) {
	//	ContentValues args = new ContentValues();
	//	args.put(KEY_SUB_HOUR, hour);
	//	args.put(KEY_SUB_MINUTE, minute);
		
	//	return mDb.update(SUB_DATABASE_TABLE, args, KEY_SUB_ROWID + "=" + mainId, null) > 0;
	//}

	public boolean updateTimetableMinute(long mainId, long hour, String minute, int type) {
		ContentValues args = new ContentValues();
		args.put(KEY_SUB_MINUTE, minute);
		String id = Long.toString(mainId);
		String hr = Long.toString(hour);
		String tp = Long.toString(type);
		
		return mDb.update(SUB_DATABASE_TABLE, args, KEY_SUB_ROWID + "=? and " + KEY_SUB_HOUR + "=? and " + KEY_SUB_TYPE + "=?", new String[]{id,hr,tp}) > 0;
		//return mDb.rawQuery("update TimeTableValues set minute = '" + minute + "' where _id = " + mainId + " and hour = " + hour, null) != null;
	}

}
