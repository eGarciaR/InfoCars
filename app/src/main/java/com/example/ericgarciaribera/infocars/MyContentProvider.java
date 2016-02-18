package com.example.ericgarciaribera.infocars;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by ericgarciaribera on 07/06/15.
 */
public class MyContentProvider extends ContentProvider {

    private DBHelper myDB;

    private static final String AUTHORITY = "com.example.ericgarciaribera.infocars.MyContentProvider";
    public static final String MODELS_TABLE = Contract.TABLE_MODELS;
    public static final String INFO_TABLE = Contract.TABLE_INFO;
    public static final Uri CONTENT_URI_MODELS = Uri.parse("content://" + AUTHORITY + "/" + MODELS_TABLE);
    public static final Uri CONTENT_URI_INFO = Uri.parse("content://" + AUTHORITY + "/" + INFO_TABLE);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int MODELS = 1;
    public static final int MODELS_ID = 2;
    public static final int INFO = 3;
    public static final int INFO_ID = 4;

    static {
        sURIMatcher.addURI(AUTHORITY, MODELS_TABLE, MODELS);
        sURIMatcher.addURI(AUTHORITY, MODELS_TABLE + "/#", MODELS_ID);
        sURIMatcher.addURI(AUTHORITY, INFO_TABLE, INFO);
        sURIMatcher.addURI(AUTHORITY, INFO_TABLE + "/#", INFO_ID);
    }

    @Override
    public boolean onCreate() {
        myDB = new DBHelper(getContext(), null, null, 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Contract.TABLE_MODELS);
        SQLiteQueryBuilder queryBuilderInfo = new SQLiteQueryBuilder();
        queryBuilderInfo.setTables(Contract.TABLE_INFO);

        int uriType = sURIMatcher.match(uri);

        boolean m = false;
        boolean i = false;

        switch (uriType) {
            case MODELS_ID:
                m = true;
                queryBuilder.appendWhere(Contract.Model.COLUMN_MODEL_NAME + "=" + uri.getLastPathSegment());
                break;
            case MODELS:
                m = true;
                break;
            case INFO_ID:
                i = true;
                queryBuilderInfo.appendWhere(Contract.Info.COLUMN_INFO_NAME + "=" + uri.getLastPathSegment());
                break;
            case INFO:
                i = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        if (m) {
            Cursor cursor = queryBuilder.query(myDB.getReadableDatabase(),
                    projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        else if (i) {
            Cursor cursor = queryBuilderInfo.query(myDB.getReadableDatabase(),
                    projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = myDB.getWritableDatabase();

        Uri uriAux = Uri.parse(MODELS_TABLE + "/" + 0);

        boolean m = false;
        boolean i = false;

        long id = 0;
        switch (uriType) {
            case MODELS:
                m = true;
                id = sqlDB.insert(Contract.TABLE_MODELS, null, values);
                break;
            case INFO:
                i = true;
                id = sqlDB.insert(Contract.TABLE_INFO, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (m) {
            uriAux = Uri.parse(MODELS_TABLE + "/" + id);
        }
        else if(i) {
            uriAux = Uri.parse(INFO_TABLE + "/" + id);
        }
        return uriAux;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case MODELS:
                rowsDeleted = sqlDB.delete(Contract.TABLE_MODELS,
                        selection,
                        selectionArgs);
                break;

            case MODELS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(Contract.TABLE_MODELS,
                            Contract.Model.COLUMN_MODEL_NAME + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(Contract.TABLE_MODELS,
                            Contract.Model.COLUMN_MODEL_NAME + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            case INFO:
                rowsDeleted = sqlDB.delete(Contract.TABLE_INFO,
                        selection,
                        selectionArgs);
                break;

            case INFO_ID:
                String idi = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(Contract.TABLE_INFO,
                            Contract.Info.COLUMN_INFO_NAME + "=" + idi,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(Contract.TABLE_INFO,
                            Contract.Info.COLUMN_INFO_NAME + "=" + idi
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case MODELS:
                rowsUpdated = sqlDB.update(Contract.TABLE_MODELS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case MODELS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(Contract.TABLE_MODELS,
                                    values,
                                    Contract.Model.COLUMN_MODEL_NAME + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(Contract.TABLE_MODELS,
                                    values,
                                    Contract.Model.COLUMN_MODEL_NAME + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            case INFO:
                rowsUpdated = sqlDB.update(Contract.TABLE_INFO,
                        values,
                        selection,
                        selectionArgs);
                break;
            case INFO_ID:
                String idi = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(Contract.TABLE_INFO,
                                    values,
                                    Contract.Info.COLUMN_INFO_NAME + "=" + idi,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(Contract.TABLE_INFO,
                                    values,
                                    Contract.Info.COLUMN_INFO_NAME + "=" + idi
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
