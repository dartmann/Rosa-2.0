package de.davidartmann.android.rosa2.adapter.viewholder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.eventbus.EventBus;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.activity.MainActivity;
import de.davidartmann.android.rosa2.database.model.Person;
import de.davidartmann.android.rosa2.util.RoundedTransformation;
import de.davidartmann.android.rosa2.util.eventbus.event.MainListItemClickEvent;

/**
 * Viewholder for the main list.
 * Created by david on 23.08.16.
 */
public class MainListViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = MainListViewholder.class.getSimpleName();

    private CardView mCardView;
    private ImageView mImageView;
    private TextView mTextViewName;
    private TextView mTextViewAddress;
    private TextView mTextViewPrice;
    private Context mContext;
    private EventBus mEventBus;
    private Person mPerson;
    private View mView;

    public MainListViewholder(View itemView, Context context, EventBus eventBus) {
        super(itemView);
        mView = itemView;
        mContext = context;
        mEventBus = eventBus;
        mCardView = (CardView) itemView.findViewById(R.id.cardview_mainlist_cardview);
        mImageView = (ImageView) itemView.findViewById(R.id.cardview_mainlist_image);
        mTextViewName = (TextView) itemView.findViewById(R.id.cardview_mainlist_name);
        mTextViewAddress = (TextView) itemView.findViewById(R.id.cardview_mainlist_address);
        mTextViewPrice = (TextView) itemView.findViewById(R.id.cardview_mainlist_price);
        itemView.setOnClickListener(this);
    }

    /**
     * Convenience method for filling viewholder with data.
     *
     * @param person given {@link Person} object with information to display.
     * @throws IllegalArgumentException when the category unequal to: {0,1,2}.
     */
    public void assignData(Person person) throws IllegalArgumentException {
        mPerson = person;
        int dimen = R.dimen.cardview_mainlist_dimen;//R.dimen.fragment_detail_image_dimen;
        if (mPerson.getPictureUrl() != null) {
            Picasso.with(mContext)
                    .load(new File(person.getPictureUrl()))
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new RoundedTransformation(0, Math.round(mContext.getResources().getDimension(dimen))))
                    .resizeDimen(dimen, dimen)
                    .centerCrop()
                    .into(mImageView);
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.ic_account_circle_black_24dp)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new RoundedTransformation(0, Math.round(mContext.getResources().getDimension(dimen))))
                    .resizeDimen(dimen, dimen)
                    .centerCrop()
                    .into(mImageView);
        }
        mTextViewName.setText(person.getName());
        mTextViewAddress.setText(person.getAddress());
        if (person.getPrice() == null) {
            mTextViewPrice.setText(R.string.zeroEuros);
        } else {
            mTextViewPrice.setText(String.format("%s €", person.getPrice()));
        }
        setCardBackground();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardview_mainlist_cardview:
                mEventBus.post(new MainListItemClickEvent(mPerson, view));
                break;
            default:
                Log.w(TAG, "Onclick unregistered view id: " + view.getId());
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

    /**
     * Called from the {@link de.davidartmann.android.rosa2.util.MyItemTouchHelper#clearView(RecyclerView, RecyclerView.ViewHolder)}.
     */
    public void onClear() {
        setCardBackground();
    }

}
