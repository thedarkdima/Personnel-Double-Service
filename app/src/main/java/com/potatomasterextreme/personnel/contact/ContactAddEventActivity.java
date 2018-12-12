package com.potatomasterextreme.personnel.contact;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.fragments.EventFragment;
import com.potatomasterextreme.personnel.fragments.GroupsFragment;
import com.potatomasterextreme.personnel.fragments.MessagesFragment;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.util.HashMap;

public class ContactAddEventActivity extends BaseActivity {

    public HashMap<String, HashMap<String, String>> events = new HashMap<>();

    public EventFragment eventFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_add_event);

        HashMap<String, HashMap<String, String>> dontInclude = gson.fromJson(getIntent().getStringExtra("events"), FileManager.GetHsHssType());
        HashMap<String, HashMap<String, String>> allEvents = DataManager.getEvents(this);
        for (String key : allEvents.keySet()) {
            if (!dontInclude.containsKey(key)) {
                events.put(key, allEvents.get(key));
            }
        }

        isOnMessanger = true;

        eventFragment = new EventFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.top_layout, new MessagesFragment());
        fragmentTransaction.add(R.id.bottom_layout, eventFragment);
        fragmentTransaction.commit();

        final View mRootView = findViewById(R.id.main_layout);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                mRootView.getWindowVisibleDisplayFrame(measureRect);
                // measureRect.bottom is the position above soft keypad
                int keypadHeight = mRootView.getRootView().getHeight() - measureRect.bottom;

                int asd = mRootView.getRootView().getHeight();
                if (keypadHeight > mRootView.getRootView().getHeight() / 3) {
                    // keyboard is opened
                    findViewById(R.id.bottom_layout).setVisibility(View.GONE);
                } else {
                    //store keyboard state to use in onBackPress if you need to
                    findViewById(R.id.bottom_layout).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Toast.makeText(this, "Added " + getIntent().getStringExtra("name") + " to " + events.get(data.getStringExtra("remove_event")).get("name"), Toast.LENGTH_LONG).show();
                    events.remove(data.getStringExtra("remove_event"));
                    eventFragment.reload();
                    break;
            }
        }
    }
}
