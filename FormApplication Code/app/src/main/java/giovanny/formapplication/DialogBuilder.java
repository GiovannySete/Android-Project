package giovanny.formapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


/**
 * Created by Giovanny on 14/11/2015.
 */
public class DialogBuilder extends DialogFragment{

    private int item;
    private EditText editText;
    private static int answerTag = 0;

    public DialogBuilder(int item){
        this.item = item;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        super.onCreateView(inflater, container, saveInstanceState);
        View view;

        final MainActivity  mainActivity = (MainActivity) getActivity();
        final TextView questionView = new TextView(getActivity());
        final TextView answer1 = new TextView(getActivity());
        final EditText answer2 = new EditText(getActivity());

        final int type;
        final int flag;
        switch (item) {
            case R.id.data:
                view = inflater.inflate(R.layout.insert_data, container, false);
                getDialog().setTitle("Entre com sua questão");
                flag = 1;
                type = InputType.TYPE_DATETIME_VARIATION_DATE;
                answer1.setInputType(type);
                answer1.setHint("Toque para escolher a data");
                answer1.setId(answerTag++);
                openDatePicker(answer1, mainActivity);
                break;
            case R.id.texto:
                view = inflater.inflate(R.layout.insert_text, container);
                getDialog().setTitle("Texto");
                flag = 2;
                type = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
                answer2.setInputType(type);
                answer2.setId(answerTag++);
                break;
            case R.id.numerico:
                view = inflater.inflate(R.layout.insert_numeric, container);
                getDialog().setTitle("Numérico");
                flag = 2;
                type = InputType.TYPE_CLASS_NUMBER;
                answer2.setInputType(type);
                answer2.setId(answerTag++);
                break;
            case R.id.localizacao:
                view = inflater.inflate(R.layout.insert_local, container);
                getDialog().setTitle("Localização");
                flag = 1;
                type = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS;
                answer1.setInputType(type);
                answer1.setHint("Toque para escolher a localização");
                answer1.setId(answerTag++);
                answer1.setOnClickListener(new EditText.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MapBuilder mapBuilder = new MapBuilder(mainActivity);
                        mapBuilder.openMapDialog(answer1.getId());
                    }
                });
                break;
            default:
                flag = -1;
                view = null;
                type = -1;
        }
        editText = (EditText) view.findViewById(R.id.textView);
        if(view != null) {
            Button exit = (Button) view.findViewById(R.id.exit);
            exit.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            Button add = (Button) view.findViewById(R.id.add);
            add.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String question = editText.getText().toString();
                    if (question == null || question.trim().equals("")) {
                        Toast.makeText(getActivity(), "Digite uma pergunta", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        questionView.setText(question);
                        if (flag == 1) mainActivity.putData(questionView, answer1, type);
                        else mainActivity.putData(questionView, answer2, type);
                        dismiss();
                    }
                }
            });
        }
        return view;
    }

    // Abre o DatePicker e salva a data escolhida
    public void openDatePicker(final TextView textView, final Activity activity) {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int monthOfYear = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                textView.setText(new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year));
            }
        };

        textView.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
              DatePickerDialog dialog = new DatePickerDialog(activity, dateListener, year, monthOfYear, dayOfMonth);
                dialog.setTitle("Selecione a data");
                dialog.show();
            }
        });
    }
}
