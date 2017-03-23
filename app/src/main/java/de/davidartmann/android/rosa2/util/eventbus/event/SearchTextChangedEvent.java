package de.davidartmann.android.rosa2.util.eventbus.event;

/**
 * Event which is fired, when the text of the search has changed.
 * Created by david on 28.10.16.
 */
public class SearchTextChangedEvent {

    private String mNewText;

    public SearchTextChangedEvent(String newText) {
        mNewText = newText;
    }

    public String getNewText() {
        return mNewText;
    }

    public void setNewText(String mNewText) {
        this.mNewText = mNewText;
    }
}
