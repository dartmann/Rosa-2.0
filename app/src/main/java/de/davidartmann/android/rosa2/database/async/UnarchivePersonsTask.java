package de.davidartmann.android.rosa2.database.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.eventbus.event.PersistedPersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.UnarchivedPersonEvent;


/**
 * AsyncTask for unarchiving one or more {@link Person}s to database.
 * Created by david on 16.09.16.
 */
public class UnarchivePersonsTask extends AsyncTask<Void, Integer, List<Person>> {

    private static final String TAG = UnarchivePersonsTask.class.getSimpleName();

    private Context mContext;
    private EventBus mEventBus;
    private PersonDao personDao;
    private List<Person> mPersToUnarchive;

    public UnarchivePersonsTask(Context context, EventBus eventBus, List<Person> persons) {
        mContext = context;
        mEventBus = eventBus;
        mPersToUnarchive = persons;
    }

    @Override
    protected List<Person> doInBackground(Void... voids) {
        personDao = new PersonDao(mContext);
        personDao.openWritable();
        //TODO: create a count() in personDao
        long offset = personDao.countActive();
        //Log.d(TAG, offset + " persons in db are active");
        //Log.d(TAG, "offset will be: " + offset);
        List<Person> persistedPersons = new ArrayList<>();
        for (int i = 0; i < mPersToUnarchive.size(); i++) {
            if (mPersToUnarchive.get(i).get_id() > 0) {
                mPersToUnarchive.get(i).setPosition((int) (offset + i));
                mPersToUnarchive.get(i).setActive(true);
                //Log.d(TAG, "set unarchiving person's position to: " + mPersToUnarchive.get(i).getPosition());
                persistedPersons.add(personDao.update(mPersToUnarchive.get(i)));
            }
        }
        return persistedPersons;
    }

    @Override
    protected void onPostExecute(List<Person> persons) {
        super.onPostExecute(persons);
        personDao.close();
        mEventBus.post(new UnarchivedPersonEvent(persons));
    }
}
