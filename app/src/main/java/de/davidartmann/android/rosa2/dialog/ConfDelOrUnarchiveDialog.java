package de.davidartmann.android.rosa2.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.util.eventbus.EventBusHelper;
import de.davidartmann.android.rosa2.util.eventbus.event.DeleteOrUnarchiveDialogConfirmedEvent;

/**
 * DialogFragment for confirming the deletion of persons from the archive.
 * Created by david on 04.10.16.
 */
public class ConfDelOrUnarchiveDialog extends DialogFragment {

    private boolean mChecked;

    public static ConfDelOrUnarchiveDialog createInstance(Bundle bundle) {
        ConfDelOrUnarchiveDialog instance = new ConfDelOrUnarchiveDialog();
        instance.setArguments(bundle);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        Bundle bundle = getArguments();
        String title = "";
        if (bundle != null) {
            String key = getString(R.string.dialog_is_for_deletion_id);
            if (bundle.containsKey(key)) {
                if (bundle.getBoolean(key)) {
                    title = getString(R.string.confirm_deletion_title);
                    return showDialog(true, title, context);
                } else {
                    title = getString(R.string.confirm_unarchiving_title);
                    return showDialog(false, title, context);
                }
            }
        }
        return null;
    }

    private AlertDialog showDialog(final boolean isDeletion, String title, final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMultiChoiceItems(new CharSequence[]{getString(R.string.not_show_any_more)},
                        null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                setChecked(isChecked);
                            }
                        })
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences settings =
                                PreferenceManager.getDefaultSharedPreferences(context);
                        if (isDeletion) {
                            settings.edit().putBoolean(getString(R.string.delete_dialog_pref), !isChecked())
                                    .apply();
                            EventBusHelper.INSTANCE.getInstance(context)
                                    .post(new DeleteOrUnarchiveDialogConfirmedEvent(true));
                        } else {
                            settings.edit().putBoolean(getString(R.string.unarchive_dialog_pref), !isChecked())
                                    .apply();
                            EventBusHelper.INSTANCE.getInstance(context)
                                    .post(new DeleteOrUnarchiveDialogConfirmedEvent(false));
                        }
                        dismiss();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean mIsChecked) {
        this.mChecked = mIsChecked;
    }
}
