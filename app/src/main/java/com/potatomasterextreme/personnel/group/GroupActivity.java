package com.potatomasterextreme.personnel.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.ContactManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.util.HashMap;

public class GroupActivity extends BaseActivity implements View.OnClickListener{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private HashMap<String, HashMap<String, String>> group;
    private HashMap<String, HashMap<String, String>> searchGroup;
    public HashMap<String, String> tempGroup;
    private boolean contacts = false;

    public String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        //Sets on click listener for the tab button
        View fab = findViewById(R.id.fab);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        tempGroup = gson.fromJson(getIntent().getStringExtra("group"), HashMap.class);
        if (tempGroup.containsKey("contacts")) {
            group = ContactManager.getContacts(this);
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(this);
            setGroup();
        }

        groupName = getIntent().getStringExtra("group_name");

        //Set Title
        if (groupName.equals(getString(R.string.contacts_group))){
            groupName = groupName.substring(1);
            contacts = true;
        }
        setTitle(groupName);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.contact_card_view, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void reload(){
        setGroup();

        mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.contact_card_view, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setGroup(){
        HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(this);
        group = new HashMap<>();
        for (String key : tempGroup.keySet()) {
            if (contacts.containsKey(key)) {
                group.put(key, contacts.get(key));
            }
        }
    }

    SearchView searchView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Saves menu instance
        //Add the events menu
        getMenuInflater().inflate(R.menu.group_menu, menu);
        if (contacts){
            menu.findItem(R.id.menu_change_name).setVisible(false);
            menu.findItem(R.id.menu_delete).setVisible(false);
        }

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView)searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchGroup = new HashMap<>();
                for (String key: group.keySet()){
                    if(group.get(key).get("name").toLowerCase().contains(newText.toLowerCase())
                            || group.get(key).get("phone").toLowerCase().contains(newText.toLowerCase())){
                        searchGroup.put(key, new HashMap<String, String>());
                        searchGroup.get(key).putAll(group.get(key));
                    }
                }

                //Reload the adapter
                mAdapter = new RecyclerViewAdapter(gson.toJson(searchGroup), R.layout.contact_card_view, GroupActivity.this);
                mRecyclerView.setAdapter(mAdapter);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_change_name:
               change_name();
                break;
            case R.id.menu_delete:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(GroupActivity.this.getString(R.string.delete_message_group));

                builder1.setPositiveButton(
                        GroupActivity.this.getString(R.string.delete),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Removes all the group
                                HashMap<String, HashMap<String, String>> thisGroup = new HashMap<>();
                                thisGroup.put(groupName, new HashMap<String, String>());

                                thisGroup.get(groupName).putAll(tempGroup);
                                FileManager.fileManager.add(GroupActivity.this, FileManager.WhatToDo.REMOVE, GroupActivity.this.getString(R.string.contact_groups), thisGroup);
                                GroupActivity.this.finish();
                            }
                        });

                builder1.setNegativeButton(
                        GroupActivity.this.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                AlertDialog alert1 = builder1.create();
                alert1.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void change_name(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.group_name);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_template, null);

        alert.setView(dialogView);
        alert.setPositiveButton(getString(R.string.change_name), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Get groups data
                String name = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                HashMap<String, HashMap<String, String>> groups = FileManager.fileManager.readFromFileHashMapHash(GroupActivity.this, getString(R.string.contact_groups));
                if (!groups.containsKey(name) && !name.equals(getString(R.string.contacts_group))){
                    //Creates the current group and saves it
                    HashMap<String, HashMap<String, String>> thisGroup = new HashMap<>();
                    thisGroup.put(name, new HashMap<String, String>());
                    thisGroup.get(name).putAll(tempGroup);
                    FileManager.fileManager.add(GroupActivity.this, FileManager.WhatToDo.ADD, GroupActivity.this.getString(R.string.contact_groups), thisGroup);

                    //Delete the group with the last name
                    HashMap<String, String> delete = new HashMap<>();
                    delete.put(groupName, "");
                    FileManager.fileManager.add(GroupActivity.this, FileManager.WhatToDo.REMOVE, GroupActivity.this.getString(R.string.contact_groups), delete);

                    //Change the name of the group if will want to change the name again
                    groupName = name;
                    GroupActivity.this.setTitle(groupName);
                } else {
                    Toast.makeText(GroupActivity.this, GroupActivity.this.getString(R.string.name_in_use_message), Toast.LENGTH_SHORT).show();
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
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String[] toAdd = data.getStringArrayExtra("to_add");
                HashMap<String, HashMap<String, String>> groupToSave = new HashMap<>();
                String group_name = getIntent().getStringExtra("group_name");


                HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(this);
                groupToSave.put(group_name, new HashMap<String, String>());
                for (String index: toAdd){
                    HashMap<String, String> contact = contacts.get(index);
                    groupToSave.get(group_name).put(contact.get("phone"), contact.get("name"));
                    tempGroup.put(index, contact.get("name"));
                }

                //Save
                FileManager.fileManager.add(this, FileManager.WhatToDo.ADD, getString(R.string.contact_groups), groupToSave);

                setGroup();

                mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.contact_card_view, this);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.fab:
                Intent intent = new Intent(this, AddGroupActivity.class);
                intent.putExtra("skip_group", gson.toJson(group));
                intent.putExtra("is_contacts", true);
                startActivityForResult(intent, 1);
                break;
        }
    }
}
