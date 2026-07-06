package pylapp.smoothclicker.android.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.DatabaseManager;
import pylapp.smoothclicker.android.database.Script;

import java.util.ArrayList;
import java.util.List;

public class ScriptListActivity extends AppCompatActivity {

    private ListView mLvScripts;
    private List<Script> mScripts;
    private ScriptListAdapter mAdapter;
    private DatabaseManager mDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbManager = new DatabaseManager(this);
        mDbManager.open();

        mLvScripts = findViewById(R.id.lvScripts);
        registerForContextMenu(mLvScripts);

        mLvScripts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Script script = mScripts.get(position);
                Intent intent = new Intent(ScriptListActivity.this, ActionListActivity.class);
                intent.putExtra("script_id", script.id);
                intent.putExtra("script_name", script.name);
                startActivity(intent);
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddScriptDialog();
            }
        });

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterScripts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScripts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbManager.close();
    }

    private void loadScripts() {
        mScripts = mDbManager.getAllScripts();
        int[] actionCounts = new int[mScripts.size()];
        for (int i = 0; i < mScripts.size(); i++) {
            actionCounts[i] = mDbManager.getActionCount(mScripts.get(i).id);
        }
        mAdapter = new ScriptListAdapter(this, mScripts, actionCounts);
        mLvScripts.setAdapter(mAdapter);
    }

    private void filterScripts(String query) {
        List<Script> filtered = new ArrayList<>();
        for (Script script : mScripts) {
            if (script.name.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(script);
            }
        }
        int[] actionCounts = new int[filtered.size()];
        for (int i = 0; i < filtered.size(); i++) {
            actionCounts[i] = mDbManager.getActionCount(filtered.get(i).id);
        }
        mAdapter = new ScriptListAdapter(this, filtered, actionCounts);
        mLvScripts.setAdapter(mAdapter);
    }

    private void showAddScriptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_script);

        final EditText input = new EditText(this);
        input.setHint(R.string.script_name_hint);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    Script script = new Script(name);
                    long id = mDbManager.insertScript(script);
                    if (id > 0) {
                        loadScripts();
                        Toast.makeText(ScriptListActivity.this, R.string.script_added, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ScriptListActivity.this, R.string.enter_name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_script_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Script script = mScripts.get(info.position);

        if (item.getItemId() == R.id.action_rename) {
            showRenameDialog(script);
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            showDeleteDialog(script);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showRenameDialog(final Script script) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rename_script);

        final EditText input = new EditText(this);
        input.setText(script.name);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    script.name = name;
                    script.updatedAt = System.currentTimeMillis();
                    mDbManager.updateScript(script);
                    loadScripts();
                    Toast.makeText(ScriptListActivity.this, R.string.script_renamed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showDeleteDialog(final Script script) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_script)
                .setMessage(R.string.delete_script_confirm)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDbManager.deleteScript(script.id);
                        loadScripts();
                        Toast.makeText(ScriptListActivity.this, R.string.script_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}