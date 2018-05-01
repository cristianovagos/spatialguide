package com.paydayme.spatialguide.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryFavoritesAdapter extends RecyclerView.Adapter<HistoryFavoritesAdapter.ViewHolder> {

    private static final String TAG = "HistoryAdapter";

    private Context context;
    private final List<Object> items;

    public HistoryFavoritesAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_history_row, parent, false);
        return new ViewHolder(v, context);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.favoriteName) TextView name;
        @BindView(R.id.favoriteDescription) TextView description;
        @BindView(R.id.favoriteImage) LabelImageView image;

        private Context context;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        void bind(final Object item) {
            if(item instanceof Route) {
                name.setText(((Route) item).getRouteName());
                description.setText(((Route) item).getRouteDescription());
                if(!((Route) item).getRouteImage().isEmpty()) {
                    Picasso.get()
                            .load(((Route) item).getRouteImage())
                            .placeholder(R.drawable.progress_animation)
                            .error(R.drawable.not_available)
                            .into(image);
                }
                image.setLabelText("ROUTE");
            } else if(item instanceof Point) {
                name.setText(((Point) item).getPointName());
                description.setText(((Point) item).getPointDescription());
                if(!((Point) item).getPointImage().isEmpty()) {
                    Picasso.get()
                            .load(((Point) item).getPointImage())
                            .placeholder(R.drawable.progress_animation)
                            .error(R.drawable.not_available)
                            .into(image);
                }
                image.setLabelText("POINT");
            }
        }
    }
}