package de.davidartmann.android.rosa2.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Log;
import android.view.View;

import com.google.common.eventbus.EventBus;

import de.davidartmann.android.rosa2.R;
import de.davidartmann.android.rosa2.adapter.MainListAdapter;
import de.davidartmann.android.rosa2.adapter.viewholder.MainListViewholder;

/**
 * Own implementation instead of using an
 * {@link android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback}.
 * Created by david on 05.10.16.
 */
public class MyItemTouchHelper extends Callback {

    private static final String TAG = MyItemTouchHelper.class.getSimpleName();
    private static final float ALPHA_FULL = 1.0f;

    private EventBus mEventBus;
    private Context mContext;
    private Paint mPaint;
    private MainListAdapter mMainListAdapter;
    private boolean mSwipeEnabled;

    public MyItemTouchHelper(Context context, EventBus eventBus) {//, MainListAdapter adapter) {
        mEventBus = eventBus;
        mContext = context;
        mPaint = new Paint();
        //mMainListAdapter = adapter;
        mSwipeEnabled = true;
    }

    /**
     * Describing which directions are allowed to swipe and drag.
     *
     * @param recyclerView our RecyclerView instance.
     * @param viewHolder   the actual ViewHolder which is swiped or dragged.
     * @return flags specifying which movements are allowed on this ViewHolder.
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START;// | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    /**
     * Listener method for when an item gets moved. Here we inform the adapter about the movements.
     *
     * @param recyclerView actual RecyclerView.
     * @param viewHolder   the dragged ViewHolder.
     * @param target       the ViewHolder over which the actual moved ViewHolder is being dragged.
     * @return true.
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        MainListAdapter adapter = ((MainListAdapter) recyclerView.getAdapter());
        if (adapter != null) {
            adapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }
        return false;
    }

    /**
     * Listener method for when an item gets swiped.
     *
     * @param viewHolder the swiped ViewHolder.
     * @param direction  the direction of swiping, either {@link ItemTouchHelper#START} or
     *                   {@link ItemTouchHelper#END}.
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mMainListAdapter.swipe(viewHolder, direction);
    }

    /**
     * If you would like to customize how your View's respond to user interactions,
     * this is a good place to override.
     *
     * @param c                 the canvas which RecyclerView is drawing its children.
     * @param recyclerView      the RecyclerView to which ItemTouchHelper is attached to.
     * @param viewHolder        the ViewHolder which is being interacted by the User or it was interacted
     *                          and simply animating to its original position.
     * @param dX                the amount of horizontal displacement caused by user's action.
     * @param dY                the amount of vertical displacement caused by user's action.
     * @param actionState       the type of interaction on the View. Is either
     *                          {@link ItemTouchHelper#ACTION_STATE_DRAG} or
     *                          {@link ItemTouchHelper#ACTION_STATE_SWIPE}.
     * @param isCurrentlyActive true if this view is currently being controlled by the user or
     *                          false it is simply animating back to its original state.
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
            View itemView = viewHolder.itemView;
            Drawable drawable;
            if (dX > 0) {// Display the background for deletion
                mPaint.setColor(ContextCompat.getColor(mContext, R.color.colorSwipeDelete));
                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), mPaint);
                // Set the image icon for right swipe => deletion
                drawable = VectorDrawableCompat.create(mContext.getResources(),
                        R.drawable.ic_delete_white_24dp, null);
                if (drawable != null) {
                    drawable.setBounds(itemView.getLeft() + 30,
                            itemView.getTop() + 55,
                            itemView.getRight() - (int) Math.round(itemView.getRight() * 0.85),
                            itemView.getBottom() - 55);
                    drawable.draw(c);
                }
            } else {// Display the background for archiving
                mPaint.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), mPaint);
                //Set the image for left swipe => archiving
                drawable = VectorDrawableCompat.create(mContext.getResources(),
                        R.drawable.ic_archive_white_24dp, null);
                if (drawable != null) {
                    drawable.setBounds(itemView.getLeft() + (int) Math.round(itemView.getRight() * 0.85),
                            itemView.getTop() + 55,
                            itemView.getRight() - 30,
                            itemView.getBottom() - 55);
                    drawable.draw(c);
                }
            }
        } else {// we drag so propagate handling
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    /**
     * Called when the ViewHolder swiped or dragged by the ItemTouchHelper is changed.
     * If you override this method, you should call super.
     *
     * @param viewHolder  the new ViewHolder that is being swiped or dragged.
     *                    Might be null if it is cleared.
     * @param actionState one of ACTION_STATE_IDLE, ACTION_STATE_SWIPE or ACTION_STATE_DRAG.
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // We only want the active item to change
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof MainListViewholder) {
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    ((CardView) viewHolder.itemView).setCardElevation(10);
                    //mContext.getResources().getDimension(R.dimen.common_elevation));
                }
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * Called by the ItemTouchHelper when the user interaction with an element is over
     * and it also completed its animation.
     *
     * @param recyclerView corresponding RecyclerView.
     * @param viewHolder   instance which was the target of action.
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setElevation(3);
        viewHolder.itemView.setAlpha(ALPHA_FULL);
        if (viewHolder instanceof MainListViewholder) {
            // Tell the view holder it's time to restore the idle state
            ((MainListViewholder) viewHolder).onClear();
        }
    }

    /**
     * We allow dragging with a longClick.
     *
     * @return true.
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    /**
     * We allow swiping.
     *
     * @return true.
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return isSwipeEnabled();
    }

    private boolean isSwipeEnabled() {
        return mSwipeEnabled;
    }

    public void setSwipeEnabled(boolean mSwipeEnabled) {
        this.mSwipeEnabled = mSwipeEnabled;
    }

    public void setMainListAdapter(MainListAdapter mMainListAdapter) {
        this.mMainListAdapter = mMainListAdapter;
    }
}
