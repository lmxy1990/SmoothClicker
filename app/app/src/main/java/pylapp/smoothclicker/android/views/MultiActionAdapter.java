package pylapp.smoothclicker.android.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.MultiAction;

import java.util.List;

public class MultiActionAdapter extends ArrayAdapter<MultiAction> {

    private Context mContext;
    private List<MultiAction> mActions;
    private OnDeleteListener mOnDeleteListener;

    public interface OnDeleteListener {
        void onDelete(MultiAction action);
    }

    public MultiActionAdapter(Context context, List<MultiAction> actions, OnDeleteListener listener) {
        super(context, 0, actions);
        mContext = context;
        mActions = actions;
        mOnDeleteListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MultiAction action = mActions.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_multi_action, parent, false);
        }

        LinearLayout llIconContainer = convertView.findViewById(R.id.llIconContainer);
        ImageView ivIcon = convertView.findViewById(R.id.ivIcon);
        switch (action.type) {
            case MultiAction.TYPE_CLICK:
                ivIcon.setImageResource(R.drawable.ic_click);
                llIconContainer.setBackgroundResource(R.drawable.circle_click_background);
                break;
            case MultiAction.TYPE_SWIPE:
                ivIcon.setImageResource(R.drawable.ic_swipe);
                llIconContainer.setBackgroundResource(R.drawable.circle_swipe_background);
                break;
        }

        TextView tvType = convertView.findViewById(R.id.tvType);
        tvType.setText(action.type == MultiAction.TYPE_CLICK ? mContext.getString(R.string.action_click) : mContext.getString(R.string.action_swipe));

        TextView tvDesc = convertView.findViewById(R.id.tvDesc);
        StringBuilder desc = new StringBuilder();
        if (action.type == MultiAction.TYPE_CLICK) {
            desc.append(mContext.getString(R.string.click_desc, action.x, action.y));
        } else {
            desc.append(mContext.getString(R.string.swipe_desc, action.x, action.y, action.endX, action.endY));
        }
        if (action.delay > 0) {
            desc.append(" ").append(mContext.getString(R.string.delay_desc, action.delay));
        }
        tvDesc.setText(desc.toString());

        ImageView ivDelete = convertView.findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDeleteListener != null) {
                    mOnDeleteListener.onDelete(action);
                }
            }
        });

        return convertView;
    }
}