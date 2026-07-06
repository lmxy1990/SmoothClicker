package pylapp.smoothclicker.android.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Action;
import pylapp.smoothclicker.android.database.DatabaseManager;
import pylapp.smoothclicker.android.database.MultiAction;

import java.util.List;

public class MultiActionEditActivity extends AppCompatActivity {

    private DatabaseManager mDbManager;
    private long mActionId;
    private long mScriptId;
    private Action mAction;
    private List<MultiAction> mMultiActions;
    private MultiActionAdapter mAdapter;

    private EditText mEtName;
    private ListView mLvMultiActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_action_edit);

        Intent intent = getIntent();
        mActionId = intent.getLongExtra("action_id", -1);
        mScriptId = intent.getLongExtra("script_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(intent.getStringExtra("action_name"));

        mDbManager = new DatabaseManager(this);
        mDbManager.open();

        if (mActionId > 0) {
            mAction = mDbManager.getAction(mActionId);
        }

        mEtName = findViewById(R.id.etName);
        mLvMultiActions = findViewById(R.id.lvMultiActions);

        if (mAction != null) {
            mEtName.setText(mAction.name);
        }

        loadMultiActions();

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMultiActionDialog();
            }
        });

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

    private void loadMultiActions() {
        if (mActionId > 0) {
            mMultiActions = mDbManager.getMultiActionsByParent(mActionId);
        }
        mAdapter = new MultiActionAdapter(this, mMultiActions, new MultiActionAdapter.OnDeleteListener() {
            @Override
            public void onDelete(MultiAction action) {
                mDbManager.deleteMultiAction(action.id);
                loadMultiActions();
                Toast.makeText(MultiActionEditActivity.this, R.string.action_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        mLvMultiActions.setAdapter(mAdapter);

        mLvMultiActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MultiAction action = mMultiActions.get(position);
                showEditMultiActionDialog(action);
            }
        });
    }

    private void showAddMultiActionDialog() {
        final String[] actionTypes = {getString(R.string.action_click), getString(R.string.action_swipe)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_sub_action)
                .setItems(actionTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int type = which;
                        MultiAction action = new MultiAction(mActionId, type);
                        action.orderNum = mMultiActions.size();
                        long id = mDbManager.insertMultiAction(action);
                        if (id > 0) {
                            action.id = id;
                            mMultiActions.add(action);
                            mAdapter.notifyDataSetChanged();
                            showEditMultiActionDialog(action);
                        }
                    }
                })
                .show();
    }

    private void showEditMultiActionDialog(final MultiAction action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(action.type == MultiAction.TYPE_CLICK ? R.string.action_click : R.string.action_swipe);

        View view = getLayoutInflater().inflate(R.layout.dialog_multi_action_edit, null);
        builder.setView(view);

        final EditText etX = view.findViewById(R.id.etX);
        final EditText etY = view.findViewById(R.id.etY);
        final EditText etEndX = view.findViewById(R.id.etEndX);
        final EditText etEndY = view.findViewById(R.id.etEndY);
        final EditText etDuration = view.findViewById(R.id.etDuration);
        final EditText etDelay = view.findViewById(R.id.etDelay);

        etX.setText(String.valueOf(action.x));
        etY.setText(String.valueOf(action.y));
        etDuration.setText(String.valueOf(action.duration));
        etDelay.setText(String.valueOf(action.delay));

        if (action.type == MultiAction.TYPE_SWIPE) {
            etEndX.setText(String.valueOf(action.endX));
            etEndY.setText(String.valueOf(action.endY));
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                action.x = parseInt(etX.getText().toString(), 0);
                action.y = parseInt(etY.getText().toString(), 0);
                action.endX = parseInt(etEndX.getText().toString(), -1);
                action.endY = parseInt(etEndY.getText().toString(), -1);
                action.duration = parseInt(etDuration.getText().toString(), 100);
                action.delay = parseInt(etDelay.getText().toString(), 0);
                mDbManager.updateMultiAction(action);
                loadMultiActions();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void saveAction() {
        String name = mEtName.getText().toString().trim();
        if (name.isEmpty()) {
            name = getString(R.string.action_multi);
        }

        if (mAction != null) {
            mAction.name = name;
            mDbManager.updateAction(mAction);
        }

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