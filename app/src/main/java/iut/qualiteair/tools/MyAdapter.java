package iut.qualiteair.tools;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import iut.qualiteair.DangerNotification;
import iut.qualiteair.Details;
import iut.qualiteair.MainActivity;
import iut.qualiteair.R;
import iut.qualiteair.models.GlobalObject;
import iut.qualiteair.models.MessageObject;


/**
 * Created by amanda on 13/02/2017.
 * Recicler view for Activity Main
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>  {

    private ArrayList<GlobalObject> mDataset;
    private Context mCtx;
    public ArrayList<String> cityName= new ArrayList<>();
    public ArrayList<Integer> cityid= new ArrayList<>();
    public static int position;
    public Resources re;


    public class ViewHolder  extends RecyclerView.ViewHolder {

        // UI elements
        public TextView global;
        public TextView gps;
        public TextView max;
        public TextView min;
        public TextView lastUpdate;
        public TextView level;
        public TableLayout table;


        public ViewHolder(View v) {
            super(v);

            //Initializing items
            global = (TextView) v.findViewById(R.id.globalinfo);
            gps = (TextView) v.findViewById(R.id.gps);
            max = (TextView) v.findViewById(R.id.pm10Max);
            min = (TextView) v.findViewById(R.id.pm10Min);
            lastUpdate = (TextView) v.findViewById(R.id.lastUpdate);
            level = (TextView) v.findViewById(R.id.level);
            table =(TableLayout) v.findViewById(R.id.row1);


            //Adding a action when user press in a row
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Start Dtails Activity
                    Intent intent = new Intent(mCtx, Details.class);
                    intent.putExtra("id",cityid.get(getAdapterPosition()));
                    mCtx.startActivity(intent);

                }
            });
            //Adding a action when user make a long press in a row
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    //Call method showPopup
                    showPopup(v, getAdapterPosition());
                    return false;
                }
            });
        }


    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context ctx, ArrayList<GlobalObject> initialCities) {
        mCtx = ctx;
        mDataset = initialCities;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_main, parent, false);
        re=v.getResources();
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, final int position) {

        //create a msg object with the information of the models
        MessageObject msg = mDataset.get(position).getRxs().getObs().get(0).getMsg();
        //create a list of names and ids
        cityName.add(msg.getCity().getName());
        cityid.add(msg.getCity().getIdx());

        //Change text in the UI
        for(int i=0;i<msg.getIaqi().size();i++) {
            if(msg.getIaqi().get(i).getP().contains("pm10")) {
                holder.max.setText(msg.getIaqi().get(i).getV().get(2).toString());
                holder.min.setText(msg.getIaqi().get(i).getV().get(1).toString());
                if(msg.getIaqi().get(i).getV().get(0)<50) {
                    holder.table.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.green));
                    holder.level.setText(re.getString(R.string.level)+" "+msg.getIaqi().get(i).getV().get(0).toString() +re.getString(R.string.good));
                } else if(msg.getIaqi().get(i).getV().get(0)<100) {
                    holder.table.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.orange));
                    holder.level.setText(re.getString(R.string.level)+" "+msg.getIaqi().get(i).getV().get(0).toString() +re.getString(R.string.mod));
                    //Call a notification if the nivel of pollution is more than 100
                    notification(msg.getCity().getName());
                } else {
                    holder.table.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.red));
                    holder.level.setText(re.getString(R.string.level)+" "+msg.getIaqi().get(i).getV().get(0).toString() +re.getString(R.string.bad));
                }
            }
            if(msg.getIaqi().get(i).getP().contains("t")) {
                String city = msg.getCity().getName();
                if(msg.getCity().getName().length()>20) {
                    city = msg.getCity().getName().substring(0,20)+"...";
                }
                holder.global.setText(city+" "+msg.getIaqi().get(i).getV().get(0)+"°C");
            }
        }

        holder.lastUpdate.setText(re.getString(R.string.update)+" "+msg.getIaqi().get(0).getH().get(0).toString());
        holder.gps.setText("Lat: "+msg.getCity().getGeo()[0].substring(0,6)+" pm10"+"      Lng: "+msg.getCity().getGeo()[1].substring(0,6)+" pm10");


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * Create a pop menu w
     * */
    public void showPopup(final View v, int pos) {

        PopupMenu popup = new PopupMenu(mCtx, v);
        popup.inflate(R.menu.menu_select);
        position=pos;

        //If one of the items is pressed
        //In this case we just use it for show of options delete
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.one:{
                        //Get information of the position
                        String name=cityName.get(position);
                        int id= cityid.get(position);
                        //Remove the object of the database
                        MainActivity.database.open();
                        MainActivity.database.removeObj(id);
                        //Show a toas
                        Toast.makeText(v.getContext() , re.getString(R.string.delete)+" "+name+" "+re.getString(R.string.d_cities), Toast.LENGTH_SHORT).show();
                        //Remove the infomation of the position
                        cityid.remove(position);
                        cityName.remove(position);
                        //Remove the information of the dataset
                        mDataset.remove(position);
                        //Notify the change
                        notifyItemRemoved(position);

                    }
                    return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }


    public void notification(String name){

        DangerNotification.notify(mCtx,name, 1);

    }



}
