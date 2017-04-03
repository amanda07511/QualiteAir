package iut.qualiteair;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import iut.qualiteair.models.GlobalObject;
import iut.qualiteair.tools.MyAdapter;
import iut.qualiteair.tools.db.DatabaseHandler;
import iut.qualiteair.tools.db.LanguagueHelper;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Components
    private MyAdapter mAdapter;
    public static DatabaseHandler database;
    private ArrayList<String> myDataset = new ArrayList<>();
    private ArrayList<GlobalObject> initialCities = new ArrayList<>();
    private Location mLastLocation;
    private JSONObject jsonRespuesta;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    // UI elements
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TextView lblLocation;
    private TextView lblTemperature;
    private TextView lblPM10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Button wich start Search Activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Search.class);
                startActivity(i);
                finish();
            }
        });

        //Initializing items of content_main
        lblLocation = (TextView) findViewById(R.id.lblocation);
        lblTemperature = (TextView) findViewById(R.id.lblT);
        lblPM10=(TextView) findViewById(R.id.lblPM);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        //Setting configurations
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Initializing adapter
        mAdapter = new MyAdapter(this, initialCities);
        mRecyclerView.setAdapter(mAdapter);

        //Getting kept data of the database
        database = new DatabaseHandler(this);
        database.open();
        List<GlobalObject> tmpCities = database.getAllObj();
        database.close();

        //Adding  idx got of the database
        for (int i = 0; i < tmpCities.size(); i++) {
            myDataset.add("@" + tmpCities.get(i).getRxs().getObs().get(0).getMsg().getCity().getIdx());
        }

        //Calling information of the Method
        for (int i = 0; i < myDataset.size(); i++) {
            Log.d("MYDATASET:", myDataset.get(i) + " en position" + i);
            getDataFromUrl("https://api.waqi.info/api/feed/" + myDataset.get(i) + "/obs.fr.json?token=af073d16e3707f6d085660cfcd0137a61b961365");
        }

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
        displayLocation();
        //getDataLocation();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Start Setting Activity
        if (id == R.id.action_settings) {
            if (id == R.id.action_settings) {
                Intent i = new Intent(MainActivity.this, Settings.class);
                startActivity(i);
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for get data of  the api by an ID
     * */
    private void getDataFromUrl(String url) {
        Log.d("URL REQUEST:", url);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        GlobalObject obj = gson.fromJson(response, GlobalObject.class);
                        if (obj == null)
                            Log.d("ERROR VOLLEY:", "ESTOY VACIO POR DENTRO!!");
                        else if (obj.getRxs().getObs().get(0).getMsg() == null) {

                            Log.d("ERROR VOLLEY:", "MSG ESTA VACIO!!!!!");
                            Intent i = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(i);
                        } else if (obj.getRxs().getObs().get(0).getMsg().equals("404")) {
                            Log.d("ERROR VOLLEY:", "MSG ESTA VACIO!!!!!");
                        } else {
                            initialCities.add(obj);
                            mAdapter.notifyDataSetChanged();
                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }


    /**
     * Method for get data of  the api by latitude and longitude
     * */
    private void getDataLocation(Double lat, Double lng) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        String url= "https://api.waqi.info/feed/geo:"+lat+";"+lng+"/?token=af073d16e3707f6d085660cfcd0137a61b961365";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url ,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //Convert the response to a JSONObject
                            jsonRespuesta = new JSONObject(response);
                            //Get Objects of the json
                            JSONObject city= jsonRespuesta.getJSONObject("data").getJSONObject("city");
                            JSONObject iaqi= jsonRespuesta.getJSONObject("data").getJSONObject("iaqi");
                            //Set UI elements
                            lblLocation.setText(city.getString("name"));
                            lblTemperature.setText(iaqi.getJSONObject("t").getDouble("v")+"°C");
                            lblPM10.setText(iaqi.getJSONObject("pm10").getDouble("v")+"PM10");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("GETTING LOCATION", response);

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

    /**
     * Method for get the location
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            getDataLocation(latitude,longitude);

        } else {

            lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("ERRO", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}
