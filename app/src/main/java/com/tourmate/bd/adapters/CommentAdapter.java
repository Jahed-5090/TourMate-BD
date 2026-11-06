package com.tourmate.bd.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.tourmate.bd.R;
import com.tourmate.bd.models.SpotComment;
import com.tourmate.bd.network.FirebaseAuthManager;

import java.util.List;

/**
 * RecyclerView Adapter for displaying real-time spot comments and feedback.
 * Supports Master Developer Admin deletion privileges for jmjahed47@gmail.com.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    public interface OnCommentDeleteListener {
        void onDeleteComment(SpotComment comment);
    }

    public static final String MASTER_ADMIN_EMAIL = "jmjahed47@gmail.com";

    private final Context context;
    private final List<SpotComment> comments;
    private final OnCommentDeleteListener deleteListener;

    public CommentAdapter(Context context, List<SpotComment> comments, OnCommentDeleteListener deleteListener) {
        this.context = context;
        this.comments = comments;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpotComment comment = comments.get(position);

        String userName = comment.getUserName() != null && !comment.getUserName().trim().isEmpty()
                ? comment.getUserName() : "Traveler";
        holder.tvUser.setText(userName);
        holder.tvText.setText(comment.getCommentText());

        // Relative timestamp (e.g. "5 mins ago")
        if (comment.getTimestamp() > 0) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    comment.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("Recently");
        }

        // Developer Admin Badge
        if (MASTER_ADMIN_EMAIL.equalsIgnoreCase(comment.getUserEmail())) {
            holder.tvAdminBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvAdminBadge.setVisibility(View.GONE);
        }

        // Master Admin & Author Delete Privileges
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        boolean canDelete = false;

        if (currentUser != null) {
            boolean isMasterAdmin = MASTER_ADMIN_EMAIL.equalsIgnoreCase(currentUser.getEmail());
            boolean isAuthor = currentUser.getUid().equals(comment.getUserUid());
            canDelete = isMasterAdmin || isAuthor;
        }

        if (canDelete) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteComment(comment);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvTime, tvText, tvAdminBadge;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_comment_user);
            tvTime = itemView.findViewById(R.id.tv_comment_time);
            tvText = itemView.findViewById(R.id.tv_comment_text);
            tvAdminBadge = itemView.findViewById(R.id.tv_admin_badge);
            btnDelete = itemView.findViewById(R.id.btn_delete_comment);
        }
    }
}
