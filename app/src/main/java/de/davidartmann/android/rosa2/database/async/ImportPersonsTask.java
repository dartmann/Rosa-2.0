package de.davidartmann.android.rosa2.database.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.eventbus.event.ImportedPersonsEvent;

/**
 * AsyncTask for creating the newly imported persons in database.
 * Created by david on 23.10.16.
 */

public class ImportPersonsTask extends AsyncTask<Void, Integer, List<Person>> {

    private static final String TAG = ImportPersonsTask.class.getSimpleName();

    private Context mContext;
    private EventBus mEventBus;
    private PersonDao personDao;
    private List<Person> mPersons;

    public ImportPersonsTask(Context context, EventBus eventBus, List<Person> persons) {
        mContext = context;
        mEventBus = eventBus;
        mPersons = persons;
    }

    @Override
    protected List<Person> doInBackground(Void... voids) {
        personDao = new PersonDao(mContext);
        personDao.openWritable();
        long numExistPers = personDao.countAll();
        List<Person> importedPersons = new ArrayList<>();
        for (Person p : mPersons) {
            p.setCategory(p.getCategory() + 1);
            if (p.isActive()) {
                p.setPosition((int) (numExistPers));
                numExistPers++;
            } else {
                p.setPosition(0);
            }
            importedPersons.add(personDao.importPerson(p));
        }
        return importedPersons;
    }

    @Override
    protected void onPostExecute(List<Person> persons) {
        super.onPostExecute(persons);
        personDao.close();
        mEventBus.post(new ImportedPersonsEvent(persons));
    }
}
