package pylapp.smoothclicker.android.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pylapp.smoothclicker.android.R;
import pylapp.smoothclicker.android.database.Action;
import pylapp.smoothclicker.android.database.DatabaseManager;
import pylapp.smoothclicker.android.database.MultiAction;
import pylapp.smoothclicker.android.database.Script;

public class ScriptListActivity extends AppCompatActivity {

    private static final int REQUEST_IMPORT = 100;

    private ListView mLvScripts;
    private List<Script> mScripts;
    private ScriptListAdapter mAdapter;
    private DatabaseManager mDbManager;
    private Spinner mSpCategory;
    private String mSelectedCategory = "";

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
                Log.d("SmoothClicker", "Script clicked at position: " + position);
                Script script = mScripts.get(position);
                Log.d("SmoothClicker", "Script: id=" + script.id + ", name=" + script.name);
                Intent intent = new Intent(ScriptListActivity.this, ActionListActivity.class);
                intent.putExtra("script_id", script.id);
                intent.putExtra("script_name", script.name);
                Log.d("SmoothClicker", "Starting ActionListActivity");
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

        Button btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/json");
                startActivityForResult(intent, REQUEST_IMPORT);
            }
        });

        mSpCategory = findViewById(R.id.spCategory);
        loadCategories();
        mSpCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = (String) parent.getItemAtPosition(position);
                if (category.equals(getString(R.string.all_categories))) {
                    mSelectedCategory = "";
                } else {
                    mSelectedCategory = category;
                }
                loadScripts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            importScript(uri);
        }
    }

    private void loadCategories() {
        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.all_categories));
        categories.addAll(mDbManager.getAllCategories());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCategory.setAdapter(adapter);
    }

    private void importScript(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            String name = json.getString("name");
            String category = json.optString("category", "");

            Script script = new Script(name, category);
            long scriptId = mDbManager.insertScript(script);

            if (scriptId > 0) {
                JSONArray actionsArray = json.getJSONArray("actions");
                for (int i = 0; i < actionsArray.length(); i++) {
                    JSONObject actionJson = actionsArray.getJSONObject(i);
                    String actionName = actionJson.optString("name", "");
                    int type = actionJson.getInt("type");
                    Action action = new Action(scriptId, actionName, type);
                    action.x = actionJson.optInt("x", 0);
                    action.y = actionJson.optInt("y", 0);
                    action.endX = actionJson.optInt("endX", -1);
                    action.endY = actionJson.optInt("endY", -1);
                    action.duration = actionJson.optInt("duration", 100);
                    action.repeat = actionJson.optInt("repeatCount", 1);
                    action.waitTime = actionJson.optInt("waitTime", 0);
                    action.orderNum = i;
                    long actionId = mDbManager.insertAction(action);

                    if (type == Action.TYPE_MULTI && actionJson.has("multiActions")) {
                        JSONArray multiArray = actionJson.getJSONArray("multiActions");
                        for (int j = 0; j < multiArray.length(); j++) {
                            JSONObject maJson = multiArray.getJSONObject(j);
                            MultiAction ma = new MultiAction(actionId, maJson.getInt("type"));
                            ma.x = maJson.optInt("x", 0);
                            ma.y = maJson.optInt("y", 0);
                            ma.endX = maJson.optInt("endX", -1);
                            ma.endY = maJson.optInt("endY", -1);
                            ma.duration = maJson.optInt("duration", 100);
                            ma.delay = maJson.optInt("delay", 0);
                            ma.orderNum = j;
                            mDbManager.insertMultiAction(ma);
                        }
                    }
                }
                loadCategories();
                loadScripts();
                Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show();
        }
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
        if (mSelectedCategory.isEmpty()) {
            mScripts = mDbManager.getAllScripts();
        } else {
            mScripts = mDbManager.getScriptsByCategory(mSelectedCategory);
        }
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

        View view = getLayoutInflater().inflate(R.layout.dialog_add_script, null);
        final EditText etName = view.findViewById(R.id.etName);
        final EditText etCategory = view.findViewById(R.id.etCategory);
        builder.setView(view);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                if (!name.isEmpty()) {
                    Script script = new Script(name, category);
                    long id = mDbManager.insertScript(script);
                    if (id > 0) {
                        loadCategories();
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

        View view = getLayoutInflater().inflate(R.layout.dialog_add_script, null);
        final EditText etName = view.findViewById(R.id.etName);
        final EditText etCategory = view.findViewById(R.id.etCategory);
        etName.setText(script.name);
        etCategory.setText(script.category);
        builder.setView(view);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                if (!name.isEmpty()) {
                    script.name = name;
                    script.category = category;
                    script.updatedAt = System.currentTimeMillis();
                    mDbManager.updateScript(script);
                    loadCategories();
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
                        loadCategories();
                        loadScripts();
                        Toast.makeText(ScriptListActivity.this, R.string.script_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}