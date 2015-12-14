package giovanny.formapplication;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import giovanny.formapplication.R;

/**
 * Created by Giovanny on 17/11/2015.
 */
public class DialogMap extends DialogFragment {

    GoogleMap map;
    MarkerOptions markerOptions = new MarkerOptions();
    Location location;
    LocationManager locationManager;
    TextView parent;

    int parentId;
    public DialogMap(int parentId) {
        this.parentId = parentId;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setCancelable(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        parent = new TextView(getActivity());
        parent.getParent();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        super.onCreateView(inflater, container, saveInstanceState);
        View mapView;
        mapView = inflater.inflate(R.layout.map_layout, container);
        getDialog().setTitle("Defina a localização");

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);


        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                testProviders();
                location = map.getMyLocation();
                if (location != null) {
                    EditText editText = (EditText) getView().findViewById(R.id.search_view);
                    editText.setText(new StringBuilder().append(location.getLatitude()).append(", ").append(location.getLongitude()));
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    markerOptions.title("Você está aqui");
                    markerOptions.position(latLng);
                    map.addMarker(markerOptions);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                return true;
            }
        });

        Button exit = (Button) mapView.findViewById(R.id.exit);
        exit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Button add = (Button) mapView.findViewById(R.id.add);
        add.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLocation();
                dismiss();
            }
        });
        final Button search = (Button) mapView.findViewById(R.id.search_btn);
        search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAddress();
            }
        });

        return mapView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null)
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
    }

    public void searchAddress(){
        EditText searchText = (EditText) getView().findViewById(R.id.search_view);
        String location = searchText.getText().toString().trim();
        if(location != null && !location.equals("")) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            new GeocoderTask().execute(location);
        }
    }

    private void sendLocation() {
        EditText searchText = (EditText) getView().findViewById(R.id.search_view);
        String location = searchText.getText().toString().trim();
        if (location != null && !location.equals("")) {
            TextView locResult = (TextView) getActivity().findViewById(parentId);
            locResult.setText(location);
        }

    }

    public void testProviders() {
        if (locationManager != null) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Toast toast = Toast.makeText(getActivity(), "Ative o serviço de localização para utilizar esta função.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        }
    }

    public void showGPSDisabled() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Atenção");
        alertDialog.setMessage("O GPS está desabilitado. Deseja habilitá-lo agora?");
        alertDialog.setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            }catch (IOException e){
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses == null || addresses.size() == 0) {
                Toast.makeText(getActivity(), "Local não encontrado.", Toast.LENGTH_SHORT).show();
            } else {
                // limpa os marcadores do mapa
                map.clear();

                for(int i = 0; i < addresses.size(); i++) {
                    Address address = addresses.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    String addressText = String.format("%s, %s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", address.getCountryName());

                    markerOptions.position(latLng);
                    markerOptions.title(addressText);
                    map.addMarker(markerOptions);

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        }
    }
}
