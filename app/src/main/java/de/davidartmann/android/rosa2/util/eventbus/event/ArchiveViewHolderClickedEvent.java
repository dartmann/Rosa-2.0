package de.davidartmann.android.rosa2.util.eventbus.event;


import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event for when the CardView of the Archive is clicked.
 * Created by david on 28.10.16.
 */
public class ArchiveViewHolderClickedEvent {

    private Person mPerson;

    public ArchiveViewHolderClickedEvent(Person person) {
        mPerson = person;
    }

    public Person getPerson() {
        return mPerson;
    }

    public void setPerson(Person mPerson) {
        this.mPerson = mPerson;
    }
}
