package pylapp.smoothclicker.android.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Action;
import pylapp.smoothclicker.android.database.DatabaseManager;

public class ActionEditActivity extends AppCompatActivity {

    private DatabaseManager mDbManager;
    private long mActionId;
    private long mScriptId;
    private Action mAction;

    private EditText mEtName;
    private EditText mEtX;
    private EditText mEtY;
    private EditText mEtEndX;
    private EditText mEtEndY;
    private EditText mEtDuration;
    private EditText mEtWaitTime;
    private EditText mEtRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mActionId = intent.getLongExtra("action_id", -1);
        mScriptId = intent.getLongExtra("script_id", -1);

        mDbManager = new DatabaseManager(this);
        mDbManager.open();

        if (mActionId > 0) {
            mAction = mDbManager.getAction(mActionId);
        }

        mEtName = findViewById(R.id.etName);
        mEtX = findViewById(R.id.etX);
        mEtY = findViewById(R.id.etY);
        mEtEndX = findViewById(R.id.etEndX);
        mEtEndY = findViewById(R.id.etEndY);
        mEtDuration = findViewById(R.id.etDuration);
        mEtWaitTime = findViewById(R.id.etWaitTime);
        mEtRepeat = findViewById(R.id.etRepeat);

        LinearLayout layoutEndCoords = findViewById(R.id.layoutEndCoords);
        LinearLayout layoutWaitTime = findViewById(R.id.layoutWaitTime);

        if (mAction != null) {
            mEtName.setText(mAction.name);
            mEtX.setText(String.valueOf(mAction.x));
            mEtY.setText(String.valueOf(mAction.y));
            mEtDuration.setText(String.valueOf(mAction.duration));
            mEtWaitTime.setText(String.valueOf(mAction.waitTime));
            mEtRepeat.setText(String.valueOf(mAction.repeat));

            if (mAction.type == Action.TYPE_SWIPE) {
                mEtEndX.setText(String.valueOf(mAction.endX));
                mEtEndY.setText(String.valueOf(mAction.endY));
                layoutEndCoords.setVisibility(View.VISIBLE);
                layoutWaitTime.setVisibility(View.VISIBLE);
            } else if (mAction.type == Action.TYPE_CLICK) {
                layoutEndCoords.setVisibility(View.GONE);
                layoutWaitTime.setVisibility(View.GONE);
            }
        } else {
            mEtDuration.setText("100");
            mEtWaitTime.setText("0");
            mEtRepeat.setText("1");
            layoutEndCoords.setVisibility(View.GONE);
            layoutWaitTime.setVisibility(View.GONE);
        }

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAction();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbManager.close();
    }

    private void saveAction() {
        String name = mEtName.getText().toString().trim();
        if (name.isEmpty()) {
            name = getString(R.string.action_click);
        }

        int x = parseInt(mEtX.getText().toString(), 0);
        int y = parseInt(mEtY.getText().toString(), 0);
        int endX = parseInt(mEtEndX.getText().toString(), -1);
        int endY = parseInt(mEtEndY.getText().toString(), -1);
        int duration = parseInt(mEtDuration.getText().toString(), 100);
        int waitTime = parseInt(mEtWaitTime.getText().toString(), 0);
        int repeat = parseInt(mEtRepeat.getText().toString(), 1);

        if (mAction == null) {
            mAction = new Action(mScriptId, name, Action.TYPE_CLICK);
            mAction.id = mDbManager.insertAction(mAction);
        }

        mAction.name = name;
        mAction.x = x;
        mAction.y = y;
        mAction.endX = endX;
        mAction.endY = endY;
        mAction.duration = duration;
        mAction.waitTime = waitTime;
        mAction.repeat = repeat;

        mDbManager.updateAction(mAction);

        setResult(RESULT_OK);
        finish();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}