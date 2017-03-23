package de.davidartmann.android.rosa2.util.eventbus;

import android.content.Context;

import com.google.common.eventbus.EventBus;

import de.davidartmann.android.rosa2.R;

/**
 * Helper class for saving the singleton instance to one application wide {@link EventBus}.
 * Created by david on 03.10.16.
 */
public enum EventBusHelper {

    /**
     * Java best practice for singleton reference to this enum.
     */
    INSTANCE;

    /**
     * Static {@link EventBus} reference.
     */
    private static EventBus sEventBus;

    /**
     * Returns static reference to {@link EventBusHelper#sEventBus}
     * @param context for {@link Context#getString(int)} to a string constant.
     * @return static eventBus instance.
     */
    public EventBus getInstance(Context context) {
        if (sEventBus == null) {
            sEventBus = new EventBus(context.getString(R.string.eventbus_id));
        }
        return sEventBus;
    }
}
