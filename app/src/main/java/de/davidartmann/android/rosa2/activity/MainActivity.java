package de.davidartmann.android.rosa2.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.async.FindAllPersonsTask;
import de.davidartmann.android.rosa2.database.async.ImportPersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.dialog.ConfirmArchiveDialog;
import de.davidartmann.android.rosa2.fragment.MainDetailFragment;
import de.davidartmann.android.rosa2.util.FileHelper;
import de.davidartmann.android.rosa2.util.eventbus.EventBusHelper;
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveDialogConfirmedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.EndArchivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.FoundAllActivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.ImportedPersonsEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.InitArchivePersonEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.MainListItemClickEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.SearchTextChangedEvent;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_READ_EXT_STORAGE = 0;
    private static final int ACTION_GET_CONTENT_JSON = 1;
    private static final int REQUEST_PERMISSION_WRITE_EXT_STORAGE = 2;

    private List<Person> mActivePers;
    private int mClickPos;
    private boolean mPersClicked;
    private EventBus mEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPersClicked = false;
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_list_toolbar);
        setSupportActionBar(toolbar);
        setUpEventBus();
        Person person = (Person) getIntent().getSerializableExtra(getString(R.string.person_bundle_id));
        if (person == null) {
            setUpFrameLayout(null);
        } else {
            setUpFrameLayout(person);
            mPersClicked = true;
            mClickPos = person.getPosition();
            invalidateOptionsMenu();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_list, menu);
        if (mPersClicked) {
            menu.findItem(R.id.menu_main_action_edit).setVisible(true);
            menu.findItem(R.id.menu_main_action_archive).setVisible(true);
        }
        final MenuItem searchItem = menu.findItem(R.id.menu_main_action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mEventBus.post(new SearchTextChangedEvent(newText));
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_action_dummy:
                createDummyPersons();
                new FindAllPersonsTask(this, mEventBus, true).execute();
                return true;
            case R.id.menu_main_action_delall:
                deleteAll();
                new FindAllPersonsTask(this, mEventBus, true).execute();
                return true;
            case R.id.menu_main_action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_main_action_add:
                startEditActivity(true);
                return true;
            case R.id.menu_main_action_edit:
                startEditActivity(false);
                return true;
            case R.id.menu_main_action_archive:
                startArchivingProcedure();
                return true;
            case R.id.menu_main_action_open_archive:
                startActivity(new Intent(this, ArchiveActivity.class));
                return true;
            case R.id.menu_main_action_import:
                startImport();
                return true;
            case R.id.menu_main_action_export:
                startExport();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startExport() {
        if (isExternalStorageWritable()) {
            checkWriteExtStoragePermission();
        } else {
            Toast.makeText(this, "(Externer) Speicher ist nicht schreibbar, konnte nicht exportieren",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void checkWriteExtStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission},
                        REQUEST_PERMISSION_WRITE_EXT_STORAGE);
            } else {
                writeJsonFile();
            }
        } else {
            writeJsonFile();
        }
    }

    private void writeJsonFile() {
        FileHelper fileHelper = new FileHelper();
        fileHelper.writeJsonFile(this);
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private void deleteAll() {
        PersonDao personDao = new PersonDao(this);
        personDao.openWritable();
        personDao.deleteAll();
        personDao.close();
    }

    private void startImport() {
        if (isExternalStorageReadable()) {
            checkReadExtStoragePermission();
        } else {
            Toast.makeText(this, "(Externer) Speicher nicht lesbar", Toast.LENGTH_LONG).show();
        }
    }

    private void checkReadExtStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission},
                        REQUEST_PERMISSION_READ_EXT_STORAGE);
            } else {
                showJsonFileChooser();
            }
        } else {
            showJsonFileChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_EXT_STORAGE:
                if (grantResults.length == 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Berechtigung verweigert, kein Import möglich",
                            Toast.LENGTH_LONG).show();
                } else {
                    showJsonFileChooser();
                }
                break;
            case REQUEST_PERMISSION_WRITE_EXT_STORAGE:
                if (grantResults.length == 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Berechtigung verweigert, kein Export möglich",
                            Toast.LENGTH_LONG).show();
                } else {
                    writeJsonFile();
                }
                break;
        }
    }

    private void showJsonFileChooser() {
        //String mimeType = "application/json"; //RFC 4627
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //intent.setType(mimeType);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Datei auswählen"), ACTION_GET_CONTENT_JSON);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            //intent.setType(mimeType);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, ACTION_GET_CONTENT_JSON);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACTION_GET_CONTENT_JSON) {
                String filePath = null;
                Uri selectedJsonFileUri = data.getData();
                //Log.d(TAG, "uri: " + selectedJsonFileUri);
                //Log.d(TAG, "path: " + selectedJsonFileUri.getPath());
                //Log.d(TAG, "authority: " + selectedJsonFileUri.getAuthority());
                getContentResolver().takePersistableUriPermission(selectedJsonFileUri,
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                FileHelper fileHlpr = new FileHelper();
                if (fileHlpr.isExtStorageDoc(selectedJsonFileUri)) {
                    //Log.d(TAG, "isExtStorageDoc()");
                    final String docId = DocumentsContract.getDocumentId(selectedJsonFileUri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    if (filePath != null && !filePath.equals("")) {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Person> personsFromJson = null;
                        try {
                            personsFromJson = mapper.readValue(new File(filePath),
                                    new TypeReference<List<Person>>() {
                                    });
                        } catch (IOException e) {
                            Log.d(TAG, "I/O error while deserialising JSON file", e);
                        }
                        if (personsFromJson != null) {
                            //Log.d(TAG, "Deserializing successfull with " + personsFromJson.size() + " persons");
                            new ImportPersonsTask(this, mEventBus, personsFromJson).execute();
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    private void listen(ImportedPersonsEvent event) {
        Snackbar.make(findViewById(android.R.id.content),
                event.getPersons().size() + " Personen importiert",
                Snackbar.LENGTH_LONG).show();
        for (Person p : event.getPersons()) {
            if (p.isActive()) {
                Log.d(TAG, "imported person with pos: " + p.getPosition());
            }
        }
        //new FindAllPersonsTask(this, mEventBus, true).execute();
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @Subscribe
    private void listen(ArchiveDialogConfirmedEvent event) {
        if (!isDestroyed() && !isFinishing()) {
            //Log.d(TAG, "ArchiveDialogConfirmedEvent received and processed");
            archivePerson();
        }
    }

    @Subscribe
    private void listen(DeadEvent deadEvent) {
        Log.d(MainActivity.class.getSimpleName(),
                "received dead event: " + deadEvent.getEvent() + " from: " + deadEvent.getSource());
    }

    @Subscribe
    private void listen(FoundAllActivePersonEvent event) {
        mActivePers = event.getPersons();
        if (mActivePers.size() == 0 || (mActivePers.size() > 0 && !mPersClicked)) {
            invalidateOptionsMenu();
        }
        //debugging:
        /*
        Log.d(TAG, "persons: " + mActivePers.size() + "");
        for (Person p : mActivePers) {
            Log.d(TAG, p.getName() + ", " + p.getPosition() + "");
        }
        */
    }

    @Subscribe
    private void listen(MainListItemClickEvent event) {
        if (!isDestroyed() && !isFinishing()) {
            mPersClicked = true;
            Log.d(TAG, "mainlist item clicked, set mClickPos to: " + event.getPerson().getPosition());
            mClickPos = event.getPerson().getPosition();
            invalidateOptionsMenu();
            setUpFrameLayout(event.getPerson());
        }
    }

    @Subscribe
    private void listen(EndArchivePersonEvent event) {
        setUpFrameLayout(null);
    }

    /*    for debugging     */
    private void createDummyPersons() {
        PersonDao personDao = new PersonDao(this);
        personDao.openWritable();
        personDao.deleteAll();
        Random random = new Random();
        Person person;
        for (int i = 0; i < 25; i++) {
            person = new Person();
            person.setName("Testname " + i);
            person.setPosition(i);
            person.setAddress("Testaddress " + i);
            person.setActive(true);
            person.setCategory(random.nextInt(4) == 0 ? 1 : 2);
            person.setEmail("Testemail" + i + "@test.de");
            person.setMisc("Testmisc " + i);
            person.setPhone("0123012301203");
            person.setPrice(String.valueOf(random.nextInt(5000)));
            person.setPictureUrl("/storage/emulated/0/oeha-band.de.jpg");
            personDao.create(person);
        }
        personDao.close();
    }

    private void startEditActivity(boolean addPerson) {
        Intent intent = new Intent(this, EditActivity.class);
        //Log.d(TAG, "number of persons: " + mActivePers.size() + "\nclickpos: " + mClickPos);
        if (addPerson) {
            intent.putExtra(getString(R.string.num_persons_bundle_id), mActivePers.size());
        } else if (mActivePers.size() > mClickPos) {
            Log.d(TAG, "persons: " + mActivePers.size() + ", clickpos: " + mClickPos);
            intent.putExtra(getString(R.string.person_bundle_id), mActivePers.get(mClickPos));
            Log.d(TAG, "person: " + mActivePers.get(mClickPos).getName() + " added for editing");
        } else {
            Log.w(TAG, "Persons list size <= clickPos -> not correct!");
        }
        startActivity(intent);
    }

    /**
     * Sets up the FrameLayout of MainActivity. Here we need to stay synchrone to avoid
     * Android Lifecycle problems. So we first check the Preferences if a value is true, which
     * is set true once in the EditActivity when the first Person gets stored to db.
     */
    private void setUpFrameLayout(Person person) {
        MainDetailFragment fragment;
        if (person != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(getString(R.string.person_bundle_id), person);
            fragment = MainDetailFragment.createInstance(bundle);
        } else {
            fragment = new MainDetailFragment();
        }
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_main_framelayout, fragment)
                .disallowAddToBackStack()
                .commit();
    }

    private void setUpEventBus() {
        mEventBus = EventBusHelper.INSTANCE.getInstance(this);
        mEventBus.register(this);
    }

    private void archivePerson() {
        mEventBus.post(new InitArchivePersonEvent(mActivePers.get(mClickPos), this.findViewById(android.R.id.content)));
    }

    private void startArchivingProcedure() {
        if (shouldShowArchiveConfirmDialog(getString(R.string.archive_dialog_pref))) {
            showArchiveConfDialog();
        } else {
            archivePerson();
        }
    }

    private boolean shouldShowArchiveConfirmDialog(String archiveDialogPref) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (!settings.contains(archiveDialogPref)) {
            settings.edit().putBoolean(archiveDialogPref, true).apply();
            return true;
        } else {
            return settings.getBoolean(archiveDialogPref, true);
        }
    }

    private void showArchiveConfDialog() {
        ConfirmArchiveDialog dialog = new ConfirmArchiveDialog();
        dialog.show(getSupportFragmentManager(), ConfirmArchiveDialog.class.getSimpleName());
    }
}
