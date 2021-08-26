package com.android.taskmaster2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;

public class SettingPage extends AppCompatActivity {

    String theTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        Spinner spinner2 =  findViewById(R.id.spinnerForTeam);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.team_menu, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                theTeam = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                theTeam = (String) parent.getItemAtPosition(0);

            }
        });

        Button saveBtn = findViewById(R.id.save_username_btn);
        saveBtn.setOnClickListener(v -> {
            String username = ((EditText)findViewById(R.id.username_edit_txt)).getText().toString();

            prefEditor.putString("Username",username);
            prefEditor.putString("team",theTeam);

            prefEditor.apply();
            Toast.makeText(SettingPage.this,"username updated",Toast.LENGTH_LONG).show();
        });
        Button goHome = SettingPage.this.findViewById(R.id.goHome_btn);
        goHome.setOnClickListener(v -> {
            Intent intent = new Intent(SettingPage.this,MainActivity.class);
            startActivity(intent);
        });

        Button logOut = SettingPage.this.findViewById(R.id.log_out);
        logOut.setOnClickListener(v -> {
            Intent logout = new Intent(getApplicationContext(),LogIn.class);
            Amplify.Auth.signOut(
                    () -> {
                        Log.i("AuthQuickstart", "Signed out successfully");
                        SettingPage.this.startActivity(logout);
                    },
                    error -> Log.e("AuthQuickstart", error.toString())
            );

        });

    }
}