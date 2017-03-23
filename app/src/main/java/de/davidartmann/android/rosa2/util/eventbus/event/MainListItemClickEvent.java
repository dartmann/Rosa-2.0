package de.davidartmann.android.rosa2.util.eventbus.event;

import android.view.View;

import de.davidartmann.android.rosa2.database.model.Person;


/**
 * Event for a click on an item in the main list in
 * {@link de.davidartmann.android.rosa2.fragment.MainListFragment}.
 * Created by david on 24.08.16.
 */
public class MainListItemClickEvent {

    private Person mPerson;
    private View mView;

    public MainListItemClickEvent(Person person, View view) {
        mPerson = person;
        mView = view;
    }

    public Person getPerson() {
        return mPerson;
    }

    public void setPerson(Person person) {
        this.mPerson = person;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }
}
