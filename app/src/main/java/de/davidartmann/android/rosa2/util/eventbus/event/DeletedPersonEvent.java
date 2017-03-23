package de.davidartmann.android.rosa2.util.eventbus.event;

import java.util.List;

import de.davidartmann.android.rosa2.database.async.DeletePersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Event which is fired after {@link DeletePersonsTask}
 * has finished its work.
 * Created by david on 18.09.16.
 */
public class DeletedPersonEvent {

    /**
     * Holds all positions of deleted persons.
     * Could also be -1 if the deletion of a person was unsuccessful.
     */
    private int[] mDelPositions;

    public DeletedPersonEvent(int[] delPersons) {
        mDelPositions = delPersons;
    }

    /**
     * @return {@link #mDelPositions}.
     */
    public int[] getDelPositions() {
        return mDelPositions;
    }

    public void setDelPositions(int[] delPositions) {
        this.mDelPositions = delPositions;
    }
}
