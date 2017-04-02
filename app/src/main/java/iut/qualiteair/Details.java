package iut.qualiteair;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import iut.qualiteair.models.GlobalObject;
import iut.qualiteair.tools.db.LanguagueHelper;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class Details extends AppCompatActivity {

    private JSONObject jsonRespuesta;
    private ColumnChartView chart;
    private ColumnChartData data;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLabels = false;
    private boolean hasLabelForSelected = false;
    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<String> hours = new ArrayList<>();
    private ArrayList<Integer> min = new ArrayList<>();
    private ArrayList<Integer> max= new ArrayList<>();
    private TextView title;
    private TextView minPM10;
    private TextView maxPM10;
    private TextView minNO2;
    private TextView maxNO2;
    private TextView minW;
    private TextView maxW;
    private TextView minH;
    private TextView maxH;
    private TextView last;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        LanguagueHelper.setLanguage(this);
        getSupportActionBar().setHomeButtonEnabled(true);

        id = getIntent().getExtras().getInt("id");

        getDataFromUrl("https://api.waqi.info/api/feed/@"+id+"/obs.fr.json?token=af073d16e3707f6d085660cfcd0137a61b961365");


        title = (TextView) findViewById(R.id.detailTitle);
        minPM10 = (TextView) findViewById(R.id.min10);
        maxPM10 = (TextView) findViewById(R.id.max10);
        minNO2 = (TextView) findViewById(R.id.min2);
        maxNO2 = (TextView) findViewById(R.id.max2);
        minW = (TextView) findViewById(R.id.minw);
        maxW = (TextView) findViewById(R.id.maxw);
        minH = (TextView) findViewById(R.id.minh);
        maxH = (TextView) findViewById(R.id.maxh);
        last = (TextView) findViewById(R.id.updateD);

        chart = (ColumnChartView) findViewById(R.id.chart);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);

        final MenuItem sendItem = menu.findItem(R.id.action_send);
        final MenuItem refreshItem = menu.findItem(R.id.action_refresh);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            return true;
        }
        if (id == R.id.action_refresh) {
            Intent refresh = new Intent(this, Details.class);
            refresh.putExtra("id",this.id);
            startActivity(refresh);
            this.finish();
            return true;
        }
        else if (item.getItemId() == android.R.id.home ) {
            Intent i=new Intent(Details.this, MainActivity.class);
            startActivity(i);
            finish();
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

                        try {
                            jsonRespuesta = new JSONObject(response);

                            JSONObject rxs =jsonRespuesta.getJSONObject("rxs");
                            JSONObject obs=rxs.getJSONArray("obs").getJSONObject(0);
                            JSONObject msg=obs.getJSONObject("msg");

                            JSONObject city=msg.getJSONObject("city");
                            title.setText(city.getString("name"));

                            JSONArray iaqi=msg.getJSONArray("iaqi");
                            if(iaqi!=null){
                                minPM10.setText(iaqi.getJSONObject(0).getJSONArray("v").getInt(1)+ " PM10");
                                maxPM10.setText(iaqi.getJSONObject(0).getJSONArray("v").getInt(2)+" PM10");
                                last.setText(iaqi.getJSONObject(0).getJSONArray("h").getString(0));
                                minNO2.setText(iaqi.getJSONObject(1).getJSONArray("v").getInt(1)+" NO2");
                                maxNO2.setText(iaqi.getJSONObject(1).getJSONArray("v").getInt(2)+" NO2");
                                minW.setText(iaqi.getJSONObject(2).getJSONArray("v").getInt(1)+"°C");
                                maxW.setText(iaqi.getJSONObject(2).getJSONArray("v").getInt(2)+"°C");
                                minH.setText(iaqi.getJSONObject(3).getJSONArray("v").getInt(1)+" %");
                                maxH.setText(iaqi.getJSONObject(3).getJSONArray("v").getInt(2)+" %");
                            }

                            JSONObject forecast=msg.getJSONObject("forecast");
                            JSONArray  aqi=forecast.getJSONArray("aqi");

                            for(int i=0;i<aqi.length();i++){
                                JSONObject data=aqi.getJSONObject(i);
                                JSONArray v= data.getJSONArray("v");

                                Date dt = null;
                                try {

                                    String str = data.getString("t");

                                    String fmt = "yyyy-MM-dd'T'HH:mm:ss";
                                    DateFormat df = new SimpleDateFormat(fmt);

                                    dt = df.parse(str);

                                    DateFormat tdf = new SimpleDateFormat("HH:mm");
                                    DateFormat dfmt  = new SimpleDateFormat("dd/MM/yyyy");

                                    String timeOnly = tdf.format(dt);
                                    String dateOnly = dfmt.format(dt);

                                    dates.add(dateOnly );
                                    hours.add(timeOnly);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }//end catch


                                for (int j=0;j<v.length();j++){
                                    min.add(v.getInt(0));
                                    max.add(v.getInt(1));
                                }

                            }

                            generateDefaultData();

                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void generateDefaultData() {
        Log.d("generateDefaultData:", "AQUI ESTOY "+ min.size());

        int numColumns = min.size();
        if(min.size()>10)
            numColumns = 10;

        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
            values.add(new SubcolumnValue(min.get(i),ChartUtils.pickColor()));
            values.add(new SubcolumnValue(max.get(i),ChartUtils.pickColor()));


            Column column = new Column(values);
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        if (hasAxes) {

            List<AxisValue> list = new ArrayList<AxisValue>();

            for(int i = 0; i< numColumns; ++i){
                AxisValue value = new AxisValue(i);
                value.setLabel(String.valueOf(hours.get(i)));
                list.add(value);
            }

            Axis axisX = new Axis(list);

            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {

                axisX.setName(getResources().getString(R.string.forecast)+ ": "+dates.get(0)+" - "+dates.get(10));
                axisY.setName("AQI "+ getResources().getString(R.string.forecast));
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        chart.setColumnChartData(data);

    }


}
