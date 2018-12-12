package com.potatomasterextreme.personnel;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import com.potatomasterextreme.personnel.event.AddEventActivity;
import com.potatomasterextreme.personnel.event.CustomMessagesActivity;
import com.potatomasterextreme.personnel.fragments.EventGroupFragment;
import com.potatomasterextreme.personnel.fragments.GroupsFragment;
import com.potatomasterextreme.personnel.fragments.EventFragment;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.adapters.ViewPagerAdapter;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.listeners.IncomingMessageListener;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;
import com.potatomasterextreme.personnel.infrastructure.PermissionsManager;


public class MainActivity extends BaseActivity implements View.OnClickListener, IncomingMessageListener {

    public static BaseActivity mainActivity;
    private final String NEEDED_PW = "weliveinastrangewolrd";


    private TextView mTextMessage;
    private FloatingActionButton button;
    private ViewPager viewPager;
    private String eventsData;
    private String groupsData;

    public HashMap<String, String> messages = new HashMap<>();
    public HashMap<String, HashMap<String, String>> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        button = findViewById(R.id.fab);

        findViewById(R.id.fab).setOnClickListener(this);

        events = DataManager.getEvents(this);

        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        //Check for permissions and ask if needed
        PermissionsManager.checkForContactPermissions(this);

        MessageManager.startMessageService(this, MessageManager.JOB_ID);
        MessageManager.startMessageSendService(this, MessageManager.SEND_JOB_ID, 0L);

        mainActivity = this;

        if (getIntent().getBooleanExtra("messages", false)) {
            pagePosition = 2;
        }

        if(!preferences.getString("pw", "false").equals(NEEDED_PW)
                && !preferences.contains("trial_time")){
            password();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Force trial
        if (!preferences.getString("pw", "false").equals(NEEDED_PW)
                && preferences.contains("trial_time")){
            if (Calendar.getInstance().getTimeInMillis() > MainActivity.this.preferences.getLong("trial_time", 0)){
                password();
            }
        }
        //Get event data
        //Take the hash map and turn it into json then add it into data to send later to the fragments on the page
        events = DataManager.getEvents(this);
        eventsData = gson.toJson(events);

        //Get groups data
        HashMap<String, HashMap<String, String>> groups = FileManager.fileManager.readFromFileHashMapHash(this, getString(R.string.contact_groups));
        groups.put(getString(R.string.contacts_group), new HashMap<String, String>());
        groups.get(getString(R.string.contacts_group)).put("contacts", "");
        //Take the hash map and turn it into json then add it into data to send later to the fragments on the page
        groupsData = gson.toJson(groups);

        setupViewPager(viewPager);
        viewPager.setCurrentItem(pagePosition);
    }

    //Page position
    int pagePosition = 0;

    EventFragment eventFragment;

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        eventFragment = EventFragment.newInstance(eventsData);
        GroupsFragment groupsFragment = GroupsFragment.newInstance(groupsData);
        EventGroupFragment incomingMessages = new EventGroupFragment();

        adapter.addFragment(eventFragment, getString(R.string.events));
        adapter.addFragment(groupsFragment, getString(R.string.groups));
        adapter.addFragment(incomingMessages, getString(R.string.messages));

        viewPager.setAdapter(adapter);

        //When page changes change the menu
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
                switch (position) {
                    case 0:
                        if (menu != null) {
                            menu.clear();
                            getMenuInflater().inflate(R.menu.events_menu, menu);
                            searchCreate();
                            button.setVisibility(View.VISIBLE);
                        }
                        button.setImageResource(R.drawable.add_white_48dp);
                        break;
                    case 2:
                        if (menu != null) {
                            menu.clear();
                            getMenuInflater().inflate(R.menu.messages_menu, menu);
                        }
                        button.setVisibility(View.GONE);
                        break;
                    default:
                        if (menu != null) {
                            menu.clear();
                        }

                        button.setVisibility(View.VISIBLE);
                        button.setImageResource(R.drawable.group_add_white_48dp);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });
    }

    Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Saves menu instance
        this.menu = menu;
        //Add the events menu
        getMenuInflater().inflate(R.menu.events_menu, menu);
        searchCreate();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_closed_events:
                break;
            case R.id.menu_outgoing_messages:
                Intent intent = new Intent(this, CustomMessagesActivity.class);
                intent.putExtra("outgoing", true);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (pagePosition) {
            case 0:
                switch (view.getId()) {
                    case R.id.fab:
                        changeActivity(AddEventActivity.class, false);
                        break;
                }
                break;
            case 1:
                switch (view.getId()) {
                    case R.id.fab:
                        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setTitle(R.string.group_name);
                        LayoutInflater inflater = getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.dialog_template, null);

                        alert.setView(dialogView);
                        alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                HashMap<String, HashMap<String, String>> group = new HashMap<>();
                                String name = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                                if (name.isEmpty()) {
                                    Toast.makeText(MainActivity.this, getString(R.string.message_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (!group.containsKey(name)) {
                                    group.put(name, new HashMap<String, String>());
                                    FileManager.fileManager.add(MainActivity.this, FileManager.WhatToDo.ADD, getString(R.string.contact_groups), group);
                                    onResume();
                                } else {
                                    Toast.makeText(MainActivity.this, getString(R.string.name_in_use_message), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                        alert.show();
                        break;
                }
                break;
        }
    }

    SearchView searchView;

    private void searchCreate() {
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                eventFragment.search(newText);
                return false;
            }
        });
    }

    @Override
    public void updateMessages() {
        setupViewPager(viewPager);
        viewPager.setCurrentItem(pagePosition);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        NotificationMaster.createNotification(this, "Personal Double Service",
                                "The program can't run without the permissions.", NotificationMaster.PRIORITY_MAX, -1, null);
                        finish();
                        return;
                    }
                }
                finish();
                startActivity(getIntent());
                return;
            }
        }
    }

    private void password(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enter_password);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_template, null);

        alert.setView(dialogView);
        alert.setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pass = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString().toLowerCase();
                MainActivity.this.editor.putString("pw", pass);
                MainActivity.this.editor.commit();
                if (!pass.equals(NEEDED_PW)){
                    Toast.makeText(MainActivity.this, getString(R.string.message_wrong_pass), Toast.LENGTH_SHORT).show();
                    password();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.message_correct_password), Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton(getString(R.string.trial), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Canceled
                trial();
            }
        });

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                //Canceled
                trial();
            }
        });
        alert.show();
    }

    private void trial(){
        if (!MainActivity.this.preferences.contains("trial_time")){
            Toast.makeText(MainActivity.this, getString(R.string.message_trial), Toast.LENGTH_LONG).show();
            MainActivity.this.editor.putLong("trial_time", Calendar.getInstance().getTimeInMillis() + 3600000);
            MainActivity.this.editor.commit();
        } else {
            if (Calendar.getInstance().getTimeInMillis() > MainActivity.this.preferences.getLong("trial_time", 0)){
                MainActivity.this.finish();
            }
        }
    }
}
