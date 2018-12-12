package com.potatomasterextreme.personnel.infrastructure;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;

    public static Gson gson = new Gson();

    public boolean isOnMessanger = false;
    private boolean hidden = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("PREFS_FILE_NAME", MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * changes the screen.
     *
     * @param targetClass          .class object the target
     * @param closeCurrentActivity should i close the current screen?
     */
    protected void changeActivity(Class targetClass, boolean closeCurrentActivity) {
        Intent intent = new Intent(this, targetClass);
        startActivity(intent);
        if (closeCurrentActivity) {
            finish();
        }
    }

    public void hideTitleBar() {
        if (!hidden) {
            //Remove notification bar
            //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //Remove title bar
            getSupportActionBar().hide();
            hidden = true;
        }
    }

    public void showTitleBar() {
        if (hidden) {
            //Remove notification bar
            //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            //Clear the fullscreen flag, to fix the title going on the top bar
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //Remove title bar
            getSupportActionBar().show();
            hidden = false;
        }
    }
}
