package pylapp.smoothclicker.android.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Action;
import pylapp.smoothclicker.android.database.DatabaseManager;
import pylapp.smoothclicker.android.database.MultiAction;
import pylapp.smoothclicker.android.database.Script;

public class ActionListActivity extends AppCompatActivity {

    public static final int REQUEST_EDIT_ACTION = 100;

    private RecyclerView mRvActions;
    private List<Action> mActions;
    private ActionRecyclerAdapter mAdapter;
    private DatabaseManager mDbManager;
    private long mScriptId;
    private String mScriptName;
    private ProgressBar mProgressBar;
    private TextView mTvProgress;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SmoothClicker", "ActionListActivity onCreate called");
        setContentView(R.layout.activity_action_list_new);

        Intent intent = getIntent();
        mScriptId = intent.getLongExtra("script_id", -1);
        mScriptName = intent.getStringExtra("script_name");
        Log.d("SmoothClicker", "ActionListActivity: script_id=" + mScriptId + ", script_name=" + mScriptName);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mScriptName);

        mDbManager = new DatabaseManager(this);
        mDbManager.open();

        mProgressBar = findViewById(R.id.progressBar);
        mTvProgress = findViewById(R.id.tvProgress);

        mRvActions = findViewById(R.id.rvActions);
        mRvActions.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                Collections.swap(mActions, fromPos, toPos);
                mAdapter.notifyItemMoved(fromPos, toPos);
                updateActionOrders();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        });
        itemTouchHelper.attachToRecyclerView(mRvActions);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddActionDialog();
            }
        });

        Button btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playScript();
            }
        });

        Button btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportScript();
            }
        });

        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteScriptDialog();
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
    protected void onResume() {
        super.onResume();
        loadActions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbManager.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_ACTION && resultCode == RESULT_OK) {
            loadActions();
        }
    }

    private void loadActions() {
        mActions = mDbManager.getActionsByScript(mScriptId);
        mAdapter = new ActionRecyclerAdapter(this, mActions, new ActionRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Action action) {
                if (action.type == Action.TYPE_MULTI) {
                    Intent i = new Intent(ActionListActivity.this, MultiActionEditActivity.class);
                    i.putExtra("action_id", action.id);
                    i.putExtra("script_id", mScriptId);
                    i.putExtra("action_name", action.name);
                    startActivityForResult(i, REQUEST_EDIT_ACTION);
                } else {
                    Intent i = new Intent(ActionListActivity.this, ActionEditActivity.class);
                    i.putExtra("action_id", action.id);
                    i.putExtra("script_id", mScriptId);
                    startActivityForResult(i, REQUEST_EDIT_ACTION);
                }
            }

            @Override
            public void onDeleteClick(Action action) {
                mDbManager.deleteAction(action.id);
                loadActions();
                Toast.makeText(ActionListActivity.this, R.string.action_deleted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                updateActionOrders();
            }
        });
        mRvActions.setAdapter(mAdapter);
    }

    private void updateActionOrders() {
        for (int i = 0; i < mActions.size(); i++) {
            mActions.get(i).orderNum = i;
            mDbManager.updateActionOrder(mActions.get(i).id, i);
        }
    }

    private void showAddActionDialog() {
        final String[] actionTypes = {getString(R.string.action_click), getString(R.string.action_swipe), getString(R.string.action_multi)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_action)
                .setItems(actionTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int type = which;
                        String name = actionTypes[which];
                        Action action = new Action(mScriptId, name, type);
                        action.orderNum = mActions.size();
                        long id = mDbManager.insertAction(action);
                        if (id > 0) {
                            loadActions();
                            if (type == Action.TYPE_MULTI) {
                                Intent i = new Intent(ActionListActivity.this, MultiActionEditActivity.class);
                                i.putExtra("action_id", id);
                                i.putExtra("script_id", mScriptId);
                                i.putExtra("action_name", name);
                                startActivityForResult(i, REQUEST_EDIT_ACTION);
                            } else {
                                Intent i = new Intent(ActionListActivity.this, ActionEditActivity.class);
                                i.putExtra("action_id", id);
                                i.putExtra("script_id", mScriptId);
                                startActivityForResult(i, REQUEST_EDIT_ACTION);
                            }
                        }
                    }
                })
                .show();
    }

    private void playScript() {
        if (mActions.isEmpty()) {
            Toast.makeText(this, R.string.no_actions, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mTvProgress.setVisibility(View.VISIBLE);
        mProgressBar.setMax(mActions.size());
        mProgressBar.setProgress(0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

                    for (int i = 0; i < mActions.size(); i++) {
                        final int finalI = i;
                        final Action action = mActions.get(i);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(finalI);
                                mTvProgress.setText(String.format(getString(R.string.executing_action), finalI + 1, mActions.size(), action.name));
                            }
                        });

                        String cmd;
                        if (action.type == Action.TYPE_CLICK) {
                            cmd = "/system/bin/input tap " + action.x + " " + action.y + "\n";
                        } else if (action.type == Action.TYPE_SWIPE) {
                            cmd = "/system/bin/input swipe " + action.x + " " + action.y + " " + action.endX + " " + action.endY + " " + action.duration + "\n";
                        } else {
                            List<MultiAction> multiActions = mDbManager.getMultiActionsByParent(action.id);
                            for (MultiAction ma : multiActions) {
                                if (ma.delay > 0) {
                                    Thread.sleep(ma.delay);
                                }
                                if (ma.type == MultiAction.TYPE_CLICK) {
                                    cmd = "/system/bin/input tap " + ma.x + " " + ma.y + "\n";
                                } else {
                                    cmd = "/system/bin/input swipe " + ma.x + " " + ma.y + " " + ma.endX + " " + ma.endY + " " + ma.duration + "\n";
                                }
                                outputStream.writeBytes(cmd);
                                outputStream.flush();
                            }
                            Thread.sleep(action.waitTime);
                            continue;
                        }
                        outputStream.writeBytes(cmd);
                        outputStream.flush();
                        Thread.sleep(action.waitTime);
                    }
                    outputStream.close();
                    process.waitFor();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(mActions.size());
                            mTvProgress.setText(R.string.script_finished);
                            Toast.makeText(ActionListActivity.this, R.string.script_finished, Toast.LENGTH_SHORT).show();
                        }
                    });

                    Thread.sleep(2000);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            mTvProgress.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            mTvProgress.setVisibility(View.GONE);
                            Toast.makeText(ActionListActivity.this, R.string.script_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void exportScript() {
        try {
            Script script = mDbManager.getScript(mScriptId);
            JSONObject json = new JSONObject();
            json.put("name", script.name);
            json.put("category", script.category);

            JSONArray actionsArray = new JSONArray();
            for (Action action : mActions) {
                JSONObject actionJson = new JSONObject();
                actionJson.put("name", action.name);
                actionJson.put("type", action.type);
                actionJson.put("x", action.x);
                actionJson.put("y", action.y);
                actionJson.put("endX", action.endX);
                actionJson.put("endY", action.endY);
                actionJson.put("duration", action.duration);
                actionJson.put("repeatCount", action.repeat);
                actionJson.put("waitTime", action.waitTime);

                if (action.type == Action.TYPE_MULTI) {
                    List<MultiAction> multiActions = mDbManager.getMultiActionsByParent(action.id);
                    JSONArray multiArray = new JSONArray();
                    for (MultiAction ma : multiActions) {
                        JSONObject maJson = new JSONObject();
                        maJson.put("type", ma.type);
                        maJson.put("x", ma.x);
                        maJson.put("y", ma.y);
                        maJson.put("endX", ma.endX);
                        maJson.put("endY", ma.endY);
                        maJson.put("duration", ma.duration);
                        maJson.put("delay", ma.delay);
                        multiArray.put(maJson);
                    }
                    actionJson.put("multiActions", multiArray);
                }
                actionsArray.put(actionJson);
            }
            json.put("actions", actionsArray);

            String fileName = script.name.replaceAll("[^a-zA-Z0-9]", "_") + ".json";
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(json.toString().getBytes());
            fos.close();

            Toast.makeText(this, String.format(getString(R.string.export_success), fileName), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteScriptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_script)
                .setMessage(R.string.delete_script_confirm)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDbManager.deleteScript(mScriptId);
                        finish();
                        Toast.makeText(ActionListActivity.this, R.string.script_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}