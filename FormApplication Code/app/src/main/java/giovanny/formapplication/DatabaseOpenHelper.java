package giovanny.formapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Giovanny on 07/05/2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    final static String TABLE_NAME = "local";
    final static String FORM_NAME = "data";
    final static String _ID = "_id";
    final static String FORM = "type";
    final static String[] columns = { _ID, FORM_NAME, FORM};

    static final String CREATE_CMD = "CREATE TABLE local"+
            "( "+_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FORM_NAME +" TEXT NOT NULL, " +
            FORM +" BLOB NOT NULL)";

    final private static String NAME = "form_db";
    final private static Integer VERSION = 1;
    final private Context mContext;

    public DatabaseOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    void deleteDatabase() {
       mContext.deleteDatabase(NAME);
    }
}
