package giovanny.formapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Giovanny on 22/11/2015.
 */

public class RecreateForm extends Activity implements Serializable{

    public DatabaseAnswer answerDb;
    public LinkedHashMap<String, Integer> formQuestions;
    public Set<String> set;
    public LinearLayout recreatedForm;
    static int viewId = 0;

    protected static final LinearLayout.LayoutParams defaultLayoutParams =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recreate_form);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        setTitle(intent.getSerializableExtra("formName").toString());

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        answerDb = new DatabaseAnswer(getApplicationContext());
        formQuestions = (LinkedHashMap<String, Integer>) Serializer.deserializeObject((byte[])intent.getSerializableExtra("formQuestions"));
        set = formQuestions.keySet();
        recreatedForm = (LinearLayout) findViewById(R.id.recreated_form);

        buildForm();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildForm() {

        final MainActivity activity = new MainActivity();
        for (String question : set) {
            final TextView answer1 = new TextView(this);
            final EditText answer2 = new EditText(this);
            TextView questionView = new TextView(this);
            questionView.setText(question);
            int type =  formQuestions.get(question);
            int flag;
            switch (type){
                case (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES):
                    flag = 2;
                    answer2.setInputType(type);
                    break;
                case InputType.TYPE_CLASS_NUMBER:
                    flag = 2;
                    answer2.setInputType(type);
                    break;
                case InputType.TYPE_DATETIME_VARIATION_DATE:
                    flag = 1;
                    answer1.setHint("Toque para escolher a data");
                    answer1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogBuilder builder = new DialogBuilder(0);
                            builder.openDatePicker(answer1, RecreateForm.this);
                        }
                    });
                    break;
                case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
                    flag = 1;
                    answer1.setId(viewId++);
                    answer1.setHint("Toque para escolher a localização");
                    answer1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapBuilder mapBuilder = new MapBuilder(RecreateForm.this);
                            mapBuilder.openMapDialog(answer1.getId());
                        }
                    });
                    break;
                default: flag = -1;
            }
            if (flag == 1) {
                activity.viewConfig(RecreateForm.this, questionView, answer1);
                recreatedForm.addView(questionView, defaultLayoutParams);
                recreatedForm.addView(answer1, defaultLayoutParams);
            } else {
                activity.viewConfig(RecreateForm.this, questionView, answer2);
                recreatedForm.addView(questionView, defaultLayoutParams);
                recreatedForm.addView(answer2, defaultLayoutParams);
            }
        }
//        inputMethodManager.hideSoftInputFromWindow(recreatedForm.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void saveAnswers(View view){
        String question;
        String answer;
        boolean ok = true;
        int numberOfQuestions = recreatedForm.getChildCount();

        // Testa se o formulario esta completamente preenchido
        for(int j = 1; j < numberOfQuestions; j +=2) {
            answer = ((TextView)recreatedForm.getChildAt(j)).getText().toString().trim();
            if(answer == null || answer.equals("")) {
                ok = false;
                break;
            }
        }
        // Salva se o formulario foi completamente preenchido
        if (ok) {
            for (int i = 0, j = 1; i < numberOfQuestions; i += 2, j += 2) {
                question = ((TextView) recreatedForm.getChildAt(i)).getText().toString();
                answer = ((TextView) recreatedForm.getChildAt(j)).getText().toString().trim();
                insertToAnswerDB(getTitle().toString(), Integer.toString(i), question, answer);
            }
            Intent intent = new Intent(this, AnswersTable.class);
            intent.putExtra("thisForm", getTitle());
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(this, "Por favor, preencha todo o formulário", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    public void insertToAnswerDB(String formName, String numberQuestion, String question, String asnwer) {
        //Salva conteudo no bd
        ContentValues values = new ContentValues();
        values.put(DatabaseAnswer.FORM_NAME, formName);
        values.put(DatabaseAnswer.NUMBER_QUESTION, numberQuestion);
        values.put(DatabaseAnswer.QUESTION, question);
        values.put(DatabaseAnswer.ANSWER, asnwer);
        answerDb.getWritableDatabase().insert(DatabaseAnswer.TABLE_NAME, null, values);

//        recreatedForm.clearDisappearingChildren();
    }
}
