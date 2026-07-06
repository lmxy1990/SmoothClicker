package pylapp.smoothclicker.android.views;

import android.content.Context;
import android.graphics.Rect;
import androidx.appcompat.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.tools.Logger;

public class StandaloneModeDialog extends AppCompatDialog {

    private OnPositiveButtonListener mPositiveButtonListener;

    private static final float MIN_WIDTH_RATIO = 0.9f;

    private static final String LOG_TAG = StandaloneActivity.class.getSimpleName();

    public StandaloneModeDialog(Context c) {
        super(c);
        initView();
    }

    private void initView() {
        Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View builderView = inflater.inflate(R.layout.dialog_standalonemode, null);
        builderView.setMinimumWidth((int) (displayRectangle.width() * MIN_WIDTH_RATIO));
        setContentView(builderView);

        setTitle(R.string.widget_standalone_dialog_title);

        RadioGroup radioGroup = findViewById(R.id.rgStandaloneModeSelector);
        radioGroup.check(R.id.rbAllPoints);

        Button btCancel = findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button btCreate = findViewById(R.id.btCreate);
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StandaloneActivity.StandaloneMode mode = getSelection();
                if (mPositiveButtonListener != null) {
                    String[] titles = getContext().getResources().getStringArray(R.array.standalone_mode_titles);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    int selectedIndex = selectedId == R.id.rbAllPoints ? 0 : 1;
                    Toast.makeText(getContext(), titles[selectedIndex], Toast.LENGTH_LONG).show();
                    mPositiveButtonListener.onPositiveButtonClick(mode);
                    Logger.d(LOG_TAG, "Stand alone mode in use : " + mode.toString());
                }
                dismiss();
            }
        });
    }

    private StandaloneActivity.StandaloneMode getSelection() {
        RadioGroup radioGroup = findViewById(R.id.rgStandaloneModeSelector);
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.rbAllPointsScreen) {
            return StandaloneActivity.StandaloneMode.ALL_POINTS_WITH_CONFIG_ACCORDING_SCREEN;
        }
        return StandaloneActivity.StandaloneMode.ALL_POINTS_WITH_CONFIG;
    }

    public void setPositiveButtonListener(OnPositiveButtonListener l) {
        mPositiveButtonListener = l;
    }

    public interface OnPositiveButtonListener {
        void onPositiveButtonClick(StandaloneActivity.StandaloneMode userSelection);
    }
}