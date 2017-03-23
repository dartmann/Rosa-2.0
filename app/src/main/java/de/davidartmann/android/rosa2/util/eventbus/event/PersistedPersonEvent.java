package de.davidartmann.android.rosa2.util.eventbus.event;

import java.util.List;

import de.davidartmann.android.rosa2.database.async.PersistPersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event which is fired when the {@link PersistPersonsTask} has finished.
 * Created by david on 16.09.16.
 */
public class PersistedPersonEvent {

    private List<Person> mPersons;

    public PersistedPersonEvent(List<Person> persons) {
        mPersons = persons;
    }

    public List<Person> getPersons() {
        return mPersons;
    }

    public void setPersons(List<Person> persons) {
        this.mPersons = persons;
    }
}
