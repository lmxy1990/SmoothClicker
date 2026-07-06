package pylapp.smoothclicker.android.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Action;

import java.util.List;

public class ActionAdapter extends ArrayAdapter<Action> {

    private Context mContext;
    private List<Action> mActions;

    public ActionAdapter(Context context, List<Action> actions) {
        super(context, 0, actions);
        mContext = context;
        mActions = actions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Action action = mActions.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_action, parent, false);
        }

        ImageView ivActionIcon = convertView.findViewById(R.id.ivActionIcon);
        switch (action.type) {
            case Action.TYPE_CLICK:
                ivActionIcon.setImageResource(R.drawable.ic_click);
                break;
            case Action.TYPE_SWIPE:
                ivActionIcon.setImageResource(R.drawable.ic_swipe);
                break;
            case Action.TYPE_MULTI:
                ivActionIcon.setImageResource(R.drawable.ic_multi);
                break;
        }

        TextView tvActionName = convertView.findViewById(R.id.tvActionName);
        tvActionName.setText(action.name);

        TextView tvActionDesc = convertView.findViewById(R.id.tvActionDesc);
        StringBuilder desc = new StringBuilder();
        switch (action.type) {
            case Action.TYPE_CLICK:
                desc.append(mContext.getString(R.string.click_desc, action.x, action.y));
                break;
            case Action.TYPE_SWIPE:
                desc.append(mContext.getString(R.string.swipe_desc, action.x, action.y, action.endX, action.endY));
                break;
            case Action.TYPE_MULTI:
                desc.append(mContext.getString(R.string.multi_desc));
                break;
        }
        tvActionDesc.setText(desc.toString());

        return convertView;
    }
}