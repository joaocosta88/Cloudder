package com.layla.cloudder.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FileDatabase extends SQLiteOpenHelper {
	
	public static final String TABLE_FILES = "files";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_DESTINY = "destiny";
	public static final String COLUMN_ID = "_id";
	private static final String DATABASE_NAME = "files.db";
	private static final int DATABASE_VERSION = 2;
	
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_FILES+ "( " + COLUMN_ID	+ " integer primary key autoincrement, " 
			+ COLUMN_NAME+ " text not null, "
			+ COLUMN_PATH+ " text, "
			+ COLUMN_DESTINY + " text);";
	
	public FileDatabase(Context c) {
		super(c, DATABASE_NAME, null, DATABASE_VERSION);		
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
		onCreate(db);
	}
}
