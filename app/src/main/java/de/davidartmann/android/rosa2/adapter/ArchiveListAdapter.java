package de.davidartmann.android.rosa2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.davidartmann.android.rosa2.adapter.viewholder.ArchiveListViewholder;
import de.davidartmann.android.rosa2.database.async.DeletePersonsTask;
import de.davidartmann.android.rosa2.database.async.UnarchivePersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveViewHolderCheckedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.DeleteOrUnarchiveEvent;

/**
 * Adapter class for the recyclerview of the mainlist.
 * Created by david on 24.08.16.
 */
public class ArchiveListAdapter extends RecyclerView.Adapter<ArchiveListViewholder> {

    private static final String TAG = ArchiveListAdapter.class.getSimpleName();

    private int mLayout;
    private List<Person> mPersons;
    private List<Person> mPersonsFiltered;
    private Context mContext;
    private EventBus mEventBus;
    private List<Person> mSelectedPers;

    public ArchiveListAdapter(int layout, Context context, EventBus eventBus) {
        mLayout = layout;
        mPersons = new ArrayList<>();
        mPersonsFiltered = new ArrayList<>();
        mContext = context;
        mEventBus = eventBus;
        mEventBus.register(this);
        mSelectedPers = new ArrayList<>();
    }

    @Override
    public ArchiveListViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ArchiveListViewholder(LayoutInflater.from(mContext).inflate(mLayout, parent, false),
                mContext, mEventBus);
    }

    @Override
    public void onBindViewHolder(ArchiveListViewholder holder, int position) {
        try {
            holder.assignData(mPersonsFiltered.get(position)/*mPersons.get(position)*/);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Unknown category while assigning data to viewholder", e);
        }
    }

    @Override
    public int getItemCount() {
        return mPersonsFiltered.size()/*mPersons.size()*/;
    }

    public void setItems(List<Person> persons) {
        if (persons != null) {
            mPersons.clear();
            mPersons.addAll(persons);
            mPersonsFiltered.clear();
            mPersonsFiltered.addAll(persons);
            notifyDataSetChanged();
        } else {
            Log.d(TAG, "given list was null");
        }
    }

    @Subscribe
    private void listen(ArchiveViewHolderCheckedEvent event) {
        for (Person p : mPersons) {
            if (p.get_id() == event.getPerson().get_id()) {
                if (event.isChecked()) {
                    mSelectedPers.add(p);
                } else {
                    if (mSelectedPers.contains(p)) {
                        mSelectedPers.remove(p);
                    }
                }
            }
        }
    }

    @Subscribe
    private void listen(DeleteOrUnarchiveEvent event) {
        if (!event.isStart()) {
            mSelectedPers.clear();
        }
    }

    /**
     * Compares all {@link Person} in {@link #mSelectedPers} with items in {@link #mPersons}.
     * If an id matches we add this person to a list which will then be used from {@link DeletePersonsTask}
     * to delete the matching persons from db.
     * Afterwards they are removed from the adapters list and notifyDataSetChanged is called.
     */
    public void deleteOrUnarchiveSelectedPersons(boolean isDeletion) {
        //TODO: is this comparison really necessary? (-> don't think so...)
        List<Person> toDeleteOrUnarchive = new ArrayList<>(mSelectedPers.size());
        if (mSelectedPers.size() > 0) {
            for (Person pDel : mSelectedPers) {
                for (Person p : mPersons) {
                    if (pDel.get_id() == p.get_id()) {
                        toDeleteOrUnarchive.add(pDel);
                    }
                }
            }
            if (isDeletion) {
                Log.d(TAG, "start DeletePersonsTask for " + toDeleteOrUnarchive.size() + " persons");
                new DeletePersonsTask(mContext, mEventBus, toDeleteOrUnarchive).execute();
            } else {
                Log.d(TAG, "start UnarchivePersonsTask for " + toDeleteOrUnarchive.size() + " persons");
                new UnarchivePersonsTask(mContext, mEventBus, toDeleteOrUnarchive).execute();
            }
            mPersons.removeAll(toDeleteOrUnarchive);
            notifyDataSetChanged();
        }
    }

    /**
     * Called from {@link de.davidartmann.android.rosa2.activity.ArchiveActivity} when user types in
     * text to search for.
     * @param newText actual typed text.
     */
    public void search(String newText) {
        if (!newText.isEmpty()) {
            for (Person p : mPersons) {
                int pos = mPersonsFiltered.indexOf(p);
                if (!p.getName().trim().toLowerCase().contains(newText.trim().toLowerCase())) {
                    if (pos != -1) {
                        mPersonsFiltered.remove(pos);
                        notifyItemRemoved(pos);
                    }
                } else {
                    if (pos == -1) {
                        mPersonsFiltered.add(p);
                        notifyItemInserted(pos);
                    }
                }
            }
        } else {
            //mPersonsFiltered = new ArrayList<>(mPersons);
            mPersonsFiltered.clear();
            mPersonsFiltered.addAll(mPersons);
            notifyDataSetChanged();
        }
    }
}
