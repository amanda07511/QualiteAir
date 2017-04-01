package iut.qualiteair;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;

import iut.qualiteair.models.ResultObject;
import iut.qualiteair.models.Results;
import iut.qualiteair.tools.MyAdapter2;
import iut.qualiteair.tools.db.DatabaseHandler;

public class Search extends AppCompatActivity {


    public static DatabaseHandler db;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter2 mAdapter;
    private ArrayList<ResultObject> cities= new ArrayList<>();
    private String url="https://api.waqi.info/search/?token=af073d16e3707f6d085660cfcd0137a61b961365&keyword=";
    private String cityName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setHomeButtonEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view2);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter2(this, cities);
        mRecyclerView.setAdapter(mAdapter);

        db=new DatabaseHandler(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }
        else if (item.getItemId() == android.R.id.home ) {
            Intent i=new Intent(Search.this, MainActivity.class);
            startActivity(i);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setQueryHint(getResources().getString(R.string.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                cityName=query;
                getDataFromUrl(url+cityName);
                searchView.setQuery("", false);
                searchView.setIconified(true);
                cities.removeAll(cities);
                mAdapter = new MyAdapter2(Search.this, cities);
                mRecyclerView.setAdapter(mAdapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cityName=newText;
                return true;
            }
        });


        return true;
    }

    private void getDataFromUrl(String url) {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.print(response);
                        Gson gson = new Gson();
                        Results obj = gson.fromJson(response, Results.class);
                        if(obj.getData().size()==0)
                            message();
                        else {
                            for (int i=0; i<obj.getData().size();i++){
                                cities.add(obj.getData().get(i));
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    public void message(){
        Toast.makeText(this, getResources().getString(R.string.result), Toast.LENGTH_SHORT).show();
    }
}
