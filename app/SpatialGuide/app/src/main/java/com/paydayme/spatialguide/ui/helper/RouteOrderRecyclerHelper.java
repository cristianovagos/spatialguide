package com.paydayme.spatialguide.ui.helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RouteOrderRecyclerHelper extends ItemTouchHelper.Callback {

    private RouteOrderRecyclerCallback callback;

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.DOWN | ItemTouchHelper.UP, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        callback.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    public interface RouteOrderRecyclerCallback {
        void onItemMove(int initialPosition, int finalPosition);
    }
}
