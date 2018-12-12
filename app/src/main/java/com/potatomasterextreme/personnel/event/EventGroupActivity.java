package com.potatomasterextreme.personnel.event;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.ViewPagerAdapter;
import com.potatomasterextreme.personnel.fragments.ConfirmationFragment;
import com.potatomasterextreme.personnel.fragments.EventGroupFragment;
import com.potatomasterextreme.personnel.fragments.MessagesFragment;
import com.potatomasterextreme.personnel.group.AddGroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.ContactManager;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.listeners.IncomingMessageListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class EventGroupActivity extends BaseActivity implements View.OnClickListener, IncomingMessageListener {

    private FloatingActionButton button;
    private ViewPager viewPager;

    public String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        button = findViewById(R.id.fab);
        button.setImageResource(R.drawable.person_add_white_48dp);
        button.setOnClickListener(this);

        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        groupName = getIntent().getStringExtra("group_name");
        setTitle(groupName);

        //When page changes change the menu
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1 || position == 2) {
                    isOnMessanger = true;
                } else {
                    isOnMessanger = false;
                    //Shows title bar and hides keyboard when moves to another window
                    showTitleBar();
                    View view = EventGroupActivity.this.getCurrentFocus();
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

        setupViewPager(viewPager);
        MessageManager.incomingMessageContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload(){
        DataManager.updateGroups(this, getIntent().getStringExtra("event_id"));
        //setupViewPager(viewPager);
        if (eventGroupFragment != null) {
            eventGroupFragment.reload();
        }
        viewPager.setCurrentItem(pagePosition);
    }

    private void superReload(){
        DataManager.updateGroups(this, getIntent().getStringExtra("event_id"));
        setupViewPager(viewPager);
        viewPager.setCurrentItem(pagePosition);
    }

    //Page position
    public int pagePosition = 0;

    public EventGroupFragment eventGroupFragment;
    private ConfirmationFragment confirmationFragment;

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        eventGroupFragment = new EventGroupFragment();
        MessagesFragment messagesFragment = new MessagesFragment();
        confirmationFragment = new ConfirmationFragment();

        adapter.addFragment(eventGroupFragment, getString(R.string.the_group));
        adapter.addFragment(messagesFragment, getString(R.string.messages));
        adapter.addFragment(confirmationFragment, getString(R.string.confirmation));

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
                            getMenuInflater().inflate(R.menu.group_menu, menu);
                            searchCreate();
                        }
                        button.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        if (menu != null) {
                            menu.clear();
                            getMenuInflater().inflate(R.menu.confirmation_menu, menu);
                            if (eventGroupFragment.tempGroup.containsKey("group_status")) {
                                switch (eventGroupFragment.tempGroup.get("group_status")) {
                                    case "sending":
                                        menu.findItem(R.id.set_menu_button).setVisible(false);
                                        menu.findItem(R.id.cancel_menu_button).setVisible(true);
                                        break;
                                    case "sent":
                                    case "done":
                                        menu.findItem(R.id.set_menu_button).setVisible(false);
                                        menu.findItem(R.id.reset_menu_button).setVisible(true);
                                        break;
                                }
                            }
                        }
                        button.setVisibility(View.GONE);
                        break;
                    default:
                        if (menu != null) {
                            menu.clear();
                        }
                        button.setVisibility(View.GONE);
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

    public Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Saves menu instance
        this.menu = menu;
        //Add the events menu
        getMenuInflater().inflate(R.menu.group_menu, menu);
        menu.findItem(R.id.copy).setVisible(true);
        searchCreate();

        return true;
    }

    private void searchCreate() {
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                eventGroupFragment.search(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (pagePosition) {
            case 0:
                switch (item.getItemId()) {
                    case R.id.menu_change_name:
                        change_name();
                        break;
                    case R.id.copy:
                        HashMap<String, String> aGroup = DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(groupName);
                        String groupToText = "";
                        for (String key : aGroup.keySet()){
                            if(PhoneNumberUtils.isDialable(key.charAt(0))) {
                                groupToText += aGroup.get(key) + ": " + key + "\n";
                            }
                        }
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(groupName, groupToText);
                        clipboard.setPrimaryClip(clip);
                        break;
                    case R.id.menu_delete:
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                        builder1.setMessage(this.getString(R.string.delete_message_group));

                        builder1.setPositiveButton(
                                this.getString(R.string.delete),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Removes all the group
                                        HashMap<String, HashMap<String, String>> thisGroup = new HashMap<>();
                                        thisGroup.put(groupName, new HashMap<String, String>());
                                        thisGroup.get(groupName).putAll(eventGroupFragment.tempGroup);
                                        FileManager.fileManager.add(EventGroupActivity.this, FileManager.WhatToDo.REMOVE
                                                , EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), thisGroup);
                                        DataManager.getGroups(EventGroupActivity.this, getIntent().getStringExtra("event_id")).remove(groupName);
                                        EventGroupActivity.this.finish();
                                    }
                                });

                        builder1.setNegativeButton(
                                this.getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });

                        AlertDialog alert1 = builder1.create();
                        alert1.show();
                        break;
                }
                break;
            case 2:
                switch (item.getItemId()) {
                    case R.id.reset_menu_button:
                    case R.id.set_menu_button:
                        HashMap<String, HashMap<String, String>> group = new HashMap<>();
                        group.put(groupName, new HashMap<String, String>());
                        if (item.getItemId() == R.id.reset_menu_button){
                            HashMap<String, String> theGroup = DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(groupName);
                            for (String key: theGroup.keySet()){
                                if (PhoneNumberUtils.isDialable(key.charAt(0))){
                                    group.get(groupName).put(key, "false");
                                    DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(groupName).put(key, "false");
                                }
                            }
                        }
                        group.get(groupName).put("group_message", confirmationFragment.editText.getText().toString());
                        group.get(groupName).put("remind_every", confirmationFragment.editReminder.getText().toString());
                        group.get(groupName).put("group_status", "sending");

                        DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(groupName).put("group_message", confirmationFragment.editText.getText().toString());
                        DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(groupName).put("remind_every", confirmationFragment.editReminder.getText().toString());
                        DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(groupName).put("group_status", "sending");

                        eventGroupFragment.tempGroup.put("group_message", confirmationFragment.editText.getText().toString());
                        eventGroupFragment.tempGroup.put("remind_every", confirmationFragment.editReminder.getText().toString());
                        eventGroupFragment.tempGroup.put("group_status", "sending");

                        try {
                            //Adds send date
                            Date theDate = new SimpleDateFormat("HH:mm dd/MM/yyyy").parse(confirmationFragment.getTime() + " " + confirmationFragment.getDate());
                            group.get(groupName).put("send_date", String.valueOf(theDate.getTime()));
                            //Add the next reminder
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(theDate);
                            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(confirmationFragment.editReminder.getText().toString()));
                            group.get(groupName).put("next_reminder", String.valueOf(cal.getTimeInMillis()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        FileManager.fileManager.add(EventGroupActivity.this, FileManager.WhatToDo.ADD
                                , EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), group);

                        menu.findItem(R.id.set_menu_button).setVisible(false);
                        menu.findItem(R.id.reset_menu_button).setVisible(false);
                        menu.findItem(R.id.cancel_menu_button).setVisible(true);

                        Toast.makeText(this, R.string.confirmation_send_message, Toast.LENGTH_LONG).show();
                        reload();
                        break;
                    case R.id.cancel_menu_button:
                        HashMap<String, HashMap<String, String>> group1 = new HashMap<>();
                        group1.put(groupName, new HashMap<String, String>());
                        group1.get(groupName).put("group_status", "canceled");
                        FileManager.fileManager.add(EventGroupActivity.this, FileManager.WhatToDo.ADD
                                , EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), group1);

                        menu.findItem(R.id.cancel_menu_button).setVisible(false);
                        menu.findItem(R.id.set_menu_button).setVisible(true);
                        break;
                    case R.id.custom_messages_menu_button:
                        Intent intent = new Intent(this, CustomMessagesActivity.class);
                        intent.putExtra("name", groupName);
                        startActivityForResult(intent, 3);
                        break;
                    case R.id.reminder_messages_menu_button:
                        Intent intent1 = new Intent(this, CustomMessagesActivity.class);
                        intent1.putExtra("reminders", true);
                        startActivity(intent1);
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void change_name() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.group_name);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_template, null);

        alert.setView(dialogView);
        alert.setPositiveButton(getString(R.string.change_name), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Get groups data
                String name = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                HashMap<String, HashMap<String, String>> groups = FileManager.fileManager.readFromFileHashMapHash(EventGroupActivity.this, EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format));
                if (!groups.containsKey(name) && !name.equals(getString(R.string.contacts_group))) {
                    //Creates the current group and saves it
                    HashMap<String, HashMap<String, String>> thisGroup = new HashMap<>();
                    thisGroup.put(name, new HashMap<String, String>());
                    thisGroup.get(name).putAll(eventGroupFragment.tempGroup);
                    FileManager.fileManager.add(EventGroupActivity.this, FileManager.WhatToDo.ADD, EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), thisGroup);

                    //Delete the group with the last name
                    HashMap<String, String> delete = new HashMap<>();
                    delete.put(groupName, "");
                    FileManager.fileManager.add(EventGroupActivity.this, FileManager.WhatToDo.REMOVE, EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), delete);

                    //Change the name of the group if will want to change the name again
                    groupName = name;
                    EventGroupActivity.this.setTitle(groupName);
                } else {
                    Toast.makeText(EventGroupActivity.this, EventGroupActivity.this.getString(R.string.name_in_use_message), Toast.LENGTH_SHORT).show();
                    change_name();
                }
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
        EditText editText = dialogView.findViewById(R.id.edit_text);
        editText.setText(groupName);
        editText.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    String[] toAdd = data.getStringArrayExtra("to_add");
                    HashMap<String, HashMap<String, String>> groupToSave = new HashMap<>();
                    String group_name = getIntent().getStringExtra("group_name");

                    HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(this);
                    groupToSave.put(group_name, new HashMap<String, String>());
                    for (String index : toAdd) {
                        HashMap<String, String> contact = contacts.get(index);
                        groupToSave.get(group_name).put(contact.get("phone"), contact.get("name"));
                        if (eventGroupFragment.tempGroup == null) {
                            eventGroupFragment.tempGroup = new HashMap<>();
                        }
                        eventGroupFragment.tempGroup.put(index, contact.get("name"));
                    }

                    //Change group status if status = done
                    DataManager.updateGroups(this, getIntent().getStringExtra("event_id"));
                    HashMap<String, String> thisGroup = DataManager.getGroups(this, getIntent().getStringExtra("event_id")).get(group_name);
                    String groupStatus = "";
                    if (thisGroup.containsKey("group_status")){
                        groupStatus = thisGroup.get("group_status");
                    }

                    if (groupStatus.equals("done")) {
                        HashMap<String, HashMap<String, String>> group = new HashMap<>();
                        group.put(group_name, new HashMap<String, String>());
                        group.get(group_name).put("group_status", "sent");
                        FileManager.fileManager.add(this, FileManager.WhatToDo.ADD
                                , getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), group);
                    }

                    //Send messages to the new people if the message sending already started
                    if (groupStatus.equals("done") || groupStatus.equals("sent")) {
                        for (String key : groupToSave.keySet()) {
                            for (String key1 : groupToSave.get(key).keySet()){
                                MessageManager.addSms(this, key1, thisGroup.get("group_message"));
                            }
                        }
                    }

                    //Save
                    FileManager.fileManager.add(this, FileManager.WhatToDo.ADD, EventGroupActivity.this.getString(R.string.groups_folder) + getIntent().getStringExtra("event_id") + getString(R.string.data_format), groupToSave);
                    DataManager.updateGroups(this, getIntent().getStringExtra("event_id"));

                    for (String key2 : groupToSave.keySet()) {
                        eventGroupFragment.tempGroup.putAll(groupToSave.get(key2));
                    }

                    eventGroupFragment.setGroup();
                    eventGroupFragment.reload();
                    break;
                case 2:
                    Intent intent = new Intent(this, AddGroupActivity.class);
                    intent.putExtra("skip_group", gson.toJson(eventGroupFragment.group));
                    intent.putExtra("group", data.getStringExtra("group"));

                    intent.putExtra("is_contacts", false);
                    startActivityForResult(intent, 1);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Intent intent = new Intent(this, ChooseGroupActivity.class);
                startActivityForResult(intent, 2);
                break;
        }
    }

    @Override
    public void updateMessages() {
        superReload();
    }
}
