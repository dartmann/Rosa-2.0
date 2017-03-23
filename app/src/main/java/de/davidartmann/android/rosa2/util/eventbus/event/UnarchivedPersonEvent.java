package de.davidartmann.android.rosa2.util.eventbus.event;

import java.util.List;

import de.davidartmann.android.rosa2.database.async.PersistPersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event which is fired when the {@link de.davidartmann.android.rosa2.database.async.UnarchivePersonsTask} has finished.
 * Created by david on 16.09.16.
 */
public class UnarchivedPersonEvent {

    private List<Person> mPersons;

    public UnarchivedPersonEvent(List<Person> persons) {
        mPersons = persons;
    }

    public List<Person> getPersons() {
        return mPersons;
    }

    public void setPersons(List<Person> persons) {
        this.mPersons = persons;
    }
}
