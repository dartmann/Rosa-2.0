package de.davidartmann.android.rosa2.util.eventbus.event;

import java.util.List;

import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event which is fired, when the archiving process has ended.
 * Created by david on 16.10.16.
 */

public class EndArchivePersonEvent {

    private List<Person> mPersons;

    public EndArchivePersonEvent(List<Person> persons) {
        mPersons = persons;
    }

    public List<Person> getPersons() {
        return mPersons;
    }

    public void setPersons(List<Person> mPersons) {
        this.mPersons = mPersons;
    }
}
