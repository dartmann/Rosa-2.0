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


/**
 * AsyncTask for storing one or more {@link Person}s to database.
 * Created by david on 16.09.16.
 */
public class PersistPersonsTask extends AsyncTask<Void, Integer, List<Person>> {

    private Context mContext;
    private EventBus mEventBus;
    private PersonDao personDao;
    private List<Person> mPersons;

    public PersistPersonsTask(Context context, EventBus eventBus, List<Person> persons) {
        mContext = context;
        mEventBus = eventBus;
        mPersons = persons;
    }

    @Override
    protected List<Person> doInBackground(Void... voids) {
        personDao = new PersonDao(mContext);
        personDao.openWritable();
        List<Person> persistedPersons = new ArrayList<>();
        for (Person p : mPersons) {
            if (p.get_id() == 0) {
                persistedPersons.add(personDao.create(p));
            } else {
                persistedPersons.add(personDao.update(p));
            }
        }
        return persistedPersons;
    }

    @Override
    protected void onPostExecute(List<Person> persons) {
        super.onPostExecute(persons);
        personDao.close();
        //TODO: no one listens to this...
        //  => maybe delete
        //mEventBus.post(new PersistedPersonEvent(persons));
    }
}
