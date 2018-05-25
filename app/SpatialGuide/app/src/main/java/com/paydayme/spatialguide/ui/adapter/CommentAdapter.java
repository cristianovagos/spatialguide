package com.paydayme.spatialguide.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Comment;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private static final String TAG = "CommentAdapter";

    private Context context;
    private final List<Comment> items;
    private SGApiClient client;
    private String authHeader;

    public CommentAdapter(Context context, List<Comment> items, SGApiClient client, String authHeader) {
        this.context = context;
        this.items = items;
        this.client = client;
        this.authHeader = authHeader;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
        return new ViewHolder(v, context, client, authHeader);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.commentUserImage) CircleImageView userImage;
        @BindView(R.id.commentUserName) TextView userName;
        @BindView(R.id.commentText) TextView commentText;

        private Context context;
        private SGApiClient client;
        private String authHeader;

        ViewHolder(View itemView, Context context, SGApiClient client, String authHeader) {
            super(itemView);
            this.context = context;
            this.client = client;
            this.authHeader = authHeader;
            ButterKnife.bind(this, itemView);
        }

        void bind(final Comment item) {
            Call<User> call = client.getUserInfoByID(authHeader, item.getUserID());
            call.enqueue(new retrofit2.Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful()) {
                        if(!response.body().getUserImage().isEmpty()) {
                            Picasso.get()
                                    .load(response.body().getUserImage())
                                    .placeholder(R.drawable.progress_animation)
                                    .error(R.drawable.not_available)
                                    .into(userImage);
                        }

                        userName.setText(response.body().getUsername() + " comentou:");
                        commentText.setText(item.getComment());
                    } else {
                        Log.e(TAG, "bind onResponse: something failed");
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "bind onFailure: something failed: " + t.getMessage());
                }
            });
        }
    }
}