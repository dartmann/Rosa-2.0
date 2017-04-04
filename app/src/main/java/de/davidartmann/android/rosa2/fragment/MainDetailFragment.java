package de.davidartmann.android.rosa2.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.RoundedTransformation;

/**
 * Fragment for the detail view of a chosen mPerson of mainlist.
 * Created by david on 03.10.16.
 */
public class MainDetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainDetailFragment.class.getSimpleName();

    private static final int REQUEST_PERMISSION_CALL = 1;
    private static final int REQUEST_PERMISSION_READ_CONTACTS = 2;

    private static final int ACTION_PICK_CONTACT = 3;
    private static final String WHATSAPP_PKG_NAME = "com.whatsapp";

    private ImageView mImageView;
    private TextView mTvName;
    private TextView mTvPhone;
    private TextView mTvEmail;
    private TextView mTvAddress;
    private TextView mTvPrice;
    private TextView mTvCategory;
    private TextView mTvMisc;
    private Context mContext;
    private Person mPerson;

    public static MainDetailFragment createInstance(Bundle bundle) {
        MainDetailFragment fragment = new MainDetailFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        View view;
        Bundle bundle = getArguments();
        if (bundle != null) {
            view = inflater.inflate(R.layout.fragment_main_detail, container, false);
            mImageView = (ImageView) view.findViewById(R.id.fragment_detail_picture);
            mTvName = (TextView) view.findViewById(R.id.fragment_detail_name);
            mTvPhone = (TextView) view.findViewById(R.id.fragment_detail_phone);
            mTvEmail = (TextView) view.findViewById(R.id.fragment_detail_email);
            mTvAddress = (TextView) view.findViewById(R.id.fragment_detail_address);
            mTvPrice = (TextView) view.findViewById(R.id.fragment_detail_price);
            mTvCategory = (TextView) view.findViewById(R.id.fragment_detail_category);
            mTvMisc = (TextView) view.findViewById(R.id.fragment_detail_misc);
            assignData(bundle);
        } else {
            view = inflater.inflate(R.layout.fragment_main_detail_empty, container, false);
        }
        return view;
    }

    private void assignData(Bundle bundle) {
        mPerson = (Person) bundle.getSerializable(getString(R.string.person_bundle_id));
        if (mPerson != null) {
            int dimen = R.dimen.fragment_detail_image_dimen;
            if (mPerson.getPictureUrl() == null) {
                Picasso.with(mContext)
                        .load(R.drawable.ic_account_circle_black_24dp)
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .transform(new RoundedTransformation(0, Math.round(getResources().getDimension(dimen))))
                        .resizeDimen(dimen, dimen)
                        .centerCrop()
                        .into(mImageView);
            } else {
                Picasso.with(mContext)
                        .load(new File(mPerson.getPictureUrl()))
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .transform(new RoundedTransformation(0, Math.round(getResources().getDimension(dimen))))
                        .resizeDimen(dimen, dimen)
                        .centerCrop()
                        .into(mImageView);
            }
            mTvName.setText(mPerson.getName());
            if (mPerson.getPhone() != null && !mPerson.getPhone().isEmpty()) {
                mTvPhone.setText(mPerson.getPhone());
                mTvPhone.setOnClickListener(this);
                if (!isWhatsAppInstalled()) {
                    mTvPhone.setCompoundDrawablesWithIntrinsicBounds(
                            mContext.getDrawable(R.drawable.ic_call_black_24dp), null, null, null);
                }
            }
            if (mPerson.getEmail() != null && !mPerson.getEmail().isEmpty()) {
                mTvEmail.setText(mPerson.getEmail());
                mTvEmail.setOnClickListener(this);
            }
            if (mPerson.getAddress() != null && !mPerson.getAddress().isEmpty()) {
                mTvAddress.setText(mPerson.getAddress());
                mTvAddress.setOnClickListener(this);
            }
            mTvPrice.setText(mPerson.getPrice());
            switch (mPerson.getCategory()) {
                case Person.SURE:
                    mTvCategory.setText(getString(R.string.sure));
                    break;
                case Person.ALMOST_SURE:
                    mTvCategory.setText(getString(R.string.almost_sure));
                    break;
                case Person.UNSURE:
                    mTvCategory.setText(getString(R.string.unsure));
                    break;
            }
            mTvMisc.setText(mPerson.getMisc());
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.fragment_detail_phone:
                if (isWhatsAppInstalled()) {
                    openWhatsappContactByNumber(setUpPhoneNr());
                } else {
                    checkCallPermission();
                }
                break;
            case R.id.fragment_detail_address:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + mPerson.getAddress()));
                startActivity(Intent.createChooser(intent, "Navigieren mit"));
                break;
            case R.id.fragment_detail_email:
                intent = new Intent(Intent.ACTION_SEND);
                //intent.setType("text/plain");
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mPerson.getEmail()});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, "Email versenden mit"));
                break;
        }
    }

    /**
     * Checks if calling is allowed by user.
     */
    private void checkCallPermission() {
        String permission = Manifest.permission.CALL_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission},
                        REQUEST_PERMISSION_CALL);
            } else {
                makeCallIntent();
            }
        } else {
            makeCallIntent();
        }
    }

    /**
     * Not used at the moment, but kept for later purpose of importing the desired contact number
     * and name in our database (?).
     */
    private void checkReadContactsPermission() {
        String permission = Manifest.permission.READ_CONTACTS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission},
                        REQUEST_PERMISSION_READ_CONTACTS);
            } else {
                makeContactChooserIntent();
            }
        } else {
            makeContactChooserIntent();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PERMISSION_CALL) {
                makeCallIntent();
            } else if (requestCode == ACTION_PICK_CONTACT) {    //not used at the moment
                Cursor cursor = null;
                try {
                    cursor = mContext.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            /*
                            data4 has the intern. phone nr. (+49...)
                            account_type has "com.whatsapp"
                             */

                            while (!cursor.isAfterLast()) {
                                for (int i = 0; i < cursor.getColumnCount(); i++) {
                                    Log.d(TAG, "cursor col: " + cursor.getColumnName(i));
                                    Log.d(TAG, "cursor val: " + cursor.getString(i));
                                }
                                Log.d(TAG, "==================================");
                                cursor.moveToNext();
                            }

                            /*
                            String phoneNr = cursor.getString(cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Log.d(TAG, "Selected PhoneNr: " + phoneNr);
                            openWhatsappContactByNumber(phoneNr);
                            */
                        }
                    }
                } finally {
                    if (cursor != null) {
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }
                    }
                }
            } else if (requestCode == REQUEST_PERMISSION_READ_CONTACTS) {   //not used at the moment
                makeContactChooserIntent();
            }
        }
    }

    private void openWhatsappContactByNumber(String phoneNr) {
        Uri uri = Uri.parse("smsto:" + phoneNr);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        //intent.putExtra("sms_body", "test");
        intent.setPackage(WHATSAPP_PKG_NAME);
        startActivity(intent);
    }

    /**
     * Creates the Intent for calling the given number.
     */
    private void makeCallIntent() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + setUpPhoneNr()));
        startActivity(intent);
    }

    private void makeContactChooserIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, ACTION_PICK_CONTACT);
    }

    /**
     * Replaces the leading "0" of number starting with "01" by "+49".
     *
     * @return newly created String.
     */
    private String setUpPhoneNr() {
        String phoneNr = mPerson.getPhone();
        if (phoneNr.startsWith("0")) {
            if (phoneNr.startsWith("01")) {
                phoneNr = phoneNr.replaceFirst("0", "+49");
            }
        }
        return phoneNr;
    }

    /**
     * Checks if the Whatsapp App is installed.
     *
     * @return true if so, false otherwise.
     */
    private boolean isWhatsAppInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo(WHATSAPP_PKG_NAME,
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}
