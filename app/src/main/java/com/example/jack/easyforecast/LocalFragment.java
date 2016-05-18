package com.example.jack.easyforecast;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LocalFragment extends Fragment implements LocationListener{
    private static final String TAG = "Localizzazione";
    private String url = "https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/";
    private OnFragmentInteractionListener mListener;
    private  View view;
    private TextView txt;
    private ImageView img;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private  Location location = null;



    //instance methods
    public LocalFragment() { }
    public static LocalFragment newInstance() {
        return new LocalFragment();
    }

    //APIKEY 8c83705e47a2b9dd696aa361d375924b
    //from Fahrenheit to Celsius: first subtract 32, then multiply by 100/180
    //https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE
    //https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/37.8267,-122.423
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_local, container, false);
        txt = (TextView) view.findViewById(R.id.locationText);
        img = (ImageView)view.findViewById(R.id.imageView);
        //localizzo l'utente
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

        //end



        return view;
    }

    @Override
    public void onLocationChanged(Location locationC) {
        location = locationC;
        latitude = locationC.getLatitude();
        longitude = locationC.getLongitude();
        txt.setText("Lat"+latitude+"long"+longitude);
        Log.d(TAG, location.toString());
        url = url+location.getLatitude()+","+location.getLongitude();
        Log.d("URL:", url);
        requestForcast(location);
        requestCityName(location);
        locationManager.removeUpdates(this);
    }

    //richiesta NomeCitta in cui ci troviamo
    public void requestCityName(Location location){
        try {

            Geocoder geo = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                txt.setText("Waiting for Location");
            }
            else {
                if (addresses.size() > 0) {
                    txt.setText(addresses.get(0).getLocality()+"-"+addresses.get(0).getCountryName());
                    //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }


    //richiesta Meteo
    public void requestForcast(Location location){
        //update request url
        url = url+location.getLatitude()+","+location.getLongitude();
        //end
        img.setBackgroundResource(R.drawable.cloudy);
        //start request
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
        asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
            @Override //managing response
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Boolean success;
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));


                    if ((success = jsonObject.getBoolean("success"))) {
                        //TODO: Parsing JSON OBJ

                    } else {
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Error on jsonObject", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity(), "Error on connection", Toast.LENGTH_LONG).show();

            }
        });
    }













    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.d("OnPause", " errore");
        }

    }









    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }
}
