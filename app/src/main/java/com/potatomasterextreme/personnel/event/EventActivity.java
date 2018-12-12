package com.potatomasterextreme.personnel.event;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.listeners.IncomingMessageListener;

import java.util.HashMap;

public class EventActivity extends BaseActivity implements View.OnClickListener, IncomingMessageListener{

    String data;
    HashMap<String, HashMap<String, String>> groups;
    public HashMap<String, String> event;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        findViewById(R.id.fab).setOnClickListener(this);

        //Add event card view to the activity
        ((LinearLayout) findViewById(R.id.event_layout)).addView(getLayoutInflater().inflate(R.layout.event_card_view, null));

        //Get recycler view
        mRecyclerView = findViewById(R.id.my_recycler_view);
        //Use this setting to improve performance if you know that changes
        //in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        //Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        event = gson.fromJson(getIntent().getStringExtra("event"), HashMap.class);
        groups = DataManager.getGroups(this, event.get("id"));

        assignAdapter();

        //Set title
        setTitle(event.get("name"));

        MessageManager.incomingMessageContext = this;

        setEventView();
    }

    private void setEventView() {
        TextView title = findViewById(R.id.title);
        TextView desc = findViewById(R.id.event_desc);
        TextView time = findViewById(R.id.time);
        TextView countMessage = findViewById(R.id.workers_count);
        TextView confirmationMessage = findViewById(R.id.confirmed_workers);

        if (event.containsKey("name")) {
            title.setText(event.get("name"));
        }
        if (event.containsKey("desc")) {
            desc.setText(event.get("desc"));
        } else {
            desc.setVisibility(View.GONE);
        }

        if (event.containsKey("date")) {

            CardView date = findViewById(R.id.date_card);
            ((TextView) date.findViewById(R.id.date)).setText(RecyclerViewAdapter.getDate(this, event.get("date"), false));
            date.setVisibility(View.VISIBLE);

        }

        if (event.containsKey("time")) {
            time.setText(event.get("time"));
        } else {
            time.setText("");
        }

        boolean started = false;
        if (event.containsKey("id")) {
            ((EditText) findViewById(R.id.event_id)).setText(event.get("id"));
            int people_count = 0;
            int confirmed_count = 0;
            DataManager.updateGroups(this, event.get("id"));
            HashMap<String, HashMap<String, String>> groups = DataManager.getGroups(this, event.get("id"));
            for (String key : groups.keySet()) {
                if (groups.get(key).containsKey("group_status")){
                    started = true;
                }
                for (String key1 : groups.get(key).keySet()) {
                    if (PhoneNumberUtils.isDialable(key1.charAt(0))) {
                        people_count++;
                        if (groups.get(key).get(key1).equals("true")) {
                            confirmed_count++;
                        }
                    }
                }
            }

            //Count message
            if (event.containsKey("worker_count")) {
                int neededCount = Integer.parseInt(event.get("worker_count"));
                countMessage.setText(people_count + " " + getString(R.string.message_workers_count_1) + " " + neededCount + " " + getString(R.string.message_workers_count_2));
                //Set the text color
                if (neededCount / 2 < people_count) {
                    countMessage.setTextColor(ContextCompat.getColor(this, R.color.warningColor));
                }
                if (neededCount / 2 > people_count) {
                    countMessage.setTextColor(ContextCompat.getColor(this, R.color.urgentColor));
                }
                if (neededCount <= people_count) {
                    countMessage.setTextColor(ContextCompat.getColor(this, R.color.goodColor));
                }
            } else {
                countMessage.setText(people_count + " " + getString(R.string.message_workers_count_3) + " " + getString(R.string.message_workers_count_2));
                countMessage.setTextColor(ContextCompat.getColor(this, R.color.warningColor));
            }

            //Confirmation message
            if (people_count > 0 && started) {
                confirmationMessage.setText(confirmed_count + " " + getString(R.string.message_event_confirmation_1) + " " + people_count + " " + getString(R.string.message_event_confirmation_2));
                //Set the text color
                if (confirmed_count / 2 < people_count) {
                    confirmationMessage.setTextColor(ContextCompat.getColor(this, R.color.warningColor));
                }
                if (confirmed_count / 2 > people_count) {
                    confirmationMessage.setTextColor(ContextCompat.getColor(this, R.color.urgentColor));
                }
                if (confirmed_count >= people_count) {
                    confirmationMessage.setTextColor(ContextCompat.getColor(this, R.color.goodColor));
                }
                confirmationMessage.setVisibility(View.VISIBLE);
            } else {
                confirmationMessage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.updateGroups(this, event.get("id"));
        groups = DataManager.getGroups(this, event.get("id"));
        assignAdapter();
        setEventView();
        MessageManager.incomingMessageContext = this;
    }

    private void assignAdapter() {
        //Get event
        Gson gson = new Gson();
        //Get groups and send it to RecyclerViewAdapter
        //Take the hash map and turn it into json
        data = gson.toJson(groups);

        //Specify an adapter (see also next example)
        mAdapter = new RecyclerViewAdapter(data, R.layout.event_group_card_view, this, event.get("id"));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
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
                        //HashMap<String, HashMap<String, String>> groups = FileManager.fileManager.readFromFileHashMapHash(EventActivity.this, getString(R.string.groups_folder) + event.get("id") + getString(R.string.data_format));
                        if (name.length() == 0) {
                            Toast.makeText(EventActivity.this, getString(R.string.message_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!groups.containsKey(name)) {
                            //Checks if there already a group with that name
                            group.put(name, new HashMap<String, String>());
                            FileManager.fileManager.add(EventActivity.this, FileManager.WhatToDo.ADD, getString(R.string.groups_folder) + event.get("id") + getString(R.string.data_format), group);
                            //Add the group to the groups
                            groups.putAll(group);
                            assignAdapter();
                        } else {
                            Toast.makeText(EventActivity.this, getString(R.string.name_in_use_message), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add the events menu
        getMenuInflater().inflate(R.menu.event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_event:
                Intent intent = new Intent(this, AddEventActivity.class);
                String extra = getIntent().getStringExtra("event");
                intent.putExtra("event", extra);
                startActivity(intent);
                finish();
                break;
            case R.id.delete_event:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(EventActivity.this.getString(R.string.delete_message_event));

                builder1.setPositiveButton(
                        EventActivity.this.getString(R.string.delete),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Delete all the messages
                                for (String key : groups.keySet()) {
                                    FileManager.removeFile(EventActivity.this, "messages/" + event.get("id") + key + getString(R.string.data_format));
                                }
                                //Removes all the groups of the event
                                FileManager.removeFile(EventActivity.this, getString(R.string.groups_folder) + event.get("id") + getString(R.string.data_format));
                                HashMap<String, String> delete = new HashMap<>();
                                delete.put(event.get("id"), "");
                                //Deletes the event from events.data
                                FileManager.fileManager.add(EventActivity.this, FileManager.WhatToDo.REMOVE, getString(R.string.events_file), delete);
                                DataManager.updateEvents(EventActivity.this);
                                EventActivity.this.finish();
                            }
                        });

                builder1.setNegativeButton(
                        EventActivity.this.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                AlertDialog alert1 = builder1.create();
                alert1.show();
                break;
            case R.id.finish_event:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage(EventActivity.this.getString(R.string.finish_message));

                builder2.setPositiveButton(
                        EventActivity.this.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Saves the event into Archive
                                HashMap<String, HashMap<String, String>> finishedEvent = new HashMap<>();
                                finishedEvent.put(event.get("id"), event);
                                FileManager.fileManager.add(EventActivity.this, FileManager.WhatToDo.REMOVE, getString(R.string.archived_events), finishedEvent);
                                //Deletes the event from events.data
                                HashMap<String, String> delete = new HashMap<String, String>();
                                delete.put(event.get("id"), "");
                                FileManager.fileManager.add(EventActivity.this, FileManager.WhatToDo.REMOVE, getString(R.string.events_file), delete);
                                EventActivity.this.finish();
                            }
                        });

                builder2.setNegativeButton(
                        EventActivity.this.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                AlertDialog alert2 = builder2.create();
                alert2.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateMessages() {
        DataManager.updateEvents(this);
        onResume();
    }
}
