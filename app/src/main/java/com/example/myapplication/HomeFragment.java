package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutionException;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link HomeFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static boolean hasModalShown = false;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String linkUrl = "";
    String linkHomeInfo = "";
    MaterialSwitch checkSwitch, checkEngine;
    TextView batteryLife, batteryLifeTxt, warningTxt;
    String setParking = null, setEngine = null, deviceId = null;
    int iParking, iEngine;
    boolean isConnectedToArduino = false, local = false;
    Button overrideBtn, temp;
    ImageButton parkHelpBtn, engineHelpBtn, battOverrideHelpBtn;
    public HomeFragment() {
        // Required empty public constructor
    }


//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment HomeFragment.
//     */
    // TODO: Rename and change types and number of parameters
//    public static HomeFragment newInstance(String param1, String param2) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        checkSwitch = (MaterialSwitch) view.findViewById(R.id.ParkingModeSwitch);
        checkEngine = (MaterialSwitch) view.findViewById(R.id.EngineImmobilizerSwitch);
        batteryLife = (TextView) view.findViewById(R.id.batteryLife);
        batteryLifeTxt = (TextView) view.findViewById(R.id.EngineImmobilizerText);
        overrideBtn = (Button) view.findViewById(R.id.overrideBtn);
        warningTxt = (TextView) view.findViewById(R.id.warningTxt);
        parkHelpBtn = (ImageButton) view.findViewById(R.id.parkHelpBtn);
        engineHelpBtn = view.findViewById(R.id.engineHelpBtn);
        battOverrideHelpBtn = view.findViewById(R.id.batteryHelpBtn);

        ConnectionHelper connectionHelper = new ConnectionHelper(getContext());
        boolean local = connectionHelper.pingNetwork("192.168.4.1");
        SharedPreferences sharedPref = getContext().getSharedPreferences("override",Context.MODE_PRIVATE);
        int pending = sharedPref.getInt("pending", -1);
        if(pending == 1){
            warningTxt.setVisibility(View.VISIBLE);
        }else{
            warningTxt.setVisibility(View.GONE);
        }
        if(local) {
            sharedPref = getContext().getSharedPreferences("override",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("pending", 1);
            editor.apply();
            isConnectedToArduino = true;
            batteryLifeTxt.setText("Override");
            overrideBtn.setVisibility(View.VISIBLE);
            sharedPref = getContext().getSharedPreferences("options",Context.MODE_PRIVATE);
            iParking = sharedPref.getInt("iParking", -1);
            iEngine = sharedPref.getInt("iEngine", -1);
            Log.d("Override iParking", iParking+"");
            Log.d("Override iEngine", iEngine+"");
            if(iParking != -1 && iEngine != -1){
                checkSwitch.setChecked(iParking == 1);
                checkEngine.setChecked(iEngine == 1);
            }

            TooltipCompat.setTooltipText(battOverrideHelpBtn, "Click the button after controlling the switch button \nfor the system to work.");

            overrideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SharedPreferences sharedPref = getContext().getSharedPreferences("override",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.remove("pending").apply();
                        if(iParking != -1 && iEngine != -1){
                            checkSwitch.setChecked(iParking == 1);
                            checkEngine.setChecked(iEngine == 1);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("This action will set the last setting. ");

                            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Call a method to override the values from the database
                                    try {
                                        Object data = new OverrideHttp().execute().get();
                                    } catch (ExecutionException e) {
                                        throw new RuntimeException(e);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    Intent intent = new Intent(getContext(), LoginPage.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Call a method to override the values from the database
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        }else {
            if(connectionHelper.haveNetworkConnection()){
                new HttpRequestTask().execute();

            }else{
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parkHelpBtn.setTooltipText("Hello");
        }
        TooltipCompat.setTooltipText(parkHelpBtn, "1. If \"On\" it will detect intruders.\n2. If \"Off\", it will detect collision");
        TooltipCompat.setTooltipText(engineHelpBtn, "1. If \"On\" it will turn off the engine");
        TooltipCompat.setTooltipText(battOverrideHelpBtn, "Battery Status of the system");

//        Info info = new Info();
//        info.execute(deviceId);


        checkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    setParking = "1";
                } else {
                    setParking = "0";
                }
                String toWhere = (local)?"override":"options";
                SharedPreferences sharedPref = getContext().getSharedPreferences(toWhere,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("iParking", Integer.parseInt(setParking));
                editor.apply();
                SetParking setPark = new SetParking();
                try {
                    Object res = setPark.execute(setParking).get();
                    Log.d("Parking Setting",res.toString());
                }catch (Exception e) {
                }
            }
        });

        checkEngine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    setEngine = "1";
                } else {
                    setEngine = "0";
                }
                String toWhere = (local)?"override":"options";
                SharedPreferences sharedPref = getContext().getSharedPreferences(toWhere,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("iEngine", Integer.parseInt(setEngine));
                editor.apply();
                SetEngine setEngineImmobilizer = new SetEngine();
                try {
                    Object res = setEngineImmobilizer.execute(setEngine).get();
                    Log.d("Engine Setting",res.toString());
                }catch (Exception e) {
                }
            }
        });
        return view;
    }


    public class SetParking extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                String parkingMode = objects[0].toString();
                if(isConnectedToArduino){
                    linkUrl = "http://192.168.4.1/setStatus?cmd=park="+((parkingMode=="1")?"on":"off");;
                }else {
                    linkUrl = "http://api.imbento.com/others/ctu2023_motorcycle_anti_theft/db.php?action=setParking&status=" + parkingMode + "&did=1";
                }
                URL url = new URL(linkUrl);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(linkUrl));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
            } catch (Exception e){
                return new String(e.getMessage());
            }
        }
    }

    public class SetEngine extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                String engine = objects[0].toString();
                if(isConnectedToArduino){
                    linkUrl = "http://192.168.4.1/setStatus?cmd=engine="+((engine=="1")?"on":"off");
                }else {
                    linkUrl = "http://api.imbento.com/others/ctu2023_motorcycle_anti_theft/db.php?action=setEngine&status=" + engine + "&did=1";
                }
                URL url = new URL(linkUrl);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(linkUrl));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
            } catch (Exception e){
                return new String(e.getMessage());
            }
        }

    }

    public class OverrideHttp extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                if(isConnectedToArduino){
                    linkUrl = "http://192.168.4.1/setStatus?cmd=override=off";
                }
                URL url = new URL(linkUrl);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(linkUrl));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
            } catch (Exception e){
                return new String(e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

        }
    }

    public class Info extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                String id = objects[0].toString();
                linkHomeInfo = "http://api.imbento.com/others/ctu2023_motorcycle_anti_theft/db.php?action=getDevice&did="+id;
                URL url = new URL(linkUrl);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(linkUrl));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
            } catch (Exception e){
                return new String(e.getMessage());
            }
        }
    }


    class HttpRequestTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {

                // Create a URL object with the endpoint you want to make the request to
                URL url = new URL("http://api.imbento.com/others/ctu2023_motorcycle_anti_theft/db.php?action=getDevice&did=1");

                // Create an HttpURLConnection object
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method (GET, POST, etc.)
                connection.setRequestMethod("GET");

                // Get the response code
                int responseCode = connection.getResponseCode();

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Disconnect the connection
                connection.disconnect();

                // Return the response data
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseData) {
            super.onPostExecute(responseData);

            if (responseData != null) {
                Log.d("HTTP Response", responseData);
                // Process the response data as needed
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseData);
                    JSONObject deviceJson = responseJson.getJSONObject("device");
                    String iBattery = deviceJson.getString("iBattery");
                    String iEngineStr = deviceJson.getString("iEngine");
                    String iParkingStr = deviceJson.getString("iParking");
                    iEngine = Integer.parseInt(iEngineStr);
                    iParking = Integer.parseInt(iParkingStr);
                    checkSwitch.setChecked(iParking == 1);
                    checkEngine.setChecked(iEngine == 1);
                    SharedPreferences sharedPref = getContext().getSharedPreferences("options",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("iParking", iParking);
                    editor.apply();

                    editor.putInt("iEngine", iEngine);
                    editor.apply();
                    try {
                        int bat = Integer.parseInt(iBattery);
                        if(bat <= 20){
                            batteryLife.setTextColor(Color.RED);
                        }else{
                            batteryLife.setTextColor(Color.GREEN);
                        }
                    }catch(Exception e){

                    }
                    Log.d("IBATTERY",iBattery);
                    batteryLife.setText(iBattery+"%");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            } else {
                // Handle the case when the request fails
            }
        }
    }
}

