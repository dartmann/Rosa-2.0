package de.davidartmann.android.rosa2.util.eventbus.event;

import android.view.View;

import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event when a person should be archived. This is fired from {@link de.davidartmann.android.rosa2.activity.MainActivity}
 * when user clicks menu button of toolbar. {@link de.davidartmann.android.rosa2.adapter.MainListAdapter}
 * listens to it and handles the events.
 * Created by david on 16.10.16.
 */

public class InitArchivePersonEvent {

    private Person mPerson;
    private View mView;

    public InitArchivePersonEvent(Person person, View view) {
        this.mPerson = person;
        this.mView = view;
    }

    public Person getPerson() {
        return mPerson;
    }

    public void setPerson(Person mPerson) {
        this.mPerson = mPerson;
    }

    public View getView() {
        return mView;
    }

    public void setView(View mView) {
        this.mView = mView;
    }
}
