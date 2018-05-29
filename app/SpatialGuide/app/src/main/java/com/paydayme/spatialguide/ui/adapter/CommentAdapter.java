package com.paydayme.spatialguide.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.model.Comment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private static final String TAG = "CommentAdapter";

    private final List<Comment> items;

    public CommentAdapter(List<Comment> items) {
        this.items = items;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
        return new ViewHolder(v);
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

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Comment item) {
            if(!item.getUserimage().isEmpty()) {
                Picasso.get()
                        .load(item.getUserimage())
                        .placeholder(R.drawable.progress_animation)
                        .error(R.drawable.not_available)
                        .into(userImage, new Callback() {
                            @Override
                            public void onSuccess() {}

                            @Override
                            public void onError(Exception e) {
                                userImage.setImageResource(R.mipmap.ic_launcher_round);
                            }
                        });
            } else {
                userImage.setImageResource(R.mipmap.ic_launcher_round);
            }

            userName.setText(item.getUsername() + " comentou:");
            commentText.setText(item.getComment());
        }
    }
}