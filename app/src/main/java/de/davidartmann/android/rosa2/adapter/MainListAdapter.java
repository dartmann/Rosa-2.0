package de.davidartmann.android.rosa2.adapter;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.adapter.viewholder.MainListViewholder;
import de.davidartmann.android.rosa2.database.async.ArchivePersonsTask;
import de.davidartmann.android.rosa2.database.async.PersistPersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.MyItemTouchHelper;
import de.davidartmann.android.rosa2.util.eventbus.event.DecMainListPersAtPosEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.EndArchivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.InitArchivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.SearchTextChangedEvent;

/**
 * Adapter class for the recyclerview of the mainlist.
 * Created by david on 24.08.16.
 */
public class MainListAdapter extends RecyclerView.Adapter<MainListViewholder> {

    private static final String TAG = MainListAdapter.class.getSimpleName();

    private int mLayout;
    private List<Person> mPersons;
    private List<Person> mPersonsFiltered;
    private Context mContext;
    private EventBus mEventBus;
    private MyItemTouchHelper mMyItemTouchHelper;

    public MainListAdapter(int layout, Context context, EventBus eventBus,
                           ItemTouchHelper.Callback cb) {
        mLayout = layout;
        mPersons = new ArrayList<>();
        mPersonsFiltered = new ArrayList<>();
        mContext = context;
        mEventBus = eventBus;
        mEventBus.register(this);
        mMyItemTouchHelper = (MyItemTouchHelper) cb;
        ((MyItemTouchHelper) cb).setMainListAdapter(this);
    }

    @Override
    public MainListViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MainListViewholder(LayoutInflater.from(mContext).inflate(mLayout, parent, false),
                mContext, mEventBus);
    }

    @Override
    public void onBindViewHolder(MainListViewholder holder, int position) {
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

    @Subscribe
    private void listen(InitArchivePersonEvent event) {
        mPersons.remove(event.getPerson());
        notifyItemRemoved(event.getPerson().getPosition());
        showSnackBar(mContext.getString(R.string.person_archived), event.getView(), event.getPerson(),
                event.getPerson().getPosition());
    }

    @Subscribe
    private void listen(DecMainListPersAtPosEvent event) {
        int pos = event.getPosition();
        //TODO: here a indexOutOfBounds is possible! -> check why
        mPersons.remove(mPersons.get(pos));
        notifyItemRemoved(pos);
        Log.d(TAG, "will iterate persons and dec maybe");
        for (Person p : mPersons) {
            if (p.getPosition() > event.getPosition()) {
                p.setPosition(p.getPosition() - 1);
                Log.d(TAG, "pers: " + p.getName() + ", with new pos: " + p.getPosition());
            }
        }
        new PersistPersonsTask(mContext, mEventBus, mPersons).execute();
        mEventBus.post(new EndArchivePersonEvent(mPersons));
    }

    /**
     * Called from the {@link de.davidartmann.android.rosa2.util.MyItemTouchHelper#onSwiped(RecyclerView.ViewHolder, int)}.
     * This is superior to the EventBus callback method, which had the problem of IOOB.
     *
     * @param viewHolder which got swiped.
     * @param direction  direction of swipe.
     */
    public void swipe(RecyclerView.ViewHolder viewHolder, int direction) {
        //we do not ask for confirmation here, because swiping requires more than a simple click
        // and thus when done, we claim the user knows what he did... (although he can undo the action)
        mMyItemTouchHelper.setSwipeEnabled(false);
        int position = viewHolder.getAdapterPosition();
        Person person = mPersons.get(position);
        mPersons.remove(person);
        notifyItemRemoved(position);
        if (direction == ItemTouchHelper.START) {       //left swipe -> archiving
            showSnackBar(mContext.getString(R.string.person_archived), viewHolder.itemView,
                    person, position);
        } else {
            Log.w(TAG, "Unknown swipe direction: " + direction);
        }
        logPos();
    }

    private void showSnackBar(String snackText, View view,
                              final Person person, final int position) {
        Snackbar.make(view, snackText, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPersons.add(position, person);
                        notifyItemInserted(position);
                        mMyItemTouchHelper.setSwipeEnabled(true);
                    }
                })
                .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            List<Person> toArchive = new ArrayList<>(1);
                            toArchive.add(person);
                            new ArchivePersonsTask(mContext, mEventBus, toArchive).execute();
                            for (Person p : mPersons) {
                                if (p.getPosition() > position) {
                                    p.setPosition(p.getPosition() - 1);
                                    Log.d(TAG, "pers: " + p.getName() + ", with new pos: " + p.getPosition());
                                }
                            }
                            new PersistPersonsTask(mContext, mEventBus, mPersons).execute();
                            mEventBus.post(new EndArchivePersonEvent(mPersons));
                            mMyItemTouchHelper.setSwipeEnabled(true);
                        }
                    }
                }).show();
    }

    public void move(int fromPos, int toPos) {
        if (fromPos < toPos) {
            for (int i = fromPos; i < toPos; i++) {
                if (i == fromPos) {
                    mPersons.get(i).setPosition(toPos);
                    mPersons.get(toPos).setPosition(i);
                } else {
                    mPersons.get(i).setPosition(i - 1);
                }
                Collections.swap(mPersons, i, i + 1);
            }
        } else if (fromPos > toPos) {
            for (int i = fromPos; i > toPos; i--) {
                if (i == fromPos) {
                    mPersons.get(i).setPosition(toPos);
                    mPersons.get(toPos).setPosition(i);
                } else {
                    mPersons.get(i).setPosition(i + 1);
                }
                Collections.swap(mPersons, i, i - 1);
            }
        } else {
            Log.w(TAG, "Listitem moved although fromPos == toPos");
        }
        notifyItemMoved(fromPos, toPos);
        new PersistPersonsTask(mContext, mEventBus, mPersons).execute();
        logPos();
    }

    //TODO: debugging
    private void logPos() {
        Log.d(TAG, "persons: " + mPersons.size() + "");
        for (Person p : mPersons) {
            Log.d(TAG, p.getName() + ", " + p.getPosition() + "");
        }
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
    private void listen(SearchTextChangedEvent event) {
        String newText = event.getNewText();
        if (!event.getNewText().isEmpty()) {
            //Log.d(TAG, "text="+newText);
            for (Person p : mPersons) {
                //Log.d(TAG, "person="+p.getName());
                int pos = mPersonsFiltered.indexOf(p);
                //Log.d(TAG, "position="+pos);
                if (!p.getName().trim().toLowerCase().contains(newText.trim().toLowerCase())) {
                    if (pos != -1) {
                        //Log.d(TAG, "person not matching but in list="+p.getName());
                        mPersonsFiltered.remove(pos);
                        notifyItemRemoved(pos);
                    }
                } else {
                    if (pos == -1) {
                        //Log.d(TAG, "person matching but not in list="+p.getName());
                        mPersonsFiltered.add(p);
                        //Log.d(TAG, "pre sorting");
                        //Collections.sort(mPersonsFiltered);
                        notifyItemInserted(pos);
                    }
                }
            }
        } else {
            //Log.d(TAG, "search text empty");
            mPersonsFiltered = new ArrayList<>(mPersons);
            notifyDataSetChanged();
        }
    }
}
