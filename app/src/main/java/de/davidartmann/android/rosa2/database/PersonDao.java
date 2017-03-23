package de.davidartmann.android.rosa2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.davidartmann.android.rosa2.database.model.Person;

/**
 * DAO class for accessing data from the person table of sqlite database.
 * Created by david on 06.09.16.
 */
public class PersonDao {

    private static final String TAG = PersonDao.class.getSimpleName();

    /**
     * The {@link SQLiteDatabase} for accessing CRUD operations on db.
     */
    private SQLiteDatabase mDatabase;

    /**
     * {@link PersonDatabaseHelper} instance for creating readable/writable db access.
     */
    private PersonDatabaseHelper mDatabaseHelper;

    /**
     * Needed application {@link Context}.
     */
    //TODO: make local, if not needed as member
    private Context mContext;

    /**
     * Constructs a new {@link PersonDao} instance and the {@link PersonDatabaseHelper} with the
     * given {@link Context}.
     *
     * @param context needed for the {@link PersonDao#mDatabaseHelper}.
     */
    public PersonDao(Context context) {
        mContext = context;
        mDatabaseHelper = new PersonDatabaseHelper(context);
    }

    /**
     * Opens the {@link PersonDao#mDatabase} in readable state.
     */
    public void openReadable() {
        try {
            mDatabase = mDatabaseHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error while opening readable DB", e);
        }
    }

    /**
     * Opens the {@link PersonDao#mDatabase} in writable state.
     */
    public void openWritable() {
        try {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error while opening writable DB", e);
        }
    }

    /**
     * Closes the {@link PersonDao#mDatabase}.
     */
    public void close() {
        if (mDatabaseHelper != null) {
            mDatabaseHelper.close();
        } else {
            Log.w(TAG, "Could not close PersonDatabaseHelper because it was null");
        }
    }

    /**
     * Helper method to convert a cursor object into a person.
     *
     * @param cursor given cursor.
     * @return person object or null.
     */
    private Person cursorToPerson(Cursor cursor) {
        Person person = new Person();
        person.set_id(cursor.getInt(0));
        person.setCreateTime(cursor.getLong(1));
        person.setUpdateTime(cursor.getLong(2));
        person.setName(cursor.getString(3));
        person.setAddress(cursor.getString(4));
        person.setPhone(cursor.getString(5));
        person.setEmail(cursor.getString(6));
        person.setPrice(cursor.getString(7));
        person.setMisc(cursor.getString(8));
        person.setCategory(cursor.getInt(9));
        person.setActive(cursor.getInt(10) == 1);
        person.setPictureUrl(cursor.getString(11));
        person.setPosition(cursor.getInt(12));
        person.setTabText(cursor.getString(13));
        return person;
    }

    /**
     * Creates a new {@link Person} in the database.
     *
     * @param person given model object.
     * @return created person.
     * @throws NullPointerException when given person is null.
     */
    public Person create(Person person) throws NullPointerException {
        if (person != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PersonTable.COLUMN_CREATETIME, new Date().getTime());
            contentValues.put(PersonTable.COLUMN_NAME, person.getName());
            contentValues.put(PersonTable.COLUMN_ADDRESS, person.getAddress());
            contentValues.put(PersonTable.COLUMN_PHONE, person.getPhone());
            contentValues.put(PersonTable.COLUMN_EMAIL, person.getEmail());
            contentValues.put(PersonTable.COLUMN_PRICE, person.getPrice());
            contentValues.put(PersonTable.COLUMN_MISC, person.getMisc());
            contentValues.put(PersonTable.COLUMN_CATEGORY, person.getCategory());
            contentValues.put(PersonTable.COLUMN_ACTIVE, person.isActive());
            contentValues.put(PersonTable.COLUMN_PICTUREURL, person.getPictureUrl());
            contentValues.put(PersonTable.COLUMN_POSITION, person.getPosition());
            contentValues.put(PersonTable.COLUMN_TABTEXT, person.getTabText());
            long insertId = mDatabase.insert(PersonTable.TABLE_PERSON, null, contentValues);
            if (insertId > 0) {
                return findById((int) insertId);
            }
        }
        throw new NullPointerException("Person must not be null when creating");
    }

    /**
     * Imports a given {@link Person}. The difference in creation is that we simply take all given
     * values and save them to database without any logic (e.g. creation date etc.).
     * @return persisted person.
     */
    public Person importPerson(Person person) {
        if (person != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PersonTable.COLUMN_CREATETIME, person.getCreateTime());
            contentValues.put(PersonTable.COLUMN_LASTUPDATE, person.getUpdateTime());
            contentValues.put(PersonTable.COLUMN_NAME, person.getName());
            contentValues.put(PersonTable.COLUMN_ADDRESS, person.getAddress());
            contentValues.put(PersonTable.COLUMN_PHONE, person.getPhone());
            contentValues.put(PersonTable.COLUMN_EMAIL, person.getEmail());
            contentValues.put(PersonTable.COLUMN_PRICE, person.getPrice());
            contentValues.put(PersonTable.COLUMN_MISC, person.getMisc());
            contentValues.put(PersonTable.COLUMN_CATEGORY, person.getCategory());
            contentValues.put(PersonTable.COLUMN_ACTIVE, person.isActive());
            contentValues.put(PersonTable.COLUMN_PICTUREURL, person.getPictureUrl());
            contentValues.put(PersonTable.COLUMN_POSITION, person.getPosition());
            contentValues.put(PersonTable.COLUMN_TABTEXT, person.getTabText());
            long insertId = mDatabase.insert(PersonTable.TABLE_PERSON, null, contentValues);
            if (insertId > 0) {
                return findById((int) insertId);
            }
        }
        throw new NullPointerException("Person must not be null when importing");
    }

    /**
     * @return all Persons from the db.
     */
    public List<Person> findAll() {
        List<Person> persons = new ArrayList<>();
        Cursor cursor = mDatabase.query(PersonTable.TABLE_PERSON, PersonTable.ALL_COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Person person = cursorToPerson(cursor);
            persons.add(person);
            cursor.moveToNext();
        }
        cursor.close();
        return persons;
    }

    /**
     * @return all active {@link Person}s from the db.
     */
    public List<Person> findAllActive() {
        List<Person> persons = new ArrayList<>();
        Cursor cursor = mDatabase.query(PersonTable.TABLE_PERSON, PersonTable.ALL_COLUMNS,
                PersonTable.COLUMN_ACTIVE + "=1", null, null, null, PersonTable.COLUMN_POSITION);
        if (cursor != null) {
            boolean moveSuccess = cursor.moveToFirst();
            if (moveSuccess) {
                while (!cursor.isAfterLast()) {
                    Person person = cursorToPerson(cursor);
                    persons.add(person);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return persons;
    }

    /**
     * @return number of entries in database.
     */
    public long countAll() {
        return DatabaseUtils.queryNumEntries(mDatabase, PersonTable.TABLE_PERSON);
    }

    /**
     * @return number of active entries in database.
     */
    public long countActive() {
        return DatabaseUtils.queryNumEntries(mDatabase, PersonTable.TABLE_PERSON, PersonTable.COLUMN_ACTIVE + "=1");
    }

    /**
     * @return number of inactive entries in database.
     */
    public long countInActive() {
        return DatabaseUtils.queryNumEntries(mDatabase, PersonTable.TABLE_PERSON, PersonTable.COLUMN_ACTIVE + "=0");
    }

    /**
     * @return all inactive {@link Person}s from the db.
     */
    public List<Person> findAllInactive() {
        List<Person> persons = new ArrayList<>();
        Cursor cursor = mDatabase.query(PersonTable.TABLE_PERSON, PersonTable.ALL_COLUMNS,
                PersonTable.COLUMN_ACTIVE + "=0", null, null, null, null);
        if (cursor != null) {
            boolean moveSuccess = cursor.moveToFirst();
            if (moveSuccess) {
                while (!cursor.isAfterLast()) {
                    Person person = cursorToPerson(cursor);
                    persons.add(person);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return persons;
    }

    /**
     * Method to find a specific {@link Person} from the db by its id.
     *
     * @param id given id of Person.
     * @return desired Person or null.
     * @throws IllegalArgumentException when given id is < 1.
     */
    private Person findById(int id) throws IllegalArgumentException {
        if (id > 0) {
            Person person = null;
            Cursor cursor = mDatabase.query(PersonTable.TABLE_PERSON, PersonTable.ALL_COLUMNS,
                    PersonTable.COLUMN_ID + "=" + id, null, null, null, null);
            if (cursor != null) {
                boolean moveSuccess = cursor.moveToFirst();
                if (moveSuccess) {
                    person = cursorToPerson(cursor);
                } else {
                    Log.w(TAG, "cursor move to first not succeeded");
                }
                cursor.close();
            } else {
                Log.e(TAG, "cursor is null");
            }
            return person;
        }
        throw new IllegalArgumentException("Id must not be < 1 when querying");
    }

    /**
     * Method to find a specific {@link Person} from the db by its position.
     *
     * @param pos given position of Person.
     * @return desired Person or null.
     * @throws IllegalArgumentException when given pos is < 1.
     */
    public Person findByPos(int pos) throws IllegalArgumentException {
        if (pos >= 0) {
            Person person = null;
            Cursor cursor = mDatabase.query(PersonTable.TABLE_PERSON, PersonTable.ALL_COLUMNS,
                    PersonTable.COLUMN_POSITION + "=" + pos, null, null, null, null);
            if (cursor != null) {
                boolean moveSuccess = cursor.moveToFirst();
                if (moveSuccess) {
                    person = cursorToPerson(cursor);
                } else {
                    Log.w(TAG, "cursor move to first not succeeded");
                }
                cursor.close();
            } else {
                Log.e(TAG, "cursor is null");
            }
            return person;
        }
        throw new IllegalArgumentException("Position must not be < 0 when querying");
    }

    /**
     * Method to update a Person.
     *
     * @param person given Person with new information.
     * @return updated Person.
     * @throws NullPointerException     when given person is null.
     * @throws IllegalArgumentException when given id of person is <= 0
     */
    public Person update(Person person) throws NullPointerException {
        if (person != null) {
            if (person.get_id() > 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PersonTable.COLUMN_CREATETIME, person.getCreateTime());
                contentValues.put(PersonTable.COLUMN_LASTUPDATE, new Date().getTime());
                contentValues.put(PersonTable.COLUMN_NAME, person.getName());
                contentValues.put(PersonTable.COLUMN_ADDRESS, person.getAddress());
                contentValues.put(PersonTable.COLUMN_PHONE, person.getPhone());
                contentValues.put(PersonTable.COLUMN_EMAIL, person.getEmail());
                contentValues.put(PersonTable.COLUMN_PRICE, person.getPrice());
                contentValues.put(PersonTable.COLUMN_MISC, person.getMisc());
                contentValues.put(PersonTable.COLUMN_CATEGORY, person.getCategory());
                contentValues.put(PersonTable.COLUMN_ACTIVE, person.isActive());
                contentValues.put(PersonTable.COLUMN_PICTUREURL, person.getPictureUrl());
                contentValues.put(PersonTable.COLUMN_POSITION, person.getPosition());
                contentValues.put(PersonTable.COLUMN_TABTEXT, person.getTabText());
                int numberOfRows = mDatabase.update(PersonTable.TABLE_PERSON, contentValues,
                        PersonTable.COLUMN_ID + "=" + person.get_id(), null);
                Person retPerson = findById(person.get_id());
                if (numberOfRows > 1) {
                    Log.w(TAG, "more than one person updated");
                } else if (numberOfRows < 1) {
                    Log.w(TAG, "no person updated");
                }/* else {
                    Log.d(TAG, "updated Person with id "+retPerson.get_id());
                }*/
                return retPerson;
            }
            throw new IllegalArgumentException("Id of person must be > 0 when updating");
        }
        throw new NullPointerException("Person must not be null when updating");
    }

    /**
     * Method to delete a Person.
     *
     * @param person given person to delete.
     * @return true if successfully deleted (also if more than one Person), false otherwise.
     * @throws NullPointerException when given person is null.
     */
    public boolean delete(Person person) throws NullPointerException {
        if (person != null) {
            int numberOfRowsAffected = mDatabase.delete(PersonTable.TABLE_PERSON,
                    PersonTable.COLUMN_ID + "=" + person.get_id(), null);
            if (numberOfRowsAffected < 0) {
                Log.w(TAG, "no person deleted");
                return false;
            } else if (numberOfRowsAffected > 1) {
                Log.w(TAG, "more than one person deleted");
                return true;
            } else {
                Log.d(TAG, "deleted person with id " + person.get_id());
                return true;
            }
        }
        throw new NullPointerException("Person must not be null when deleting");
    }

    //TODO: test and document
    public int deleteAll() {
        return mDatabase.delete(PersonTable.TABLE_PERSON, null, null);
    }

    /**
     * Method to soft delete a person.
     * This will set its status to false (db stores a zero) and set its position to 0.
     *
     * @param person given person.
     * @return soft deleted person.
     */
    public Person deactivate(Person person) throws NullPointerException {
        if (person != null) {
            person.setActive(false);
            person.setPosition(0);
            return update(person);
        }
        throw new NullPointerException("Person must not be null when soft deleting");
    }

    /**
     * Method to permanently delete all inactive Persons from the db.
     *
     * @return number of deleted Persons.
     */
    public int deleteAllInactive() {
        return mDatabase.delete(PersonTable.TABLE_PERSON, PersonTable.COLUMN_ACTIVE + "=0", null);
    }
}
