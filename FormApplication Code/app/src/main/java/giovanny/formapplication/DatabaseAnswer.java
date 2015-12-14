package giovanny.formapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Giovanny on 24/11/2015.
 */
public class DatabaseAnswer extends SQLiteOpenHelper {
    final static String TABLE_NAME = "table_asnwer";
    final static String _ID = "_id";
    final static String FORM_NAME = "form_name";
    final static String NUMBER_QUESTION = "number_question";
    final static String QUESTION = "question";
    final static String ANSWER = "answer";
    final static String[] columns = { _ID, FORM_NAME, NUMBER_QUESTION, QUESTION, ANSWER};

    static final String CREATE_CMD = "CREATE TABLE table_asnwer"+
            "( "+_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FORM_NAME +" TEXT NOT NULL, " +
            NUMBER_QUESTION +" TEXT NOT NULL, " +
            QUESTION +" TEXT NOT NULL, " +
            ANSWER +" TEXT )";

    final private static String NAME = "answer_db";
    final private static Integer VERSION = 1;
    final private Context mContext;

    public DatabaseAnswer(Context context) {
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
