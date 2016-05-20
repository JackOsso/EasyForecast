package com.example.jack.easyforecast;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

import com.loopj.android.http.*;

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
    private static final String HEADER = "x-bitrace-session";
    private static String url = "https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/";
    private OnFragmentInteractionListener mListener;
    private View view;
    private TextView txt;
    private TextView status;
    private TextView temp;
    private String icon;
    private ImageView img;
    private String response;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private double temperature;
    private  Location location=null;
    private JSONObject jsonObject;




    //instance methods
    public LocalFragment() { }
    public static LocalFragment getInstance() {
        return new LocalFragment();
    }

    //APIKEY 8c83705e47a2b9dd696aa361d375924b
    //from Fahrenheit to Celsius: first subtract 32, then multiply by 100/180
    //https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE
    //https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/37.8267,-122.423
    //icons: clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
    //--------
    ////api.openweathermap.org/data/2.5/weather?q=pordenone&APPID=3785f98c8a216d5047f935ad5c4cd02a
    //TODO: LayoutFragmentDecente
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_local, container, false);
        txt = (TextView) view.findViewById(R.id.locationText);
        temp =(TextView) view.findViewById(R.id.temp);
        status =(TextView) view.findViewById(R.id.status);
        img = (ImageView)view.findViewById(R.id.imageView);
        txt.setText("");
        //localizzo l'utente
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);
        //requestForcast(location);
        return view;
    }

    @Override
    public void onLocationChanged(Location locationC) {
        location = locationC;
        latitude = locationC.getLatitude();
        longitude = locationC.getLongitude();
        requestCityName();
        Log.d(TAG, location.toString());
        Log.d("URL:", url+latitude+","+longitude);
        locationManager.removeUpdates(this);
    }

    //richiesta NomeCitta in cui ci troviamo
    public void requestCityName(){
        try {
            Geocoder geo = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                txt.setText("Waiting for Location");
            }
            else {
                if (addresses.size() > 0) {
                    //add to get country +"-"+addresses.get(0).getCountryName()
                    txt.setText(addresses.get(0).getLocality());
                    String cityname =addresses.get(0).getLocality();
                    requestForcast(cityname);

                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }


    //richiesta Meteo
    public void requestForcast(String nomecitta){
        //img.setBackgroundResource(R.drawable.cloudy);
        //start request
        nomecitta = nomecitta.replace(" ","");
        AsyncHttpClient client = new AsyncHttpClient();
        Log.d("NOMEcitta",nomecitta.toLowerCase());
        //https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/37.8267,-122.423
        //http://api.openweathermap.org/data/2.5/weather?q="+nomecitta.toLowerCase()+"&APPID=3785f98c8a216d5047f935ad5c4cd02a
        client.get("https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/"+latitude+","+longitude , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("URL:","https://api.forecast.io/forecast/8c83705e47a2b9dd696aa361d375924b/"+latitude+","+longitude);
                try {
                    jsonObject = new JSONObject(new String(responseBody));
                    Log.d("OnSuccess","gotJSON");
                } catch (JSONException e) {
                    Log.d("OnSuccess","didn't gotJSON");
                    e.printStackTrace();
                }
                try {
                        response = jsonObject.getJSONObject("currently").toString();
                        Log.d("OnSuccess","gotCurrently");
                        jsonObject = new JSONObject(response);
                        try {
                            //first subtract 32, then multiply by 100/180
                            //temperature start --------------

                            temperature=jsonObject.getDouble("temperature");
                            Log.d("Temp", ""+temperature);
                            temperature=(temperature-32)*100/180;
                            temperature=Math.ceil(temperature);
                            int tempInt=(int)temperature;
                            //temperature=temperature-273;
                            temp.setText(""+tempInt+"°");
                            Log.d("OnSuccess","gotTemperature");
                            //temperature end -----------------
                            //Start Adding drawable
                            //img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.clearday));
                            //start status ---
                            status.setText(jsonObject.getString("summary"));
                            //end status ---
                            //start addicons---
                            icon = jsonObject.getString("summary").replace("-","").replace(" ","").toLowerCase();
                            Log.d("IMG",icon);
                            switch (icon) {
                                case "clearday" :
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.clearday));
                                    break;
                                case "clearnight":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.clearnight));
                                    break;
                                case "cloudy":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cloudy));
                                    break;
                                case "fog" :
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.fog));
                                    break;
                                case "partlycloudy":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.partlycloudyday));
                                    break;
                                case "partlycloudynight":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.partlycloudynight));
                                    break;
                                case "rain":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.rain));
                                    break;
                                case "snow":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.snow));
                                    break;
                                case "wind":
                                    img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.wind));
                                    break;
                            }
                            //img.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.clearday));
                            //end icons---



                        } catch (JSONException e) {
                            Log.d("OnSuccess","didn't got Temperature");
                            e.printStackTrace();
                        }
                } catch (JSONException e) {
                    Log.d("OnSuccess","didn't got Currently");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), "Connessione fallita", Toast.LENGTH_SHORT).show();
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
