package com.paydayme.spatialguide.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public PointAdapter(Context context, List<Point> list) {
        inflater = LayoutInflater.from(context);
        this.pointList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.point_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Point point = pointList.get(position);
        holder.pointName.setText(point.getPointName());
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView pointName;

        public MyViewHolder(View itemView) {
            super(itemView);
            pointName = (TextView) itemView.findViewById(R.id.pointName);

        }


    }
}
