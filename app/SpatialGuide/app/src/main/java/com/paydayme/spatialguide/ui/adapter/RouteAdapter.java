package com.paydayme.spatialguide.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.model.Route;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Route item);
    }

    private final List<Route> items;
    private final OnItemClickListener listener;

    public RouteAdapter(List<Route> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_row, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.routeName) TextView name;
        @BindView(R.id.routeDescription) TextView description;
        @BindView(R.id.routeImage) ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Route item, final OnItemClickListener listener) {
            name.setText(item.getRouteName());
            description.setText(item.getRouteDescription());
            Picasso.get()
                    .load(item.getRouteImage())
                    .placeholder(R.drawable.not_available)
                    .into(image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}