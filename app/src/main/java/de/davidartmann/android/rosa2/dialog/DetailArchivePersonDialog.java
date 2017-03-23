package de.davidartmann.android.rosa2.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Dialog for showing the details of person from archive.
 * Created by david on 19.10.16.
 */
@Deprecated
public class DetailArchivePersonDialog extends DialogFragment {

    private TextView mTvAddress;
    private TextView mTvEmail;
    private TextView mTvMisc;
    private TextView mTvPrice;
    private TextView mTvPhone;
    private Context mContext;

    public static DetailArchivePersonDialog createInstance(Bundle bundle) {
        DetailArchivePersonDialog dialog = new DetailArchivePersonDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(getString(R.string.detail_archive_person_dialog_person_id))) {
                Person person = (Person) bundle.getSerializable(
                        getString(R.string.detail_archive_person_dialog_person_id));
                if (person != null) {
                    builder.setTitle(person.getName());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View view = inflater.inflate(R.layout.bottom_sheet_archive, null);
                    mTvAddress = (TextView) view.findViewById(R.id.bottom_sheet_address);
                    mTvEmail = (TextView) view.findViewById(R.id.bottom_sheet_email);
                    mTvMisc = (TextView) view.findViewById(R.id.bottom_sheet_misc);
                    mTvPrice = (TextView) view.findViewById(R.id.bottom_sheet_price);
                    mTvPhone = (TextView) view.findViewById(R.id.bottom_sheet_phone);
                    if (!isWhatsAppInstalled()) {
                        mTvPhone.setCompoundDrawablesWithIntrinsicBounds(
                                mContext.getDrawable(R.drawable.ic_call_black_24dp), null, null, null);
                    }
                    setUpView(person);
                    builder.setView(view);
                }
            }
        }
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public void setUpView(Person person) {
        mTvAddress.setText(person.getAddress());
        mTvEmail.setText(person.getEmail());
        mTvMisc.setText(person.getMisc());
        mTvPrice.setText(person.getPrice());
        mTvPhone.setText(person.getPhone());
    }

    /**
     * Checks if the Whatsapp App is installed.
     *
     * @return true if so, false otherwise.
     */
    private boolean isWhatsAppInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo("com.whatsapp",
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}
