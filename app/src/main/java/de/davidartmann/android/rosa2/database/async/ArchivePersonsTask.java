package de.davidartmann.android.rosa2.database.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.eventbus.EventBus;

import java.util.List;

import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.eventbus.event.EndArchivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.PersistedPersonEvent;


/**
 * AsyncTask for archiving a given List of Persons.
 * Created by david on 19.09.16.
 */
public class ArchivePersonsTask extends AsyncTask<Void, Integer, List<Person>> {

    private Context mContext;
    private EventBus mEventBus;
    private PersonDao personDao;
    private List<Person> mPersons;

    public ArchivePersonsTask(Context context, EventBus eventBus, List<Person> persons) {
        mContext = context;
        mEventBus = eventBus;
        mPersons = persons;
    }

    @Override
    protected List<Person> doInBackground(Void... voids) {
        personDao = new PersonDao(mContext);
        personDao.openWritable();
        for (Person p : mPersons) {
            if (p.get_id() > 0) {
                mPersons.add(personDao.deactivate(p));
            }
        }
        return mPersons;
    }

    @Override
    protected void onPostExecute(List<Person> persons) {
        super.onPostExecute(persons);
        personDao.close();
        //this is now positioned in EditActivity and MainListAdapter after calling this AsyncTasc
        // additionally the list of archived persons is not used, so no problem at all
        //mEventBus.post(new EndArchivePersonEvent(persons));
    }
}
