package de.davidartmann.android.rosa2.util.eventbus.event;

/**
 * Event class for the confirmation of the deletion or unarchiving dialog of
 * {@link de.davidartmann.android.rosa2.activity.ArchiveActivity}.
 * Created by david on 05.10.16.
 */
public class DeleteOrUnarchiveDialogConfirmedEvent {

    private boolean mDeletion;

    public DeleteOrUnarchiveDialogConfirmedEvent(boolean deletion) {
        mDeletion = deletion;
    }

    public boolean isDeletion() {
        return mDeletion;
    }

    public void setDeletion(boolean mDeletion) {
        this.mDeletion = mDeletion;
    }
}
