package com.example.ericgarciaribera.infocars;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ericgarciaribera on 07/06/15.
 */
public class DBHelper extends SQLiteOpenHelper {

        private ContentResolver myCR;

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            myCR = context.getContentResolver();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Contract.SQL_CREATE_MODELS);
            db.execSQL(Contract.SQL_CREATE_INFO);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Contract.TABLE_MODELS);
            db.execSQL("DROP TABLE IF EXISTS " + Contract.TABLE_INFO);
        }

        public void deleteIfExists() {
            myCR.delete(MyContentProvider.CONTENT_URI_MODELS, null, null);
        }

        public void deleteIfExistsI() {
            myCR.delete(MyContentProvider.CONTENT_URI_INFO, null, null);
        }

        public void addModel(String name) {

            ContentValues values = new ContentValues();
            values.put(Contract.Model.COLUMN_MODEL_NAME, name);

            myCR.insert(MyContentProvider.CONTENT_URI_MODELS, values);
        }

        public void addInfo(String name) {

            ContentValues values = new ContentValues();
            values.put(Contract.Info.COLUMN_INFO_NAME, name);

            myCR.insert(MyContentProvider.CONTENT_URI_INFO, values);
        }

}
