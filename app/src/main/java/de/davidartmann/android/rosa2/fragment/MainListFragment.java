package de.davidartmann.android.rosa2.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.adapter.MainListAdapter;
import de.davidartmann.android.rosa2.database.async.FindAllPersonsTask;
import de.davidartmann.android.rosa2.util.ItemTouchHelperCallback;
import de.davidartmann.android.rosa2.util.eventbus.EventBusHelper;
import de.davidartmann.android.rosa2.util.eventbus.event.FoundAllActivePersonEvent;

/**
 * Fragment for the listview in the MainActivity.
 */
public class MainListFragment extends Fragment {

    private MainListAdapter mMainListAdapter;
    private RecyclerView mRecyclerView;
    private EventBus mEventBus;

    public MainListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_list, container, false);
        Context context = getContext();
        if (mEventBus == null) {
            mEventBus = EventBusHelper.INSTANCE.getInstance(context);
        }
        mEventBus.register(this);

        ItemTouchHelper.Callback cb = new ItemTouchHelperCallback(context);
        ItemTouchHelper touchHelper = new ItemTouchHelper(cb);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_main_list_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mMainListAdapter = new MainListAdapter(R.layout.cardview_mainlist, view.getContext(),
                mEventBus, cb);
        mRecyclerView.setAdapter(mMainListAdapter);
        touchHelper.attachToRecyclerView(mRecyclerView);

        new FindAllPersonsTask(context, mEventBus, true).execute();

        if (savedInstanceState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(
                    savedInstanceState.getParcelable(getString(R.string.scroll_state_id_main_list)));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.scroll_state_id_main_list), mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Subscribe
    public void listen(FoundAllActivePersonEvent event) {
        if (isAdded()) {
            mMainListAdapter.setItems(event.getPersons());
        }
    }
}
