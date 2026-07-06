package pylapp.smoothclicker.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertScript(Script script) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_SCRIPT_NAME, script.name);
        values.put(DBHelper.COLUMN_SCRIPT_CREATED_AT, script.createdAt);
        values.put(DBHelper.COLUMN_SCRIPT_UPDATED_AT, script.updatedAt);
        return database.insert(DBHelper.TABLE_SCRIPTS, null, values);
    }

    public void updateScript(Script script) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_SCRIPT_NAME, script.name);
        values.put(DBHelper.COLUMN_SCRIPT_UPDATED_AT, script.updatedAt);
        database.update(DBHelper.TABLE_SCRIPTS, values,
                DBHelper.COLUMN_SCRIPT_ID + " = ?",
                new String[]{String.valueOf(script.id)});
    }

    public void deleteScript(long scriptId) {
        database.delete(DBHelper.TABLE_MULTI_ACTIONS,
                DBHelper.COLUMN_MULTI_PARENT_ID + " IN (SELECT " + DBHelper.COLUMN_ACTION_ID + " FROM " + DBHelper.TABLE_ACTIONS + " WHERE " + DBHelper.COLUMN_ACTION_SCRIPT_ID + " = ?)",
                new String[]{String.valueOf(scriptId)});
        database.delete(DBHelper.TABLE_ACTIONS,
                DBHelper.COLUMN_ACTION_SCRIPT_ID + " = ?",
                new String[]{String.valueOf(scriptId)});
        database.delete(DBHelper.TABLE_SCRIPTS,
                DBHelper.COLUMN_SCRIPT_ID + " = ?",
                new String[]{String.valueOf(scriptId)});
    }

    public List<Script> getAllScripts() {
        List<Script> scripts = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_SCRIPTS,
                null, null, null, null, null,
                DBHelper.COLUMN_SCRIPT_CREATED_AT + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Script script = cursorToScript(cursor);
            scripts.add(script);
            cursor.moveToNext();
        }
        cursor.close();
        return scripts;
    }

    public Script getScript(long scriptId) {
        Cursor cursor = database.query(DBHelper.TABLE_SCRIPTS,
                null, DBHelper.COLUMN_SCRIPT_ID + " = ?",
                new String[]{String.valueOf(scriptId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            Script script = cursorToScript(cursor);
            cursor.close();
            return script;
        }
        cursor.close();
        return null;
    }

    private Script cursorToScript(Cursor cursor) {
        Script script = new Script();
        script.id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_SCRIPT_ID));
        script.name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_SCRIPT_NAME));
        script.createdAt = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_SCRIPT_CREATED_AT));
        script.updatedAt = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_SCRIPT_UPDATED_AT));
        return script;
    }

    public long insertAction(Action action) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_ACTION_SCRIPT_ID, action.scriptId);
        values.put(DBHelper.COLUMN_ACTION_NAME, action.name);
        values.put(DBHelper.COLUMN_ACTION_TYPE, action.type);
        values.put(DBHelper.COLUMN_ACTION_X, action.x);
        values.put(DBHelper.COLUMN_ACTION_Y, action.y);
        values.put(DBHelper.COLUMN_ACTION_END_X, action.endX);
        values.put(DBHelper.COLUMN_ACTION_END_Y, action.endY);
        values.put(DBHelper.COLUMN_ACTION_DURATION, action.duration);
        values.put(DBHelper.COLUMN_ACTION_WAIT_TIME, action.waitTime);
        values.put(DBHelper.COLUMN_ACTION_REPEAT, action.repeat);
        values.put(DBHelper.COLUMN_ACTION_ORDER, action.orderNum);
        return database.insert(DBHelper.TABLE_ACTIONS, null, values);
    }

    public void updateAction(Action action) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_ACTION_NAME, action.name);
        values.put(DBHelper.COLUMN_ACTION_TYPE, action.type);
        values.put(DBHelper.COLUMN_ACTION_X, action.x);
        values.put(DBHelper.COLUMN_ACTION_Y, action.y);
        values.put(DBHelper.COLUMN_ACTION_END_X, action.endX);
        values.put(DBHelper.COLUMN_ACTION_END_Y, action.endY);
        values.put(DBHelper.COLUMN_ACTION_DURATION, action.duration);
        values.put(DBHelper.COLUMN_ACTION_WAIT_TIME, action.waitTime);
        values.put(DBHelper.COLUMN_ACTION_REPEAT, action.repeat);
        values.put(DBHelper.COLUMN_ACTION_ORDER, action.orderNum);
        database.update(DBHelper.TABLE_ACTIONS, values,
                DBHelper.COLUMN_ACTION_ID + " = ?",
                new String[]{String.valueOf(action.id)});
    }

    public void deleteAction(long actionId) {
        database.delete(DBHelper.TABLE_MULTI_ACTIONS,
                DBHelper.COLUMN_MULTI_PARENT_ID + " = ?",
                new String[]{String.valueOf(actionId)});
        database.delete(DBHelper.TABLE_ACTIONS,
                DBHelper.COLUMN_ACTION_ID + " = ?",
                new String[]{String.valueOf(actionId)});
    }

    public List<Action> getActionsByScript(long scriptId) {
        List<Action> actions = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_ACTIONS,
                null, DBHelper.COLUMN_ACTION_SCRIPT_ID + " = ?",
                new String[]{String.valueOf(scriptId)},
                null, null, DBHelper.COLUMN_ACTION_ORDER + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Action action = cursorToAction(cursor);
            actions.add(action);
            cursor.moveToNext();
        }
        cursor.close();
        return actions;
    }

    public Action getAction(long actionId) {
        Cursor cursor = database.query(DBHelper.TABLE_ACTIONS,
                null, DBHelper.COLUMN_ACTION_ID + " = ?",
                new String[]{String.valueOf(actionId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            Action action = cursorToAction(cursor);
            cursor.close();
            return action;
        }
        cursor.close();
        return null;
    }

    private Action cursorToAction(Cursor cursor) {
        Action action = new Action();
        action.id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_ID));
        action.scriptId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_SCRIPT_ID));
        action.name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_NAME));
        action.type = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_TYPE));
        action.x = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_X));
        action.y = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_Y));
        action.endX = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_END_X));
        action.endY = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_END_Y));
        action.duration = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_DURATION));
        action.waitTime = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_WAIT_TIME));
        action.repeat = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_REPEAT));
        action.orderNum = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTION_ORDER));
        return action;
    }

    public int getActionCount(long scriptId) {
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + DBHelper.TABLE_ACTIONS + " WHERE " + DBHelper.COLUMN_ACTION_SCRIPT_ID + " = ?",
                new String[]{String.valueOf(scriptId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public long insertMultiAction(MultiAction multiAction) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MULTI_PARENT_ID, multiAction.parentId);
        values.put(DBHelper.COLUMN_MULTI_TYPE, multiAction.type);
        values.put(DBHelper.COLUMN_MULTI_X, multiAction.x);
        values.put(DBHelper.COLUMN_MULTI_Y, multiAction.y);
        values.put(DBHelper.COLUMN_MULTI_END_X, multiAction.endX);
        values.put(DBHelper.COLUMN_MULTI_END_Y, multiAction.endY);
        values.put(DBHelper.COLUMN_MULTI_DURATION, multiAction.duration);
        values.put(DBHelper.COLUMN_MULTI_WAIT_TIME, multiAction.waitTime);
        values.put(DBHelper.COLUMN_MULTI_REPEAT, multiAction.repeat);
        values.put(DBHelper.COLUMN_MULTI_DELAY, multiAction.delay);
        values.put(DBHelper.COLUMN_MULTI_ORDER, multiAction.orderNum);
        return database.insert(DBHelper.TABLE_MULTI_ACTIONS, null, values);
    }

    public void updateMultiAction(MultiAction multiAction) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MULTI_TYPE, multiAction.type);
        values.put(DBHelper.COLUMN_MULTI_X, multiAction.x);
        values.put(DBHelper.COLUMN_MULTI_Y, multiAction.y);
        values.put(DBHelper.COLUMN_MULTI_END_X, multiAction.endX);
        values.put(DBHelper.COLUMN_MULTI_END_Y, multiAction.endY);
        values.put(DBHelper.COLUMN_MULTI_DURATION, multiAction.duration);
        values.put(DBHelper.COLUMN_MULTI_WAIT_TIME, multiAction.waitTime);
        values.put(DBHelper.COLUMN_MULTI_REPEAT, multiAction.repeat);
        values.put(DBHelper.COLUMN_MULTI_DELAY, multiAction.delay);
        values.put(DBHelper.COLUMN_MULTI_ORDER, multiAction.orderNum);
        database.update(DBHelper.TABLE_MULTI_ACTIONS, values,
                DBHelper.COLUMN_MULTI_ID + " = ?",
                new String[]{String.valueOf(multiAction.id)});
    }

    public void deleteMultiAction(long multiActionId) {
        database.delete(DBHelper.TABLE_MULTI_ACTIONS,
                DBHelper.COLUMN_MULTI_ID + " = ?",
                new String[]{String.valueOf(multiActionId)});
    }

    public List<MultiAction> getMultiActionsByParent(long parentId) {
        List<MultiAction> actions = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_MULTI_ACTIONS,
                null, DBHelper.COLUMN_MULTI_PARENT_ID + " = ?",
                new String[]{String.valueOf(parentId)},
                null, null, DBHelper.COLUMN_MULTI_ORDER + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MultiAction action = cursorToMultiAction(cursor);
            actions.add(action);
            cursor.moveToNext();
        }
        cursor.close();
        return actions;
    }

    private MultiAction cursorToMultiAction(Cursor cursor) {
        MultiAction action = new MultiAction();
        action.id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_ID));
        action.parentId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_PARENT_ID));
        action.type = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_TYPE));
        action.x = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_X));
        action.y = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_Y));
        action.endX = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_END_X));
        action.endY = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_END_Y));
        action.duration = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_DURATION));
        action.waitTime = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_WAIT_TIME));
        action.repeat = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_REPEAT));
        action.delay = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_DELAY));
        action.orderNum = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MULTI_ORDER));
        return action;
    }

    public void reorderActions(long scriptId, List<Long> actionIds) {
        for (int i = 0; i < actionIds.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_ACTION_ORDER, i);
            database.update(DBHelper.TABLE_ACTIONS, values,
                    DBHelper.COLUMN_ACTION_ID + " = ?",
                    new String[]{String.valueOf(actionIds.get(i))});
        }
    }
}