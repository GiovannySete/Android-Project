package giovanny.formapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by Giovanny on 22/11/2015.
 */

public class FormsTable extends Activity implements Serializable{

    private DatabaseOpenHelper db = null;
    private SimpleCursorAdapter adapter;
    private ListView listView;
    public int lvPosition;
    private DatabaseAnswer databaseAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_forms);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.columns);
        registerForContextMenu(listView);
        databaseAnswer = new DatabaseAnswer(getApplicationContext());
        db = new DatabaseOpenHelper(getApplicationContext());
        if (db != null && !isEmpty()){
            showDB();
        } else {
            Toast toast = Toast.makeText(this, "Banco de dados vazio", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_data_base_table, menu);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.columns) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.form_context_menu, menu);
        } else super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.answering:
                recreateForm(lvPosition);
                return true;
            case R.id.view_data:
                showFormAnswers();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (db != null && !isEmpty()){
            showDB();
        }
    }

    public boolean isEmpty() {
        Cursor mCursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseOpenHelper.TABLE_NAME, null);
        return (mCursor.moveToFirst() ? false : true);
    }

    public void showDB() {
        Cursor cursor = db.getWritableDatabase().query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[]{}, null, null, null);

        adapter = new SimpleCursorAdapter(this, R.layout.forms_layout, cursor,
                new String[]{DatabaseOpenHelper.FORM_NAME},
                new int[]{R.id.form_name}, 0);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lvPosition = position;
                openContextMenu(view);
                //recreateForm(position);
            }
        });
    }

    private void recreateForm(int position) {
        Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        byte[] formBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseOpenHelper.FORM));
        LinkedHashMap<String, Integer> form = (LinkedHashMap<String, Integer>) Serializer.deserializeObject(formBytes);
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseOpenHelper.FORM_NAME));
        Intent intent = new Intent(this, RecreateForm.class);
        intent.putExtra("formQuestions", formBytes);
        intent.putExtra("formName", title);
        startActivity(intent);
    }

    private void showFormAnswers() {
        Cursor cursor = (Cursor) listView.getItemAtPosition(lvPosition);
        String form = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseOpenHelper.FORM_NAME));
        Intent intent = new Intent(this, AnswersTable.class);
        intent.putExtra("thisForm", form);
        startActivity(intent);
    }

    public void deleteDB(View view) {
        if (!isEmpty()) {
            listView.setAdapter(null);
            db.getWritableDatabase().delete(DatabaseOpenHelper.TABLE_NAME, null, null);
            // Deleta respostas associadas ao formulario
            databaseAnswer.getWritableDatabase().delete(DatabaseAnswer.TABLE_NAME, DatabaseAnswer.FORM_NAME + " = ?", new String[]{(String) getIntent().getSerializableExtra("formName")});
            // Fechando o banco
            db.getWritableDatabase().close();
        }
    }
}
