package iut.qualiteair;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import iut.qualiteair.models.GlobalObject;
import iut.qualiteair.tools.MyAdapter;
import iut.qualiteair.tools.db.DatabaseHandler;



public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    public static DatabaseHandler database;
    private ArrayList<String> myDataset = new ArrayList<>();
    private ArrayList<GlobalObject> initialCities = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, Search.class);
                startActivity(i);
                finish();
            }
        });


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(this, initialCities);
        mRecyclerView.setAdapter(mAdapter);

        database = new DatabaseHandler(this);
        database.open();
        List<GlobalObject> tmpCities = database.getAllObj();
        database.close();

        /*for (int i = 0; i < tmpCities.size(); i++) {
            myDataset.add("@" + tmpCities.get(i).getRxs().getObs().get(0).getMsg().getCity().getIdx());
        }*/
        myDataset.add("@3068");
        myDataset.add("@404");
        myDataset.add("@5026");
        myDataset.add("@3069");


        for (int i = 0; i < myDataset.size(); i++) {
            Log.d("MYDATASET:", myDataset.get(i) + " en position" + i);
            getDataFromUrl( "https://api.waqi.info/api/feed/" + myDataset.get(i) + "/obs.fr.json?token=af073d16e3707f6d085660cfcd0137a61b961365");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (id == R.id.action_settings) {
                Intent i = new Intent(MainActivity.this, Settings.class);
                startActivity(i);
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDataFromUrl(String url) {
        Log.d("URL REQUEST:", url );
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        GlobalObject obj = gson.fromJson(response, GlobalObject.class);
                        if(obj==null)
                            Log.d("ERROR VOLLEY:", "ESTOY VACIO POR DENTRO!!");
                        else if(obj.getRxs().getObs().get(0).getMsg()==null){

                            Log.d("ERROR VOLLEY:", "MSG ESTA VACIO!!!!!");
                            Intent i= new Intent(MainActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                        else if(obj.getRxs().getObs().get(0).getMsg().equals("404")){
                            Log.d("ERROR VOLLEY:", "MSG ESTA VACIO!!!!!");
                        }
                        else {
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

}
