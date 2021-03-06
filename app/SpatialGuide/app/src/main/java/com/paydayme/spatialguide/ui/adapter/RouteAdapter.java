package com.paydayme.spatialguide.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private static final String TAG = "RouteAdapter";

    public interface OnItemClickListener {
        void onItemClick(Route item, View view);
    }

    private Context context;
    private final List<Route> items;
    private final OnItemClickListener listener;

    public RouteAdapter(Context context, List<Route> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_row, parent, false);
        return new ViewHolder(v, context);
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
        @BindView(R.id.routeImage) LabelImageView image;
        @BindView(R.id.routeImageProgress) ProgressBar imageProgress;

        private Context context;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        void bind(final Route item, final OnItemClickListener listener) {
            if(isOnStorage(item.getRouteID(), context)) {
                image.setLabelText(context.getResources().getString(R.string.on_device));
            }

            name.setText(item.getRouteName());
            description.setText(item.getRouteDescription());

            if(!item.getRouteImage().isEmpty()) {
                Picasso.get()
                        .load(item.getRouteImage())
                        .error(R.drawable.not_available)
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                imageProgress.setVisibility(View.GONE);
                            }
                        });
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item, itemView);
                }
            });
        }
    }

    private static boolean isOnStorage(int routeID, Context context) {
        try {
            Route route = (Route) InternalStorage.readObject(context, Constant.ROUTE_STORAGE_SEPARATOR + routeID);
            if(route.getRoutePoints().size() < 1)
                return false;
            for(Point p : route.getRoutePoints()) {
                try {
                    if(InternalStorage.getFile(context, Constant.POINT_STORAGE_SEPARATOR + p.getPointID() + ".wav") == null)
                        return false;
                } catch (Exception e) {
                    Log.d(TAG, "isOnStorage IOException: " + e.getMessage());
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "isOnStorage IOException: " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "isOnStorage ClassNotFoundException: " + e.getMessage());
            return false;
        }
    }
}