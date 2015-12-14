package giovanny.formapplication;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by Giovanny on 25/11/2015.
 */
public class MapBuilder extends Fragment{
    Activity activity;
    public MapBuilder(Activity activity){
        this.activity = activity;
    }

    // Abre o Mapa e pega o endereco escolhido
    public void openMapDialog(int id) {
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        DialogMap dialogMap = new DialogMap(id);
        dialogMap.show(transaction, "map");
    }

}
