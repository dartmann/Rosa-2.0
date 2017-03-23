package de.davidartmann.android.rosa2.util.eventbus.event;

/**
 * Event which is fired when the {@link de.davidartmann.android.rosa2.activity.ArchiveActivity} starts or ends the deletion process.
 * This procedure consist of the displaying/hiding of checkboxes on all viewholders.
 * Created by david on 17.10.16.
 */

public class DeleteOrUnarchiveEvent {

    private boolean mStart;

    public DeleteOrUnarchiveEvent(boolean start) {
        mStart = start;
    }

    public boolean isStart() {
        return mStart;
    }
}
