package com.potatomasterextreme.personnel.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.ContactManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddGroupActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private HashMap<String, HashMap<String, String>> group = new HashMap<>();
    private HashMap<String, HashMap<String, String>> searchGroup;

    public List<String> added = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        HashMap<String, String> tempGroup = gson.fromJson(getIntent().getStringExtra("group"), HashMap.class);
        if (getIntent().getBooleanExtra("is_contacts", false)) {
            group = ContactManager.getContacts(this);
            //Get the contacts that already exists in the group and skip them
            for (String key : ((HashMap<String, HashMap<String, String>>) gson.fromJson(getIntent().getStringExtra("skip_group"), FileManager.GetHsHssType())).keySet()) {
                group.remove(key);
            }
        } else {
            for (String key : tempGroup.keySet()) {
                HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(this);
                if (contacts.containsKey(key)) {
                    group.put(key, contacts.get(key));
                }
                for (String key1 : ((HashMap<String, HashMap<String, String>>) gson.fromJson(getIntent().getStringExtra("skip_group"), FileManager.GetHsHssType())).keySet()) {
                    group.remove(key1);
                }
            }
        }

        mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.add_contact_card_view, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Saves menu instance
        //Add the events menu
        getMenuInflater().inflate(R.menu.add_group_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchGroup = new HashMap<>();
                for (String key : group.keySet()) {
                    if (group.get(key).get("name").toLowerCase().contains(newText.toLowerCase())
                            || group.get(key).get("phone").toLowerCase().contains(newText.toLowerCase())) {
                        searchGroup.put(key, new HashMap<String, String>());
                        searchGroup.get(key).putAll(group.get(key));
                    }
                }

                //Reload the adapter
                mAdapter = new RecyclerViewAdapter(gson.toJson(searchGroup), R.layout.add_contact_card_view, AddGroupActivity.this);
                mRecyclerView.setAdapter(mAdapter);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu_button:
                Intent intent = new Intent();
                String[] extra = new String[added.size()];
                added.toArray(extra);
                intent.putExtra("to_add", extra);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
