package de.davidartmann.android.rosa2.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.adapter.ArchiveListAdapter;
import de.davidartmann.android.rosa2.database.async.FindAllPersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.dialog.ConfDelOrUnarchiveDialog;
import de.davidartmann.android.rosa2.util.eventbus.EventBusHelper;
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveViewHolderClickedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.DeleteOrUnarchiveDialogConfirmedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.DeleteOrUnarchiveEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.DeletedPersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.FoundAllArchivedPersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.SearchTextChangedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.UnarchivedPersonEvent;

public class ArchiveActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ArchiveActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ArchiveListAdapter mAdapter;
    private EventBus mEventBus;
    private boolean mLayoutCheckable;
    private FloatingActionButton mFabDel;
    private FloatingActionButton mFabUnarchive;
    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        mLayoutCheckable = false;
        mEventBus = EventBusHelper.INSTANCE.getInstance(this);
        mEventBus.register(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_archive_recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new ArchiveListAdapter(R.layout.cardview_archivelist_checkable, this, mEventBus);
        mRecyclerView.setAdapter(mAdapter);
        mFabDel = (FloatingActionButton) findViewById(R.id.activity_archive_fab_delete);
        mFabUnarchive = (FloatingActionButton) findViewById(R.id.activity_archive_fab_unarchive);
        mFabDel.setOnClickListener(this);
        mFabUnarchive.setOnClickListener(this);
        findViewById(R.id.bottom_sheet_fab).setOnClickListener(this);

        new FindAllPersonsTask(this, mEventBus, false).execute();

        if (savedInstanceState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(
                    savedInstanceState.getParcelable(getString(R.string.scroll_state_id_archive_list)));
        }

        LinearLayout bottomSheetViewgroup
                = (LinearLayout) findViewById(R.id.bottom_sheet_linearlayout);
        mBottomSheetBehavior =
                BottomSheetBehavior.from(bottomSheetViewgroup);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventBus.register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable(getString(R.string.scroll_state_id_archive_list),
                mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_archive, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_archive_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.search(newText);
                return true;
            }
        });
        if (mLayoutCheckable) {
            menu.findItem(R.id.action_archive_unarchive).setVisible(false);
            menu.findItem(R.id.action_archive_del).setVisible(false);
            searchItem.setVisible(false);
        } else {
            menu.findItem(R.id.action_archive_cancel).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_archive_unarchive:
                if (!mLayoutCheckable) {
                    mLayoutCheckable = true;
                    mEventBus.post(new DeleteOrUnarchiveEvent(true));
                    invalidateOptionsMenu();
                    mFabUnarchive.show();
                }
                break;
            case R.id.action_archive_del:
                if (!mLayoutCheckable) {
                    mLayoutCheckable = true;
                    mEventBus.post(new DeleteOrUnarchiveEvent(true));
                    invalidateOptionsMenu();
                    mFabDel.show();
                }
                break;
            case R.id.action_archive_cancel:
                if (mLayoutCheckable) {
                    mLayoutCheckable = false;
                    mEventBus.post(new DeleteOrUnarchiveEvent(false));
                    invalidateOptionsMenu();
                    mFabDel.hide();
                    mFabUnarchive.hide();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(ArchiveActivity.this);
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.activity_archive_fab_delete:
                bundle.putBoolean(getString(R.string.dialog_is_for_deletion_id), true);
                String deleteDialogPref = getString(R.string.delete_dialog_pref);
                if (shouldShowDeleteConfDialog(settings, deleteDialogPref)) {
                    Log.d(TAG, "deletion dialog should be displayed...");
                    showDeletionOrUnarchiveDialog(bundle);
                } else {
                    delOrUnarchiveSelectedPers(true);
                }
                //mEventBus.post(new DeleteOrUnarchiveEvent(true));
                break;
            case R.id.activity_archive_fab_unarchive:
                bundle.putBoolean(getString(R.string.dialog_is_for_deletion_id), false);
                String unarchiveDialogPref = getString(R.string.unarchive_dialog_pref);
                if (shouldShowUnarchiveConfDialog(settings, unarchiveDialogPref)) {
                    showDeletionOrUnarchiveDialog(bundle);
                } else {
                    delOrUnarchiveSelectedPers(false);
                }
                //mEventBus.post(new DeleteOrUnarchiveEvent(true));
                break;
            case R.id.bottom_sheet_fab:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
        }
    }

    @Subscribe
    private void listen(ArchiveViewHolderClickedEvent event) {
        Person person = event.getPerson();
        TextView tvName = (TextView) findViewById(R.id.bottom_sheet_name);
        TextView tvAddress = (TextView) findViewById(R.id.bottom_sheet_address);
        TextView tvEmail = (TextView) findViewById(R.id.bottom_sheet_email);
        TextView tvMisc = (TextView) findViewById(R.id.bottom_sheet_misc);
        TextView tvPrice = (TextView) findViewById(R.id.bottom_sheet_price);
        TextView tvPhone = (TextView) findViewById(R.id.bottom_sheet_phone);
        tvName.setText(person.getName());
        tvAddress.setText(person.getAddress());
        tvEmail.setText(person.getEmail());
        tvMisc.setText(person.getMisc());
        tvPrice.setText(person.getPrice());
        tvPhone.setText(person.getPhone());
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Subscribe
    private void listen(FoundAllArchivedPersonEvent event) {
        if (!isFinishing() && !isDestroyed()) {
            mAdapter.setItems(event.getPersons());
        }
    }

    @Subscribe
    private void listen(DeleteOrUnarchiveDialogConfirmedEvent event) {
        if (!isFinishing() && !isDestroyed()) {
            delOrUnarchiveSelectedPers(event.isDeletion());
        }
    }

    private void delOrUnarchiveSelectedPers(boolean isDeletion) {
        mAdapter.deleteOrUnarchiveSelectedPersons(isDeletion);
        mEventBus.post(new DeleteOrUnarchiveEvent(true));
        resetMenu();
    }

    @Subscribe
    private void listen(DeletedPersonEvent event) {
        int size = event.getDelPositions().length;
        String snackBarMsg = size + " ";
        if (size == 1) {
            snackBarMsg += getString(R.string.person_deleted);
        } else {
            snackBarMsg += getString(R.string.persons_deleted);
        }
        Snackbar.make(this.findViewById(android.R.id.content), snackBarMsg, Snackbar.LENGTH_SHORT)
                .show();
        resetMenu();
    }

    @Subscribe
    private void listen(UnarchivedPersonEvent event) {
        String snackMsg = event.getPersons().size() + " ";
        if (event.getPersons().size() == 1) {
            snackMsg += getString(R.string.person_unarchived);
        } else {
            snackMsg += getString(R.string.persons_unarchived);
        }
        Snackbar.make(this.findViewById(android.R.id.content), snackMsg, Snackbar.LENGTH_SHORT)
                .show();
        resetMenu();
    }

    private boolean shouldShowUnarchiveConfDialog(SharedPreferences settings, String unarchiveDialogPref) {
        if (!settings.contains(unarchiveDialogPref)) {
            settings.edit().putBoolean(unarchiveDialogPref, true).apply();
            return true;
        } else {
            return settings.getBoolean(unarchiveDialogPref, true);
        }
    }

    private boolean shouldShowDeleteConfDialog(SharedPreferences settings, String deleteDialogPref) {
        if (!settings.contains(deleteDialogPref)) {
            settings.edit().putBoolean(deleteDialogPref, true).apply();
            return true;
        } else {
            return settings.getBoolean(deleteDialogPref, true);
        }
    }

    private void resetMenu() {
        mLayoutCheckable = false;
        invalidateOptionsMenu();
        mFabDel.hide();
        mFabUnarchive.hide();
    }

    private void showDeletionOrUnarchiveDialog(Bundle bundle) {
        ConfDelOrUnarchiveDialog dialog = ConfDelOrUnarchiveDialog.createInstance(bundle);
        dialog.show(getSupportFragmentManager(), ConfDelOrUnarchiveDialog.class.getSimpleName());
    }
}
