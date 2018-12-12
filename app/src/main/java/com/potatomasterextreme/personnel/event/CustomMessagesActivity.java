package com.potatomasterextreme.personnel.event;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.potatomasterextreme.personnel.MainActivity;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.CustomMessagesRecyclerViewAdapter;
import com.potatomasterextreme.personnel.adapters.EventsRecyclerViewAdapter;
import com.potatomasterextreme.personnel.contact.ContactActivity;
import com.potatomasterextreme.personnel.contact.ContactAddEventActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.listeners.OutgoingMessageListener;

import java.util.HashMap;

public class CustomMessagesActivity extends BaseActivity implements View.OnClickListener, OutgoingMessageListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public boolean reminders;
    public boolean outgoing;
    public HashMap<String, HashMap<String, String>> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_messages);
        reminders = getIntent().getBooleanExtra("reminders", false);
        outgoing = getIntent().getBooleanExtra("outgoing", false);
        if (outgoing) {
            messages = FileManager.fileManager.readFromFileHashMapHash(this
                    , getString(R.string.sending_messages) + getString(R.string.data_format));
            findViewById(R.id.fab).setVisibility(View.GONE);
        } else {
            if (reminders) {
                messages = FileManager.fileManager.readFromFileHashMapHash(this
                        , getString(R.string.reminders) + getString(R.string.data_format));
                setTitle(getString(R.string.reminders));
            } else {
                messages = FileManager.fileManager.readFromFileHashMapHash(this
                        , getString(R.string.custom_folder) + getIntent().getStringExtra("name") + getString(R.string.custom_messages_file));
                setTitle(getString(R.string.custom_messages));
            }
        }
        findViewById(R.id.fab).setOnClickListener(this);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomMessagesRecyclerViewAdapter(messages, R.layout.fragment_custom_message, this);
        mRecyclerView.setAdapter(mAdapter);
        MessageManager.outgoingMessageContext = this;
    }

    public void reload() {
        if (outgoing) {
            messages = FileManager.fileManager.readFromFileHashMapHash(this
                    , getString(R.string.sending_messages) + getString(R.string.data_format));
            findViewById(R.id.fab).setVisibility(View.GONE);
        } else {
            if (reminders) {
                messages = FileManager.fileManager.readFromFileHashMapHash(this
                        , getString(R.string.reminders) + getString(R.string.data_format));
                setTitle("Reminders");
            } else {
                messages = FileManager.fileManager.readFromFileHashMapHash(this
                        , getString(R.string.custom_folder) + getIntent().getStringExtra("name") + getString(R.string.custom_messages_file));
                setTitle("Custom messages");
            }
        }
        mAdapter = new CustomMessagesRecyclerViewAdapter(messages, R.layout.fragment_custom_message, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add the events menu
        getMenuInflater().inflate(R.menu.outgoing_messages_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_messages:
                deleteFile(getString(R.string.sending_messages) + getString(R.string.data_format));
                reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add message");
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_template2, null);

        alert.setView(dialogView);
        alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                HashMap<String, HashMap<String, String>> message = new HashMap<>();
                String messageText = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                if (!messageText.isEmpty() && !message.containsKey(messageText)) {
                    message.put(messageText, new HashMap<String, String>());
                    if (!reminders) {
                        FileManager.fileManager.add(CustomMessagesActivity.this, FileManager.WhatToDo.ADD
                                , getString(R.string.custom_folder) + getIntent().getStringExtra("name") + getString(R.string.custom_messages_file), message);
                    } else {
                        FileManager.fileManager.add(CustomMessagesActivity.this, FileManager.WhatToDo.ADD
                                , getString(R.string.reminders) + getString(R.string.data_format), message);
                    }
                    messages.putAll(message);
                    reload();
                } else {
                    Toast.makeText(CustomMessagesActivity.this, "Message already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    @Override
    public void updateMessages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reload();
            }
        });
    }
}
