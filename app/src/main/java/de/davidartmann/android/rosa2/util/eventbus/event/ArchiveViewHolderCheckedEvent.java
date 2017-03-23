package de.davidartmann.android.rosa2.util.eventbus.event;

import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event for telling the ArchiveListAdapter to add/remove checked/unchecked persons from his internal list.
 * Created by david on 17.10.16.
 */

public class ArchiveViewHolderCheckedEvent {

    private Person mPerson;

    private boolean mChecked;

    public ArchiveViewHolderCheckedEvent(Person person, boolean checked) {
        mPerson = person;
        mChecked = checked;
    }

    public Person getPerson() {
        return mPerson;
    }

    public void setPerson(Person mPerson) {
        this.mPerson = mPerson;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }
}
