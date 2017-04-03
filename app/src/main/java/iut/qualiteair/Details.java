package iut.qualiteair;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    //Components
    private JSONObject jsonRespuesta;
    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<String> hours = new ArrayList<>();
    private ArrayList<Integer> min = new ArrayList<>();
    private ArrayList<Integer> max= new ArrayList<>();
    //Components for create chart
    private ColumnChartView chart;
    private ColumnChartData data;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLabels = false;
    private boolean hasLabelForSelected = false;
    // UI elements
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
    private String urlCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Set up the language configuration
        LanguagueHelper.setLanguage(this);
        //Adding Back button
        getSupportActionBar().setHomeButtonEnabled(true);

        //Getting information sended by the Intent
        id = getIntent().getExtras().getInt("id");

        //Initializing items of content_details
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

        //Calling information of the Method
        getDataFromUrl("https://api.waqi.info/api/feed/@"+id+"/obs.fr.json?token=af073d16e3707f6d085660cfcd0137a61b961365");

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
            sendEmail();
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

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //Convert the response to a JSONObject
                            jsonRespuesta = new JSONObject(response);

                            //Get Objects of the json
                            JSONObject rxs =jsonRespuesta.getJSONObject("rxs");
                            JSONObject obs=rxs.getJSONArray("obs").getJSONObject(0);
                            JSONObject msg=obs.getJSONObject("msg");

                            JSONObject city=msg.getJSONObject("city");
                            title.setText(city.getString("name"));
                            urlCity = city.getString("url");

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

                                //Get simple date and simple time of a xs:date:time
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

                            //Call Method for generate the chart
                            generateDefaultData();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void generateDefaultData() {

        //Define the number of colums by array of min values
        int numColumns = min.size();
        //I just will take 10 values if the array if so big
        if(min.size()>10)
            numColumns = 10;

        // I use 2 subcolumn in each of one column, one for min values, other for max values.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
            values.add(new SubcolumnValue(min.get(i),ChartUtils.pickColor()));
            values.add(new SubcolumnValue(max.get(i),ChartUtils.pickColor()));

            //Add the values to the column
            Column column = new Column(values);
            //Add Labels
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);
        }
        //Set the information of the chart
        data = new ColumnChartData(columns);

        if (hasAxes) {

            List<AxisValue> list = new ArrayList<AxisValue>();
            //Create a array of labels of hours values
            for(int i = 0; i< numColumns; ++i){
                AxisValue value = new AxisValue(i);
                value.setLabel(String.valueOf(hours.get(i)));
                list.add(value);
            }
            //Set labels in x with array
            Axis axisX = new Axis(list);
            //Set labels in Y by default
            Axis axisY = new Axis().setHasLines(true);

            if (hasAxesNames) {
                //Set title in X with  start date and finish date
                axisX.setName(getResources().getString(R.string.forecast)+ ": "+dates.get(0)+" - "+dates.get(10));
                //Set title of  Y
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

    private void sendEmail(){

        LayoutInflater inflater = getLayoutInflater();

        View dialoglayout = inflater.inflate(R.layout.alert_dialog, null);
        final EditText Vemail = (EditText) dialoglayout.findViewById(R.id.email);
        final EditText Vsubject = (EditText) dialoglayout.findViewById(R.id.subject);

        Button btnEnviarMail = (Button) dialoglayout.findViewById(R.id.btn_send);
        if(!Vemail.getText().toString().trim().equals(""))
            btnEnviarMail.setEnabled(true);


        btnEnviarMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String subject = Vsubject.getText().toString();
                String email = Vemail.getText().toString();

                String body="Hi, \nOne of your friends if worried about you and he/she wants you kwon the situation of "+title.getText()+".\n"+
                        "\nThis is a little review:\n"
                        +getResources().getString(R.string.particulas).toString()+"\nMIN: "+minPM10.getText().toString()+"  MAX: "+maxPM10.getText().toString()+
                        "\n"+getResources().getString(R.string.sulfur).toString()+"\nMIN: "+ minNO2.getText().toString()+"  MAX: "+ maxNO2.getText().toString()+"\n"+
                        getResources().getString(R.string.weather).toString()+"\nMIN: "+minW.getText().toString()+" MAX: "+maxW.getText().toString()+
                        "\n"+getResources().getString(R.string.humidity).toString()+"\nMIN: "+minH.getText().toString()+"%  MAX: "+maxH.getText().toString()
                        +"\n \n \nGet more information here: "+urlCity;

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + email));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    //Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(Details.this);
        builder.setView(dialoglayout);
        builder.show();
    }


}
