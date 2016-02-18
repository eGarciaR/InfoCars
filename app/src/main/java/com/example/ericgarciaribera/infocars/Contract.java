package com.example.ericgarciaribera.infocars;

import android.provider.BaseColumns;

/**
 * Created by ericgarciaribera on 07/06/15.
 */
public class Contract {

    public static final String TABLE_MODELS = "models";
    public static final String TABLE_INFO = "info";
    private static final String TEXT_TYPE = " TEXT";
    public static final String SQL_CREATE_MODELS =
            "CREATE TABLE " + TABLE_MODELS + " (" +
                    Model._ID + " INTEGER PRIMARY KEY," +
                    Model.COLUMN_MODEL_NAME + TEXT_TYPE + " )";

    public static final String SQL_CREATE_INFO =
            "CREATE TABLE " + TABLE_INFO + " (" +
                    Info._ID + " INTEGER PRIMARY KEY," +
                    Info.COLUMN_INFO_NAME + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_MODELS;

    public Contract() {}

    public static abstract class Model implements BaseColumns {

        public static final String COLUMN_MODEL_NAME = "modelName";
        public static final String TABLE_NAME = "models";

    }

    public static abstract class Info implements BaseColumns {

        public static final String COLUMN_INFO_NAME = "infoName";
        public static final String TABLE_INFO = "info";

    }

}
