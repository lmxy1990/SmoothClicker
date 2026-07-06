package pylapp.smoothclicker.android.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import pylapp.smoothclicker.android.views.PointsListAdapter.Point;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ActionListActivity extends AppCompatActivity {

    public static final int REQUEST_EDIT_ACTION = 100;

    private ListView mLvActions;
    private List<Action> mActions;
    private ActionAdapter mAdapter;
    private DatabaseManager mDbManager;
    private long mScriptId;
    private String mScriptName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_list_new);

        Intent intent = getIntent();
        mScriptId = intent.getLongExtra("script_id", -1);
        mScriptName = intent.getStringExtra("script_name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mScriptName);

        mDbManager = new DatabaseManager(this);
        mDbManager.open();

        mLvActions = findViewById(R.id.lvActions);
        registerForContextMenu(mLvActions);

        mLvActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Action action = mActions.get(position);
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
        });

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
        mAdapter = new ActionAdapter(this, mActions);
        mLvActions.setAdapter(mAdapter);
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                    InputStream inputStream = process.getInputStream();
                    byte[] buffer = new byte[1024];

                    for (Action action : mActions) {
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
                            continue;
                        }
                        outputStream.writeBytes(cmd);
                        outputStream.flush();
                        Thread.sleep(action.waitTime);
                    }
                    outputStream.close();
                    process.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Toast.makeText(this, R.string.script_playing, Toast.LENGTH_SHORT).show();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_action_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Action action = mActions.get(info.position);

        if (item.getItemId() == R.id.action_delete_action) {
            mDbManager.deleteAction(action.id);
            loadActions();
            Toast.makeText(this, R.string.action_deleted, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}