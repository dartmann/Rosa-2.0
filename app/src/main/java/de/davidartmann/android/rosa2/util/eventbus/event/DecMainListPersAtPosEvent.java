package de.davidartmann.android.rosa2.util.eventbus.event;

/**
 * Event for also notify the {@link de.davidartmann.android.rosa2.adapter.MainListAdapter} when
 * the {@link de.davidartmann.android.rosa2.activity.EditActivity} has archived a {@link de.davidartmann.android.rosa2.database.model.Person}.
 * Without this notification we would be in an inconsistent state because the {@link java.util.List} of Persons
 * of the MainListAdapter is not aware of the single selected Person which was archived in EditActivity.
 * Created by david on 21.10.16.
 */

public class DecMainListPersAtPosEvent {

    private int mPosition;

    public DecMainListPersAtPosEvent(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }
}
