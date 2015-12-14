package giovanny.formapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashMap;

import static android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends Activity {

    public DatabaseOpenHelper formDb;
    private LinkedHashMap<String, Integer> formTable;
    private Button button;
    private Button adicionar;
    private LinearLayout form;
    private FragmentTransaction transaction;
    protected static final LayoutParams defaultLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        formTable = new LinkedHashMap<>();
        formDb = new DatabaseOpenHelper(this);
        form = (LinearLayout) findViewById(R.id.form);
        button = (Button) findViewById(R.id.button);
        registerForContextMenu(button);
        adicionar = (Button) findViewById(R.id.button2);
        adicionar.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveForm();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.to_forms:
                Intent intent = new Intent(this, FormsTable.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.button) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            menu.setHeaderTitle("Selecione um tipo de resposta");
        } else super.onCreateContextMenu(menu, v, menuInfo);
    }

    public void openFields(View view) {
        openContextMenu(view);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        // Apenas para testar se a seleção dos itens do menu funciona
        openDialog(item.getItemId());

        return super.onContextItemSelected(item);
    }

    public void openDialog(int item){
        transaction = getFragmentManager().beginTransaction();
        DialogBuilder builder = new DialogBuilder(item);
        builder.show(transaction, "dialog");
    }

    public void putData(View question, View answer, int type){
        // Configuracao de layout das views
        viewConfig(MainActivity.this, question, answer);
        // Adicionando ao hash de recuperacao
        formTable.put(((TextView)question).getText().toString(), type);
        // Configurando os listeners
        form.addView(question, defaultLayoutParams);
        form.addView(answer, defaultLayoutParams);
    }

    // Metodo de configuracao das views a serem inseridas
    public void viewConfig(Context context, View question, View answer) {
        int paddingPixel = 10;
        float density = context.getResources().getDisplayMetrics().density;
        if (question != null) {
            question.setPadding(0, (int) (paddingPixel * density), 0, 0); //Padding em dp
            ((TextView) question).setTextAppearance(context, android.R.style.TextAppearance_Medium);
        }
        paddingPixel = 5;
        if (answer != null) {
            answer.setPadding((int) (paddingPixel * density), (int) (paddingPixel * density), 0, 0); //Padding em dp
            answer.setSelected(false);
            ((TextView) answer).setTextColor(Color.BLACK);
        }
    }



    // Salva o formulario
    public void saveForm (){
        int numberOfQuestions = form.getChildCount();

        if(numberOfQuestions > 0) { // so salva se o formulario nao esta vazio
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Salvar formulário");
            final EditText formName = new EditText(this);
            formName.setTextAppearance(this, android.R.style.TextAppearance_Large);
            formName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            formName.setHint("Nome do formulário");
            builder.setView(formName);
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = formName.getText().toString();
                    if(name != null && !name.trim().equals("")) {
                        if (unique(name))
                            insertToFormDB(name);
                        else {
                            Toast toast = Toast.makeText(MainActivity.this, "Já existe um formulário chamado \"" + name + "\". Tente outro nome", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, "Digite uma nome para o formulário", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else
            Toast.makeText(this, "Formulário vazio", Toast.LENGTH_SHORT).show();
    }

    public void insertToFormDB(String name) {
        // Salva conteudo no bd
        ContentValues values = new ContentValues();
        byte formBytes[] = Serializer.serializeObject(formTable);
        values.put(DatabaseOpenHelper.FORM_NAME, name.trim());
        values.put(DatabaseOpenHelper.FORM, formBytes);
        formDb.getWritableDatabase().insert(DatabaseOpenHelper.TABLE_NAME, null, values);

        // Chama activity que mostra o bd
        Intent intent = new Intent(this, FormsTable.class);
        startActivity(intent);
        form.removeAllViewsInLayout();
    }

    // Verifica se o nome do formulario e unico
    public boolean unique(String name) {
        Cursor cursor = formDb.getReadableDatabase()
                .query(false, DatabaseOpenHelper.TABLE_NAME, new String[]{DatabaseOpenHelper.FORM_NAME}, DatabaseOpenHelper.FORM_NAME + " = ?" , new String[]{name.trim()}, null, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return false;
        } else
            return true;
    }

}
