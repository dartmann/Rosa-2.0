package de.davidartmann.android.rosa2.adapter.viewholder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.dialog.DetailArchivePersonDialog;
import de.davidartmann.android.rosa2.util.RoundedTransformation;
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveViewHolderCheckedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.ArchiveViewHolderClickedEvent;
import de.davidartmann.android.rosa2.util.eventbus.event.DeleteOrUnarchiveEvent;

/**
 * Viewholder for the main list.
 * Created by david on 23.08.16.
 */
public class ArchiveListViewholder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = ArchiveListViewholder.class.getSimpleName();

    private CardView mCardView;
    private ImageView mImageView;
    private TextView mTextViewName;
    private TextView mTextViewEmail;
    private CheckBox mCheckBox;
    private Context mContext;
    private EventBus mEventBus;
    private Person mPerson;

    public ArchiveListViewholder(View itemView, Context context, EventBus eventBus) {
        super(itemView);
        mContext = context;
        mEventBus = eventBus;
        mCardView = (CardView) itemView.findViewById(R.id.cardview_archivelist_cardview);
        mImageView = (ImageView) itemView.findViewById(R.id.cardview_archivelist_image);
        mTextViewName = (TextView) itemView.findViewById(R.id.cardview_archivelist_name);
        mTextViewEmail = (TextView) itemView.findViewById(R.id.cardview_archivelist_email);
        mCheckBox = (CheckBox) itemView.findViewById(R.id.cardview_archivelist_checkbox);
        mCardView.setOnClickListener(this);
        mCheckBox.setOnCheckedChangeListener(this);
        mEventBus.register(this);
    }

    /**
     * Convenience method for filling viewholder with data.
     *
     * @param person given {@link Person} object with information to display.
     * @throws IllegalArgumentException when the category unequal to: {0,1,2}.
     */
    public void assignData(Person person) throws IllegalArgumentException {
        mPerson = person;
        int dimen = R.dimen.cardview_mainlist_dimen;
        if (person.getPictureUrl() == null) {
            Picasso.with(mContext)
                    .load(R.drawable.ic_account_circle_black_24dp)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new RoundedTransformation(0, Math.round(mContext.getResources()
                            .getDimension(dimen))))
                    .resizeDimen(dimen, dimen)
                    .centerCrop()
                    .into(mImageView);
        } else {
            Picasso.with(mContext)
                    .load(new File(person.getPictureUrl()))
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new RoundedTransformation(0, Math.round(mContext.getResources().
                            getDimension(dimen))))
                    .resizeDimen(dimen, dimen)
                    .centerCrop()
                    .into(mImageView);
        }
        mTextViewName.setText(person.getName());
        mTextViewEmail.setText(person.getEmail());
        setCardBackground();
    }

    @Subscribe
    private void listen(DeleteOrUnarchiveEvent event) {
        if (event.isStart()) {
            mCheckBox.setAlpha(0.0f);
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.animate()
                    .alpha(1.0f)
                    .setDuration(400)
                    .setInterpolator(new AnticipateOvershootInterpolator());

        } else {
            mCheckBox.animate()
                    .alpha(0.0f)
                    .setDuration(400)
                    .setInterpolator(new AnticipateOvershootInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mCheckBox.setVisibility(View.INVISIBLE);
                            mCheckBox.setChecked(false);
                        }
                    });
        }
    }

    private void setCardBackground() {
        int bgColor;
        switch (mPerson.getCategory()) {
            case Person.SURE:
                bgColor = ContextCompat.getColor(mContext, R.color.colorSure);
                break;
            case Person.ALMOST_SURE:
                bgColor = ContextCompat.getColor(mContext, R.color.colorAlmostSure);
                break;
            case Person.UNSURE:
                bgColor = ContextCompat.getColor(mContext, R.color.colorUnsure);
                break;
            default:
                throw new IllegalArgumentException("Unknown category number: " + mPerson.getCategory());
        }
        mCardView.setCardBackgroundColor(bgColor);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            mEventBus.post(new ArchiveViewHolderCheckedEvent(mPerson, true));
        } else {
            mEventBus.post(new ArchiveViewHolderCheckedEvent(mPerson, false));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardview_archivelist_cardview:
                //TODO
                mEventBus.post(new ArchiveViewHolderClickedEvent(mPerson));
                //showDetailDialog();
                break;
        }
    }

    private void showDetailDialog() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(mContext.getString(R.string.detail_archive_person_dialog_person_id),
                mPerson);
        DetailArchivePersonDialog dialog = DetailArchivePersonDialog.createInstance(bundle);
        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(),
                DetailArchivePersonDialog.class.getSimpleName());
    }
}
