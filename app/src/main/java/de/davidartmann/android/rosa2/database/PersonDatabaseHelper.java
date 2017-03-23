package de.davidartmann.android.rosa2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Databasehelper for creating and updating the database schema.
 * Created by david on 21.08.16.
 */
public class PersonDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "person.db";
    private static final int DATABASE_VERSION = 1;

    public PersonDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        PersonTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        PersonTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }
}
