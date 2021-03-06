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
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveDialogConfirmedEvent;

/**
 * DialogFragment for confirming the archiving of a person.
 * Created by david on 04.10.16.
 */
public class ConfirmArchiveDialog extends DialogFragment {

    private boolean mIsChecked;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_archive_person_title)
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
                    settings.edit().putBoolean(getString(R.string.archive_dialog_pref), !isChecked())
                            .apply();
                    EventBusHelper.INSTANCE.getInstance(context)
                            .post(new ArchiveDialogConfirmedEvent());
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
        return mIsChecked;
    }

    public void setChecked(boolean mIsChecked) {
        this.mIsChecked = mIsChecked;
    }
}
