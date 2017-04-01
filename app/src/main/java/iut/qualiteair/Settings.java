package iut.qualiteair;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import iut.qualiteair.tools.db.LanguagueHelper;

public class Settings extends AppCompatActivity {

    private String lan;
    private SharedPreferences.Editor editor;
    private Button button;
    private RadioButton selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        LanguagueHelper.setLanguage(this);
        getSupportActionBar().setHomeButtonEnabled(true);

        SharedPreferences pref = this.getSharedPreferences("Mypfre", MODE_PRIVATE);
        editor= pref.edit();

        if(pref.getString("lang_code","en").equals("en")){
            selected= (RadioButton) findViewById(R.id.radioButton_en);
            selected.setChecked(true);
        }
        else if (pref.getString("lang_code","en").equals("fr")){
            selected= (RadioButton) findViewById(R.id.radioButton_fr);
            selected.setChecked(true);
        }
        else if(pref.getString("lang_code","en").equals("es")){
            selected= (RadioButton) findViewById(R.id.radioButton_es);
            selected.setChecked(true);
        }


        button =(Button) findViewById(R.id.b_save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LanguagueHelper.setLanguage(Settings.this);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_title));
                Intent intent=new Intent(Settings.this, Settings.class);
                startActivity(intent);
                finish();
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb=(RadioButton)findViewById(checkedId);

                switch (checkedId) {
                    case R.id.radioButton_en:
                        if (rb.isChecked()) {
                            editor.putString("lang_code", "en");
                            editor.commit();
                            Toast.makeText(getApplicationContext(),"You change to "+ rb.getText(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.radioButton_fr:
                        if (rb.isChecked()) {
                            editor.putString("lang_code", "fr");
                            editor.commit();
                            Toast.makeText(getApplicationContext(),"Vouz avez changé a "+ rb.getText(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.radioButton_es:
                        if (rb.isChecked()){
                            editor.putString("lang_code", "es");
                            editor.commit();
                            Toast.makeText(getApplicationContext(),"Usted cambio a "+ rb.getText(), Toast.LENGTH_SHORT).show();
                        }
                        break;
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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton_en:
                if (checked) {
                    editor.putString("lang_code", "en");
                    editor.commit();

                }
                break;
            case R.id.radioButton_fr:
                if (checked) {
                    editor.putString("lang_code", "fr");
                    editor.commit();

                }
                break;
            case R.id.radioButton_es:
                if (checked){
                    editor.putString("lang_code", "es");
                    editor.commit();
                }
                break;
        }
    }
}
