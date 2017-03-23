package de.davidartmann.android.rosa2.database.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.eventbus.EventBus;

import java.util.List;

import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.eventbus.event.DeletedPersonEvent;


/**
 * AsyncTask for the deletion of a given number of Persons.
 * Created by david on 18.09.16.
 */
public class DeletePersonsTask extends AsyncTask<Void, Integer, int[]> {

    private Context mContext;
    private EventBus mEventBus;
    private PersonDao personDao;
    private List<Person> mPersons;
    private int[] mDelPositions;

    public DeletePersonsTask(Context context, EventBus eventBus, List<Person> persons) {
        mContext = context;
        mEventBus = eventBus;
        mPersons = persons;
        mDelPositions = new int[mPersons.size()];
    }

    @Override
    protected int[] doInBackground(Void... voids) {
        personDao = new PersonDao(mContext);
        personDao.openWritable();
        for (int i = 0; i < mPersons.size(); i++) {
            boolean success = personDao.delete(mPersons.get(i));
            mDelPositions[i] = success ? mPersons.get(i).getPosition() : -1;
        }
        return mDelPositions;
    }

    @Override
    protected void onPostExecute(int[] delPositions) {
        super.onPostExecute(delPositions);
        personDao.close();
        mEventBus.post(new DeletedPersonEvent(delPositions));
    }
}
