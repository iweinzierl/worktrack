package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.EActivity;

@EActivity
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
