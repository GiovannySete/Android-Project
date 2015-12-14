package giovanny.formapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Created by Giovanny on 25/11/2015.
 */
public class AnswersTable extends Activity implements Serializable {

    DatabaseAnswer dbAnswer;
    String formName;
    LinearLayout answerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers_table);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        answerLayout = (LinearLayout) findViewById(R.id.answer_layout);
        formName = getIntent().getSerializableExtra("thisForm").toString();
        setTitle("Dados de " + formName);

        dbAnswer = new DatabaseAnswer(getApplicationContext());
        if (dbAnswer != null && !isEmpty()) {
            showAnswers();
        } else {
            Toast toast = Toast.makeText(this, "Este formulário não contém respostas", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0,"Apagar todas as respostas");
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, FormsTable.class);
                startActivity(intent);
                finish();
                return true;
            case 1:
                deleteAllAnswers();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAnswers() {
        Cursor questionsCursor = dbAnswer.getReadableDatabase()
                .query(true, DatabaseAnswer.TABLE_NAME, new String[]{DatabaseAnswer.QUESTION, DatabaseAnswer.NUMBER_QUESTION}, DatabaseAnswer.FORM_NAME + " = ?", new String[]{formName}, null, null, null, null );
        MainActivity activity = new MainActivity();

        String question;
        String answer;
        String numberQuestion;
        while (questionsCursor.moveToNext()) {
            // configura views
            TextView questionView = new TextView(this);
            int num = 1;
            activity.viewConfig(this, questionView, null);

            numberQuestion = questionsCursor.getString(questionsCursor.getColumnIndexOrThrow(DatabaseAnswer.NUMBER_QUESTION));
            question = questionsCursor.getString(questionsCursor.getColumnIndexOrThrow(DatabaseAnswer.QUESTION));
            questionView.setText(question);
            questionView.setTypeface(null, Typeface.BOLD);
            answerLayout.addView(questionView);

            Cursor answerCursor = dbAnswer.getReadableDatabase()
                    .query(false, DatabaseAnswer.TABLE_NAME, new String[]{DatabaseAnswer.ANSWER}, DatabaseAnswer.NUMBER_QUESTION + " = ?  AND " + DatabaseAnswer.FORM_NAME + " = ?",
                            new String[]{numberQuestion, formName}, null, null, null, null);
            while (answerCursor.moveToNext()) {
                TextView answerView = new TextView(this);
                answer = "  " + num + " -  " + answerCursor.getString(answerCursor.getColumnIndexOrThrow(DatabaseAnswer.ANSWER));
                answerView.setText(answer);
                activity.viewConfig(this, null, answerView);
                answerLayout.addView(answerView);
                num++;
            }
            answerCursor.close();
        }
        questionsCursor.close();
    }

    public void deleteAllAnswers(){
        if (!isEmpty()) {
            dbAnswer.getWritableDatabase().delete(DatabaseAnswer.TABLE_NAME, DatabaseAnswer.FORM_NAME + " = ?", new String[]{formName});
            dbAnswer.close();
        }
    }

    public boolean isEmpty() {
        Cursor mCursor = dbAnswer.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseAnswer.TABLE_NAME + " WHERE " + DatabaseAnswer.FORM_NAME + " = ?", new String[]{formName});
        return (mCursor.moveToFirst() ? false : true);
    }
}
