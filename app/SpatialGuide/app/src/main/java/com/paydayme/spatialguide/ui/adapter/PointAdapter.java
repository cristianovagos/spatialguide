package com.paydayme.spatialguide.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.model.Point;

import java.util.Collections;
import java.util.List;

/**
 * Created by cvagos on 24-03-2018.
 */

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private List<Point> pointList = Collections.emptyList();
    private boolean draggable;

    public PointAdapter(Context context, List<Point> list, boolean draggable) {
        inflater = LayoutInflater.from(context);
        this.pointList = list;
        this.draggable = draggable;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.point_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Point point = pointList.get(position);
        holder.pointName.setText(point.getPointName());
        if(draggable)
            holder.pointDrag.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }

    public void onItemMove(int initialPosition, int finalPosition) {
        if(initialPosition < pointList.size() && finalPosition < pointList.size()) {
            if(initialPosition < finalPosition) {
                for(int i = initialPosition; i < finalPosition; i++) {
                    Collections.swap(pointList, i, i+1);
                }
            } else {
                for(int i = initialPosition; i > finalPosition; i--) {
                    Collections.swap(pointList, i, i-1);
                }
            }
        }

        notifyItemMoved(initialPosition, finalPosition);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView pointName;
        ImageView pointDrag;

        public MyViewHolder(View itemView) {
            super(itemView);
            pointName = (TextView) itemView.findViewById(R.id.pointName);
            pointDrag = (ImageView) itemView.findViewById(R.id.pointDrag);
        }

    }
}
