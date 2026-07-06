package pylapp.smoothclicker.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smoothclicker.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_SCRIPTS = "scripts";
    public static final String COLUMN_SCRIPT_ID = "_id";
    public static final String COLUMN_SCRIPT_NAME = "name";
    public static final String COLUMN_SCRIPT_CATEGORY = "category";
    public static final String COLUMN_SCRIPT_CREATED_AT = "created_at";
    public static final String COLUMN_SCRIPT_UPDATED_AT = "updated_at";

    public static final String TABLE_ACTIONS = "actions";
    public static final String COLUMN_ACTION_ID = "_id";
    public static final String COLUMN_ACTION_SCRIPT_ID = "script_id";
    public static final String COLUMN_ACTION_NAME = "name";
    public static final String COLUMN_ACTION_TYPE = "type";
    public static final String COLUMN_ACTION_X = "x";
    public static final String COLUMN_ACTION_Y = "y";
    public static final String COLUMN_ACTION_END_X = "end_x";
    public static final String COLUMN_ACTION_END_Y = "end_y";
    public static final String COLUMN_ACTION_DURATION = "duration";
    public static final String COLUMN_ACTION_WAIT_TIME = "wait_time";
    public static final String COLUMN_ACTION_REPEAT = "repeat";
    public static final String COLUMN_ACTION_ORDER = "order_num";

    public static final String TABLE_MULTI_ACTIONS = "multi_actions";
    public static final String COLUMN_MULTI_ID = "_id";
    public static final String COLUMN_MULTI_PARENT_ID = "parent_id";
    public static final String COLUMN_MULTI_TYPE = "type";
    public static final String COLUMN_MULTI_X = "x";
    public static final String COLUMN_MULTI_Y = "y";
    public static final String COLUMN_MULTI_END_X = "end_x";
    public static final String COLUMN_MULTI_END_Y = "end_y";
    public static final String COLUMN_MULTI_DURATION = "duration";
    public static final String COLUMN_MULTI_WAIT_TIME = "wait_time";
    public static final String COLUMN_MULTI_REPEAT = "repeat";
    public static final String COLUMN_MULTI_DELAY = "delay";
    public static final String COLUMN_MULTI_ORDER = "order_num";

    private static final String CREATE_TABLE_SCRIPTS =
            "CREATE TABLE " + TABLE_SCRIPTS + " (" +
                    COLUMN_SCRIPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SCRIPT_NAME + " TEXT NOT NULL, " +
                    COLUMN_SCRIPT_CATEGORY + " TEXT DEFAULT '', " +
                    COLUMN_SCRIPT_CREATED_AT + " INTEGER NOT NULL, " +
                    COLUMN_SCRIPT_UPDATED_AT + " INTEGER NOT NULL);";

    private static final String CREATE_TABLE_ACTIONS =
            "CREATE TABLE " + TABLE_ACTIONS + " (" +
                    COLUMN_ACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ACTION_SCRIPT_ID + " INTEGER NOT NULL, " +
                    COLUMN_ACTION_NAME + " TEXT NOT NULL, " +
                    COLUMN_ACTION_TYPE + " INTEGER NOT NULL, " +
                    COLUMN_ACTION_X + " INTEGER DEFAULT 0, " +
                    COLUMN_ACTION_Y + " INTEGER DEFAULT 0, " +
                    COLUMN_ACTION_END_X + " INTEGER DEFAULT -1, " +
                    COLUMN_ACTION_END_Y + " INTEGER DEFAULT -1, " +
                    COLUMN_ACTION_DURATION + " INTEGER DEFAULT 100, " +
                    COLUMN_ACTION_WAIT_TIME + " INTEGER DEFAULT 0, " +
                    COLUMN_ACTION_REPEAT + " INTEGER DEFAULT 1, " +
                    COLUMN_ACTION_ORDER + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COLUMN_ACTION_SCRIPT_ID + ") REFERENCES " + TABLE_SCRIPTS + "(" + COLUMN_SCRIPT_ID + "));";

    private static final String CREATE_TABLE_MULTI_ACTIONS =
            "CREATE TABLE " + TABLE_MULTI_ACTIONS + " (" +
                    COLUMN_MULTI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MULTI_PARENT_ID + " INTEGER NOT NULL, " +
                    COLUMN_MULTI_TYPE + " INTEGER NOT NULL, " +
                    COLUMN_MULTI_X + " INTEGER DEFAULT 0, " +
                    COLUMN_MULTI_Y + " INTEGER DEFAULT 0, " +
                    COLUMN_MULTI_END_X + " INTEGER DEFAULT -1, " +
                    COLUMN_MULTI_END_Y + " INTEGER DEFAULT -1, " +
                    COLUMN_MULTI_DURATION + " INTEGER DEFAULT 100, " +
                    COLUMN_MULTI_WAIT_TIME + " INTEGER DEFAULT 0, " +
                    COLUMN_MULTI_REPEAT + " INTEGER DEFAULT 1, " +
                    COLUMN_MULTI_DELAY + " INTEGER DEFAULT 0, " +
                    COLUMN_MULTI_ORDER + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COLUMN_MULTI_PARENT_ID + ") REFERENCES " + TABLE_ACTIONS + "(" + COLUMN_ACTION_ID + "));";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SCRIPTS);
        db.execSQL(CREATE_TABLE_ACTIONS);
        db.execSQL(CREATE_TABLE_MULTI_ACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_SCRIPTS + " ADD COLUMN " + COLUMN_SCRIPT_CATEGORY + " TEXT DEFAULT '';");
        }
    }
}