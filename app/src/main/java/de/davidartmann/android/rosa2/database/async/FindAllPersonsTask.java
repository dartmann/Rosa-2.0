package de.davidartmann.android.rosa2.database.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.eventbus.EventBus;

import java.util.List;

import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.eventbus.event.FoundAllActivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.FoundAllArchivedPersonEvent;


/**
 * AsyncTask for loading all {@link Person} from database.
 * When the task has finished its work the given {@link EventBus} is used to fire an
 * {@link de.davidartmann.android.rosa2.util.eventbus.event.FoundAllActivePersonEvent} with the given list of {@link Person}s.
 * Created by david on 30.08.16.
 */
public class FindAllPersonsTask extends AsyncTask<Void, Integer, List<Person>> {

    private Context mContext;
    private EventBus mEventBus;
    private PersonDao personDao;
    private boolean mFindActive;

    public FindAllPersonsTask(Context context, EventBus eventBus, boolean findActive) {
        mContext = context;
        mEventBus = eventBus;
        mFindActive = findActive;
    }

    @Override
    protected List<Person> doInBackground(Void... voids) {
        personDao = new PersonDao(mContext);
        personDao.openReadable();
        if (mFindActive) {
            return personDao.findAllActive();
        } else {
            return personDao.findAllInactive();
        }

    }

    @Override
    protected void onPostExecute(List<Person> persons) {
        super.onPostExecute(persons);
        personDao.close();
        if (mFindActive) {
            mEventBus.post(new FoundAllActivePersonEvent(persons));
        } else {
            mEventBus.post(new FoundAllArchivedPersonEvent(persons));
        }
    }
}
