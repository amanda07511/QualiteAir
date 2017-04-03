package iut.qualiteair;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import iut.qualiteair.tools.db.LanguagueHelper;

public class Settings extends AppCompatActivity {

    private String lan;
    private SharedPreferences.Editor editor;
    private Button button;
    private RadioButton selected;
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        //Call the preferences stoked in LanguageHelper
        LanguagueHelper.setLanguage(this);
        //Adding Back button
        getSupportActionBar().setHomeButtonEnabled(true);
        //Create a variable wich recive Prefereces of system
        SharedPreferences pref = this.getSharedPreferences("Mypfre", MODE_PRIVATE);
        //Instance for edit the preferences
        editor = pref.edit();

        //Get last status of the radion button
        if (pref.getString("lang_code", "en").equals("en")) {
            selected = (RadioButton) findViewById(R.id.radioButton_en);
            selected.setChecked(true);
        } else if (pref.getString("lang_code", "en").equals("fr")) {
            selected = (RadioButton) findViewById(R.id.radioButton_fr);
            selected.setChecked(true);
        } else if (pref.getString("lang_code", "en").equals("es")) {
            selected = (RadioButton) findViewById(R.id.radioButton_es);
            selected.setChecked(true);
        }

        //Saving the changes in language configuration
        button = (Button) findViewById(R.id.b_save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Save the prefrence
                LanguagueHelper.setLanguage(Settings.this);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_title));
                //Reset the activity for see the changes
                Intent intent = new Intent(Settings.this, Settings.class);
                startActivity(intent);
                finish();
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton rb = (RadioButton) findViewById(checkedId);
                // checkedId which RadioButton selected
                switch (checkedId) {
                    case R.id.radioButton_en:
                        if (rb.isChecked()) {
                            //Set prefrerences to english
                            editor.putString("lang_code", "en");
                            editor.commit();
                            Toast.makeText(getApplicationContext(), "You change to " + rb.getText(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.radioButton_fr:
                        if (rb.isChecked()) {
                            //Set prefrerences to french
                            editor.putString("lang_code", "fr");
                            editor.commit();
                            Toast.makeText(getApplicationContext(), "Vouz avez changé a " + rb.getText(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.radioButton_es:
                        if (rb.isChecked()) {
                            //Set prefrerences to spanish
                            editor.putString("lang_code", "es");
                            editor.commit();
                            Toast.makeText(getApplicationContext(), "Usted cambio a " + rb.getText(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });


        mySwitch = (Switch) findViewById(R.id.switchStatus);
        //Get last status switch
        if(pref.getBoolean("mode", false ) == false){
            mySwitch.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else{
            mySwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    //If is checked, mode night will be activated
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(getApplicationContext(),"Modo nocturno activado", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("mode", true);
                    editor.commit();
                    //Reset the activity to see the changes
                    Intent i= new Intent(Settings.this, Settings.class);
                    startActivity(i);
                    finish();

                }
                else{
                    //If is not checked, mode night will be desactivated
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("mode", false);
                    editor.commit();
                    Toast.makeText(getApplicationContext(),"Modo nocturno desactivado", Toast.LENGTH_SHORT).show();
                    //Reset the activity to see the changes
                    Intent i= new Intent(Settings.this, Settings.class);
                    startActivity(i);
                    finish();
                }

            }
        });


    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(Settings.this, MainActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

}
