package de.davidartmann.android.rosa2.util.eventbus.event;

import java.util.List;

import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event for the importing of Persons from JSON.
 * Created by david on 23.10.16.
 */
public class ImportedPersonsEvent {

    private List<Person> mPersons;

    public ImportedPersonsEvent(List<Person> persons) {
        mPersons = persons;
    }

    public List<Person> getPersons() {
        return mPersons;
    }

    public void setPersons(List<Person> mPersons) {
        this.mPersons = mPersons;
    }
}
