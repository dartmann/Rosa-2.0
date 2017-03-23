package de.davidartmann.android.rosa2.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.database.async.ArchivePersonsTask;
import de.davidartmann.android.rosa2.database.async.PersistPersonsTask;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.dialog.ConfCancelEditDialog;
import de.davidartmann.android.rosa2.dialog.ConfirmArchiveDialog;
import de.davidartmann.android.rosa2.util.FileHelper;
import de.davidartmann.android.rosa2.util.RoundedTransformation;
import de.davidartmann.android.rosa2.util.eventbus.EventBusHelper;
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveDialogConfirmedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.CancelEditDialogConfirmedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.DecMainListPersAtPosEvent;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EditActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 3;

    private EditText mEditTextName;
    private EditText mEditTextPhone;
    private EditText mEditTextEmail;
    private EditText mEditTextAddress;
    private EditText mEditTextPrice;
    private EditText mEditTextMisc;
    private ImageView mImageView;
    private Person mPerson;
    private EventBus mEventBus;
    private RadioButton mRadioBtnSure;
    private RadioButton mRadioBtnAlmostSure;
    private RadioButton mRadioBtnUnsure;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mEditTextName = (EditText) findViewById(R.id.activity_edit_name);
        mEditTextPhone = (EditText) findViewById(R.id.activity_edit_phone);
        mEditTextEmail = (EditText) findViewById(R.id.activity_edit_email);
        mEditTextAddress = (EditText) findViewById(R.id.activity_edit_address);
        mEditTextPrice = (EditText) findViewById(R.id.activity_edit_price);
        mEditTextMisc = (EditText) findViewById(R.id.activity_edit_misc);
        mImageView = (ImageView) findViewById(R.id.activity_edit_picture);
        mRadioBtnSure = (RadioButton) findViewById(R.id.radioButtonSure);
        mRadioBtnAlmostSure = (RadioButton) findViewById(R.id.radioButtonAlmostSure);
        mRadioBtnUnsure = (RadioButton) findViewById(R.id.radioButtonUnsure);
        mRadioGroup = (RadioGroup) findViewById(R.id.activity_edit_radiogroup);
        mImageView.setOnClickListener(this);
        mRadioBtnSure.setOnClickListener(this);
        mRadioBtnAlmostSure.setOnClickListener(this);
        mRadioBtnUnsure.setOnClickListener(this);
        findViewById(R.id.activity_edit_fab_save).setOnClickListener(this);
        setUpEventBus();
        checkForArgs();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        if (getTitle().equals(getString(R.string.edit_person))) {
            menu.findItem(R.id.action_edit_archive).setVisible(true);
            invalidateOptionsMenu();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String editDialogPref = getString(R.string.edit_dialog_pref);
        switch (item.getItemId()) {
            case android.R.id.home:
                //TODO:        overridePendingTransition();
                // if we want to override the navigate back animation
                if (shouldShowCancelEditDialog(editDialogPref)) {
                    showCancelEditDialog();
                } else {
                    //mIsCancelled = true;
                    startMainActivity(true);
                }
                return true;
            case R.id.action_edit_save:
                saveProcedure();
                return true;
            case R.id.action_edit_cancel:
                if (shouldShowCancelEditDialog(editDialogPref)) {
                    showCancelEditDialog();
                } else {
                    //mIsCancelled = true;
                    startMainActivity(true);
                }
                return true;
            case R.id.action_edit_archive:
                if (shouldShowArchivingDialog(getString(R.string.archive_dialog_pref))) {
                    showArchiveDialog();
                } else {
                    archivePerson();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_edit_picture:
                showPictureChooserDialog();
                break;
            case R.id.activity_edit_fab_save:
                saveProcedure();
                break;
            case R.id.radioButtonSure:
                mPerson.setCategory(Person.SURE);
                break;
            case R.id.radioButtonAlmostSure:
                mPerson.setCategory(Person.ALMOST_SURE);
                break;
            case R.id.radioButtonUnsure:
                mPerson.setCategory(Person.UNSURE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                //Credits to:
                //  http://no-magic.info/development-for-android-os/get-image-path-from-device-gallery.html
                String filePath = null;
                if (Build.VERSION.SDK_INT < 19) {
                    final Uri selectedImgUri = data.getData();
                    final String[] filePathCol = {MediaStore.Images.Media.DATA};
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(selectedImgUri, filePathCol, null,
                                null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            final int columnIndex = cursor.getColumnIndex(filePathCol[0]);
                            filePath = cursor.getString(columnIndex);
                            cursor.close();
                        }
                    } finally {
                        if (cursor != null) {
                            if (!cursor.isClosed()) {
                                cursor.close();
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "API version > 19\n--------------------------");
                    FileHelper fileHlpr = new FileHelper();
                    final Uri selectedImgUri = data.getData();
                    //final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // Check for the freshest data.
                    getContentResolver().takePersistableUriPermission(selectedImgUri,
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                    if (fileHlpr.isExtStorageDoc(selectedImgUri)) {
                        Log.d(TAG, "isExtStorageDoc()");
                        final String docId = DocumentsContract.getDocumentId(selectedImgUri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                        }
                    } else if (fileHlpr.isDownloadDoc(selectedImgUri)) {
                        Log.d(TAG, "isDownloadDoc()");
                        final String docId = DocumentsContract.getDocumentId(selectedImgUri);
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                        filePath = fileHlpr.getDataColumn(this, contentUri, null, null);
                    } else if (fileHlpr.isMediaDoc(selectedImgUri)) {
                        Log.d(TAG, "isMediaDoc()");
                        final String docId = DocumentsContract.getDocumentId(selectedImgUri);
                        final String[] split = docId.split(":");
                        final String type = split[0];

                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        final String selection = "_id=?";
                        final String[] selectionArgs = new String[]{
                                split[1]
                        };
                        filePath = fileHlpr.getDataColumn(this, contentUri, selection, selectionArgs);
                    } else if ("content".equalsIgnoreCase(selectedImgUri.getScheme())) {
                        Log.d(TAG, "content");
                        filePath = fileHlpr.getDataColumn(getBaseContext(), selectedImgUri, null, null);
                    } else if ("file".equalsIgnoreCase(selectedImgUri.getScheme())) {
                        Log.d(TAG, "file");
                        filePath = selectedImgUri.getPath();
                    }
                }
                //in case of further problems check this:
                //  http://stackoverflow.com/questions/2789276/android-get-real-path-by-uri-getpath#9989900
                Log.d(TAG, "filePath: " + filePath);
                if (filePath != null && !filePath.equals("")) {
                    Picasso.with(this)
                            .load(new File(filePath))
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .transform(new RoundedTransformation(0, R.dimen.edit_activity_image_dimen))
                            .resizeDimen(R.dimen.edit_activity_image_dimen,
                                    R.dimen.edit_activity_image_dimen)
                            .centerCrop()
                            .into(mImageView);
                    mPerson.setPictureUrl(filePath);
                } else {
                    Log.d(TAG, "filePath was null or empty after onActivityResult()");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(createImagePicker(), REQUEST_IMAGE_PICK);
                }
                break;
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

    /**
     * Calls {@link #startMainActivity(boolean)}.
     * This happens when the {@link ConfCancelEditDialog} is confirmed.
     *
     * @param event which is fired.
     */
    @Subscribe
    private void listen(CancelEditDialogConfirmedEvent event) {
        //mIsCancelled = true;
        startMainActivity(true);
    }

    /**
     * Calls {@link #startMainActivity(boolean)}
     * This happens when the {@link ConfirmArchiveDialog} is confirmed.
     *
     * @param event which is fired.
     */
    @Subscribe
    private void listen(ArchiveDialogConfirmedEvent event) {
        if (!isDestroyed() && !isFinishing()) {
            archivePerson();
        }
    }

    private boolean shouldShowArchivingDialog(String archiveDialogPref) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(EditActivity.this);
        if (!settings.contains(archiveDialogPref)) {
            settings.edit().putBoolean(archiveDialogPref, true).apply();
            return true;
        } else {
            return settings.getBoolean(archiveDialogPref, true);
        }
    }

    private boolean shouldShowCancelEditDialog(String editDialogPref) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(EditActivity.this);
        if (!settings.contains(editDialogPref)) {
            settings.edit().putBoolean(editDialogPref, true).apply();
            return true;
        } else {
            return settings.getBoolean(editDialogPref, true);
        }
    }

    /**
     * Adds {@link #mPerson} to a list, starts a new {@link ArchivePersonsTask} and calls
     * {@link #startMainActivity(boolean)}.
     */
    private void archivePerson() {
        Log.d(TAG, "will archive person: " + mPerson.getName() + " with position: " + mPerson.getPosition());
        List<Person> persons = new ArrayList<>(1);
        persons.add(mPerson);
        new ArchivePersonsTask(this, mEventBus, persons).execute();
        //this is now positioned here after calling ArchivePersonsTask
        // additionally the list of archived persons is not used, so no problem at all
        //mEventBus.post(new EndArchivePersonEvent(persons));
        mEventBus.post(new DecMainListPersAtPosEvent(mPerson.getPosition()));
        startMainActivity(false);
    }

    /**
     * Validates the input of the view and if valid, saves the person to db and returns to the
     * {@link MainActivity}.
     */
    private void saveProcedure() {
        if (validateInput()) {
            savePerson();
            //mIsCancelled = false;
            startMainActivity(true);
        }
    }

    /**
     * Shows the {@link ConfCancelEditDialog}.
     */
    private void showCancelEditDialog() {
        ConfCancelEditDialog dialog = new ConfCancelEditDialog();
        dialog.show(getSupportFragmentManager(), ConfCancelEditDialog.class.getSimpleName());
    }

    /**
     * Shows the {@link ConfirmArchiveDialog}.
     */
    private void showArchiveDialog() {
        ConfirmArchiveDialog dialog = new ConfirmArchiveDialog();
        dialog.show(getSupportFragmentManager(), ConfirmArchiveDialog.class.getSimpleName());
    }

    /**
     * Checks for valid {@link EditText} fields. This contains the {@link #mEditTextName} only.
     * If the editText is empty, it gets a warning message to inform the user.
     *
     * @return true if name is not empty, false otherwise.
     */
    private boolean validateInput() {
        if (mEditTextName.getText().toString().isEmpty()) {
            mEditTextName.setError(getString(R.string.name_must_not_be_empty));
            return false;
        }
        if (!mRadioBtnUnsure.isChecked() && !mRadioBtnAlmostSure.isChecked()
                && !mRadioBtnSure.isChecked()) {
            ScrollView scrollView = (ScrollView) findViewById(R.id.activity_edit_scrollview);
            scrollView.smoothScrollTo(0, scrollView.getBottom());
            mRadioGroup.animate()
                    .alpha(0.1f)
                    .setDuration(500)
                    .setInterpolator(new LinearInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mRadioGroup.animate()
                                    .alpha(1.0f)
                                    .setDuration(500)
                                    .setInterpolator(new LinearInterpolator())
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRadioGroup.animate()
                                                    .alpha(0.1f)
                                                    .setDuration(500)
                                                    .setInterpolator(new LinearInterpolator())
                                                    .withEndAction(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mRadioGroup.animate()
                                                                    .alpha(1.0f)
                                                                    .setDuration(500)
                                                                    .setInterpolator(new LinearInterpolator());
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
            return false;
        }
        return true;
    }

    private void setUpEventBus() {
        mEventBus = EventBusHelper.INSTANCE.getInstance(this);
        mEventBus.register(this);
    }

    private void startMainActivity(boolean isActive) {
        Intent intent = new Intent(this, MainActivity.class);
        if (isActive) {
            intent.putExtra(getString(R.string.person_bundle_id), mPerson);
        }
        startActivity(intent);
    }

    private void savePerson() {
        mPerson.setName(mEditTextName.getText().toString());
        mPerson.setAddress(mEditTextAddress.getText().toString());
        mPerson.setPhone(mEditTextPhone.getText().toString());
        mPerson.setEmail(mEditTextEmail.getText().toString());
        mPerson.setPrice(mEditTextPrice.getText().toString());
        mPerson.setMisc(mEditTextMisc.getText().toString());
        mPerson.setActive(true);
        //position is set when person is newly created or by bundle
        List<Person> persons = new ArrayList<>(1);
        persons.add(mPerson);
        new PersistPersonsTask(this, mEventBus, persons).execute();
    }

    private void showPictureChooserDialog() {
        String[] text = new String[]{"Aus Galerie auswählen", "Bild löschen"};
        String[] items;
        if (mPerson.getPictureUrl() == null || mPerson.getPictureUrl().isEmpty()) {
            items = new String[]{text[0]};
        } else {
            items = new String[]{text[0], text[1]};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Optionen");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, items);
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {// image choosing from gallery
                    requestReadExternalStorage();
                    dialog.dismiss();
                } else {// image deletion
                    File file = new File(mPerson.getPictureUrl());
                    boolean delResult = file.delete();
                    Log.d(TAG, "deletion of picture result: " + delResult);
                    int dimen = R.dimen.fragment_detail_image_dimen;
                    Picasso.with(EditActivity.this)
                            .load(R.drawable.ic_account_circle_black_24dp)
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .transform(new RoundedTransformation(0, Math.round(getResources().getDimension(dimen))))
                            .resizeDimen(dimen, dimen)
                            .centerCrop()
                            .into(mImageView);
                    mPerson.setPictureUrl(null);
                    //we do not save this state -> only via "Speichern" of Fab or Menu
                    /*
                    List<Person> persons = new ArrayList<>(1);
                    persons.add(mPerson);
                    new PersistPersonsTask(EditActivity.this, mEventBus, persons).execute();
                    */
                    dialog.dismiss();
                }
            }
        });
        builder.create().show();
    }

    private void requestReadExternalStorage() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Explain to the user why we need to read the contacts (NOT NEEDED!)
                //if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                //        Manifest.permission.READ_EXTERNAL_STORAGE)) {}
                ActivityCompat.requestPermissions(this, new String[]{permission},
                        EditActivity.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
            } else {
                startActivityForResult(createImagePicker(), EditActivity.REQUEST_IMAGE_PICK);
            }
        } else {
            startActivityForResult(createImagePicker(), EditActivity.REQUEST_IMAGE_PICK);
        }
    }

    private Intent createImagePicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            return Intent.createChooser(intent, "Bild auswählen");
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            return intent;//Intent.createChooser(intent, "Bild auswählen");
        }
    }

    private void checkForArgs() {
        Bundle bundle = getIntent().getExtras();
        int numberPers = 0;
        if (bundle != null) {
            if (bundle.containsKey(getString(R.string.num_persons_bundle_id))) {
                numberPers = bundle.getInt(getString(R.string.num_persons_bundle_id));
            }
            if (bundle.containsKey(getString(R.string.person_bundle_id))) {
                setTitle(getString(R.string.edit_person));
                mPerson = (Person) bundle.getSerializable(getString(R.string.person_bundle_id));
            }
            if (mPerson == null) {
                setTitle(getString(R.string.create_new_person));
                mPerson = new Person();
                mPerson.setPosition(numberPers);
            }
            assignData(mPerson);
        }
    }

    private Person assignData(Person person) {
        int dimen = R.dimen.fragment_detail_image_dimen;
        if (person.getPictureUrl() == null) {
            Picasso.with(this)
                    .load(R.drawable.ic_account_circle_black_24dp)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new RoundedTransformation(0, Math.round(getResources().getDimension(dimen))))
                    .resizeDimen(dimen, dimen)
                    .centerCrop()
                    .into(mImageView);
        } else {
            Picasso.with(this)
                    .load(new File(person.getPictureUrl()))
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new RoundedTransformation(0, Math.round(getResources().getDimension(dimen))))
                    .resizeDimen(dimen, dimen)
                    .centerCrop()
                    .into(mImageView);
        }
        mEditTextName.setText(person.getName());
        mEditTextPhone.setText(person.getPhone());
        mEditTextEmail.setText(person.getEmail());
        mEditTextAddress.setText(person.getAddress());
        mEditTextPrice.setText(person.getPrice());
        mEditTextMisc.setText(person.getMisc());
        switch (person.getCategory()) {
            case Person.SURE:
                mRadioBtnSure.setChecked(true);
                break;
            case Person.ALMOST_SURE:
                mRadioBtnAlmostSure.setChecked(true);
                break;
            case Person.UNSURE:
                mRadioBtnUnsure.setChecked(true);
                break;
        }
        return person;
    }
}
