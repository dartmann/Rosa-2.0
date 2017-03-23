package de.davidartmann.android.rosa2.util.eventbus.event;

import android.support.v4.app.Fragment;

/**
 * Event for the fragments to inform the activity to replace the actual fragment.
 * Created by david on 18.09.16.
 */
public class AddOrReplaceFragmentEvent {

    /**
     * Fragment instance.
     */
    private Fragment mFragment;

    /**
     * States if the new fragment should be added to the framelayout container or replacing the old one.
     */
    private boolean mAdd;

    /**
     * States if the new fragment transaction should be added to the backstack for navigation.
     */
    private boolean mAddToBackStack;

    /**
     * Constructor with given {@link Fragment} instance.
     * @param fragment to replace the old one.
     * @param add should the new Fragment be added to the FrameLayout. It will be replacing the actual if false.
     */
    public AddOrReplaceFragmentEvent(Fragment fragment, boolean add, boolean addToBackStack) {
        mFragment = fragment;
        mAdd = add;
        mAddToBackStack = addToBackStack;
    }


    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public boolean ismAdd() {
        return mAdd;
    }

    public void setmAdd(boolean mAdd) {
        this.mAdd = mAdd;
    }

    public boolean ismAddToBackStack() {
        return mAddToBackStack;
    }

    public void setmAddToBackStack(boolean mAddToBackStack) {
        this.mAddToBackStack = mAddToBackStack;
    }
}
