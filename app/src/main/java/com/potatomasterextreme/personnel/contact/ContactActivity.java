package com.potatomasterextreme.personnel.contact;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.ViewPagerAdapter;
import com.potatomasterextreme.personnel.fragments.EventFragment;
import com.potatomasterextreme.personnel.fragments.MessagesFragment;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.listeners.IncomingMessageListener;

import java.util.HashMap;

public class ContactActivity extends BaseActivity implements View.OnClickListener, IncomingMessageListener {

    private FloatingActionButton button;
    private ViewPager viewPager;

    public String phone;
    private String groupName;
    public HashMap<String, HashMap<String, String>> events = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        button = findViewById(R.id.fab);
        button.setOnClickListener(this);
        button.setVisibility(View.GONE);

        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        phone = getIntent().getStringExtra("phone");
        if (getIntent().getStringExtra("name") != null) {
            groupName = getIntent().getStringExtra("name");
        } else {
            groupName = phone;
        }
        setTitle(groupName);

        HashMap<String, HashMap<String, String>> allEvents = DataManager.getEvents(this);
        for (String key : allEvents.keySet()) {
            String id = allEvents.get(key).get("id");
            HashMap<String, HashMap<String, String>> groups = DataManager.getGroupsFromFile(this, id);
            for (String key1 : groups.keySet()) {
                if (groups.get(key1).containsKey(phone)) {
                    events.put(key, allEvents.get(key));
                }
            }
        }
        isOnMessanger = true;
        //When page changes change the menu
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    isOnMessanger = true;
                } else {
                    isOnMessanger = false;
                    //Shows title bar and hides keyboard when moves to another window
                    showTitleBar();
                    View view = ContactActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });

        MessageManager.incomingMessageContext = this;

        setupViewPager(viewPager);
    }

    public void reload() {
        setupViewPager(viewPager);
        viewPager.setCurrentItem(pagePosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, HashMap<String, String>> allEvents = DataManager.getEvents(this);
        for (String key : allEvents.keySet()) {
            String id = allEvents.get(key).get("id");
            HashMap<String, HashMap<String, String>> groups = DataManager.getGroupsFromFile(this, id);
            for (String key1 : groups.keySet()) {
                if (groups.get(key1).containsKey(phone)) {
                    events.put(key, allEvents.get(key));
                }
            }
        }
        reload();
    }

    EventFragment eventFragment;

    //Page position
    int pagePosition = 0;

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        MessagesFragment messagesFragment = new MessagesFragment();
        eventFragment = new EventFragment();

        adapter.addFragment(messagesFragment, getString(R.string.messages));
        adapter.addFragment(eventFragment, getString(R.string.events));

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
                            getMenuInflater().inflate(R.menu.contacts_menu, menu);
                        }
                        button.setVisibility(View.GONE);
                        break;
                    case 1:
                        if (menu != null) {
                            menu.clear();
                            getMenuInflater().inflate(R.menu.contact_events_menu, menu);
                            searchCreate();
                        }
                        button.setVisibility(View.VISIBLE);
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


    SearchView searchView;

    Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Saves menu instance
        this.menu = menu;
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (pagePosition) {
            case 0:
                switch (item.getItemId()) {
                    case R.id.action_call:
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null)));
                        break;
                    case R.id.action_whatsapp:
                        /*String url = "https://api.whatsapp.com/send?phone=" + "" + phone;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);*/
                        String country_code = "";
                        if (phone.charAt(0) != '+' && !phone.substring(0, 2).equals("972")){
                            country_code += "+972";
                        }
                        String url = "https://api.whatsapp.com/send?phone=" + country_code + phone;
                        try {
                            PackageManager pm = getPackageManager();
                            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        } catch (PackageManager.NameNotFoundException e) {
                            Toast.makeText(this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        break;
                    case R.id.confirm:
                        //Loop all the events
                        for (String event_id : DataManager.getEvents(this).keySet()) {
                            //Get all the groups from the current event
                            HashMap<String, HashMap<String, String>> groups = DataManager.getGroupsFromFile(this, event_id);
                            HashMap<String, HashMap<String, String>> groupToSave = new HashMap<>();
                            //Loop all the groups of the current event
                            for (String group_name : groups.keySet()) {
                                //Loop all the phone numbers of the current group
                                for (String phone_number : groups.get(group_name).keySet()) {
                                    //If the phone number is present then change true to its state
                                    if (phone_number.equals(phone)) {
                                        if (!groupToSave.containsKey(group_name)) {
                                            groupToSave.put(group_name, new HashMap<String, String>());
                                        }
                                        groupToSave.get(group_name).put(phone_number, "true");
                                    }
                                }
                            }
                            //Save the change
                            FileManager.fileManager.add(this, FileManager.WhatToDo.ADD, getString(R.string.groups_folder) + event_id + getString(R.string.data_format), groupToSave);
                        }
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Intent intent = new Intent(this, ContactAddEventActivity.class);
                intent.putExtra("phone", phone);
                intent.putExtra("name", groupName);
                intent.putExtra("events", gson.toJson(events));
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    public void updateMessages() {
        reload();
        HashMap<String, HashMap<String, String>> messages = FileManager.fileManager.readFromFileHashMapHash(this,
                "messages" + getString(R.string.data_format));
        if (messages.containsKey(phone)) {
            HashMap<String, HashMap<String, String>> message = new HashMap<>();
            message.put(phone, new HashMap<String, String>());
            message.get(phone).put("msg_count", "0");
            FileManager.fileManager.add(this, FileManager.WhatToDo.ADD, getString(R.string.messages_file) + getString(R.string.data_format), message);
        }
    }
}
