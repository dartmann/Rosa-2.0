package de.davidartmann.android.rosa2.util.eventbus.event;

import java.util.List;

import de.davidartmann.android.rosa2.database.model.Person;


/**
 * Event which is fired when the {@link de.davidartmann.android.rosa2.database.async.FindAllPersonsTask}
 * is finished.
 * Created by david on 09.09.16.
 */
public class FoundAllArchivedPersonEvent {

    private List<Person> persons;

    public FoundAllArchivedPersonEvent(List<Person> foundPersons) {
        persons = foundPersons;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }
}
