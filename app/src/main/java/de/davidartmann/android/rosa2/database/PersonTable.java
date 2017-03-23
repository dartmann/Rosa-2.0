package de.davidartmann.android.rosa2.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Metaclass for description of person table and providing static create and upgrade methods.
 * Created by david on 21.08.16.
 */
public class PersonTable {

    //Database informations
    public static final String TABLE_PERSON = "Person";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CREATETIME = "create_time";
    public static final String COLUMN_LASTUPDATE = "last_update";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_MISC = "misc";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_PICTUREURL = "picture_url";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_TABTEXT = "tab_text";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_CREATETIME, COLUMN_LASTUPDATE,
        COLUMN_NAME, COLUMN_ADDRESS, COLUMN_PHONE, COLUMN_EMAIL, COLUMN_PRICE, COLUMN_MISC,
        COLUMN_CATEGORY, COLUMN_ACTIVE, COLUMN_PICTUREURL, COLUMN_POSITION, COLUMN_TABTEXT};

    /**
     * Database creation string.
     */
    private static final String DATABASE_CREATE = "create table "
            + TABLE_PERSON
            + "("
            + COLUMN_ID + " integer primary key autoincrement," //0
            + COLUMN_CREATETIME + " integer not null,"          //1
            + COLUMN_LASTUPDATE + " integer,"                   //2
            + COLUMN_NAME + " text not null,"                   //3
            + COLUMN_ADDRESS + " text,"                         //4
            + COLUMN_PHONE + " text,"                           //5
            + COLUMN_EMAIL + " text,"                           //6
            + COLUMN_PRICE + " text,"                           //7
            + COLUMN_MISC + " text,"                            //8
            + COLUMN_CATEGORY + " integer,"                     //9
            + COLUMN_ACTIVE + " integer not null,"              //10
            + COLUMN_PICTUREURL + " text,"                      //11
            + COLUMN_POSITION + " integer not null,"            //12
            + COLUMN_TABTEXT + " text"                          //13
            +");";

    /**
     * Creation of database.
     * @param database db object.
     */
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(PersonTable.class.getSimpleName(), "Upgrading database from version "
                +oldVersion+" to "+newVersion+" which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS "+TABLE_PERSON);
        onCreate(database);
    }
}
