package pylapp.smoothclicker.android.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Action;

public class ActionRecyclerAdapter extends RecyclerView.Adapter<ActionRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Action> mActions;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Action action);
        void onDeleteClick(Action action);
        void onItemMove(int fromPosition, int toPosition);
    }

    public ActionRecyclerAdapter(Context context, List<Action> actions, OnItemClickListener listener) {
        mContext = context;
        mActions = actions;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_action, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Action action = mActions.get(position);
        holder.tvActionName.setText(action.name);

        int iconRes = R.drawable.ic_click;
        int circleBgRes = R.drawable.circle_click_background;
        String desc = "";
        if (action.type == Action.TYPE_CLICK) {
            iconRes = R.drawable.ic_click;
            circleBgRes = R.drawable.circle_click_background;
            desc = mContext.getString(R.string.click_desc, action.x, action.y);
        } else if (action.type == Action.TYPE_SWIPE) {
            iconRes = R.drawable.ic_swipe;
            circleBgRes = R.drawable.circle_swipe_background;
            desc = mContext.getString(R.string.swipe_desc, action.x, action.y, action.endX, action.endY);
        } else if (action.type == Action.TYPE_MULTI) {
            iconRes = R.drawable.ic_multi;
            circleBgRes = R.drawable.circle_multi_background;
            desc = mContext.getString(R.string.multi_desc);
        }
        holder.ivActionIcon.setImageResource(iconRes);
        holder.llIconContainer.setBackgroundResource(circleBgRes);
        holder.tvActionDesc.setText(desc);

        holder.llActionItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(action);
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteClick(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mActions.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mActions, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mActions, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        mListener.onItemMove(fromPosition, toPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llActionItem;
        LinearLayout llIconContainer;
        ImageView ivActionIcon;
        TextView tvActionName;
        TextView tvActionDesc;
        ImageView ivDelete;
        ImageView ivDrag;

        public ViewHolder(View itemView) {
            super(itemView);
            llActionItem = itemView.findViewById(R.id.llActionItem);
            llIconContainer = itemView.findViewById(R.id.llIconContainer);
            ivActionIcon = itemView.findViewById(R.id.ivActionIcon);
            tvActionName = itemView.findViewById(R.id.tvActionName);
            tvActionDesc = itemView.findViewById(R.id.tvActionDesc);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivDrag = itemView.findViewById(R.id.ivDrag);
        }
    }
}