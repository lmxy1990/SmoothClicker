package pylapp.smoothclicker.android.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Script;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScriptListAdapter extends ArrayAdapter<Script> {

    private Context mContext;
    private List<Script> mScripts;
    private int[] mActionCounts;

    public ScriptListAdapter(Context context, List<Script> scripts, int[] actionCounts) {
        super(context, 0, scripts);
        mContext = context;
        mScripts = scripts;
        mActionCounts = actionCounts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Script script = mScripts.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_script, parent, false);
        }

        TextView tvScriptName = convertView.findViewById(R.id.tvScriptName);
        tvScriptName.setText(script.name);

        TextView tvActionCount = convertView.findViewById(R.id.tvActionCount);
        int count = position < mActionCounts.length ? mActionCounts[position] : 0;
        tvActionCount.setText(mContext.getString(R.string.action_count, count));

        TextView tvCreatedAt = convertView.findViewById(R.id.tvCreatedAt);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvCreatedAt.setText(sdf.format(new Date(script.createdAt)));

        return convertView;
    }
}