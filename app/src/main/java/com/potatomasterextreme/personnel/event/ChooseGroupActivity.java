package com.potatomasterextreme.personnel.event;

import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.fragments.GroupsFragment;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.util.HashMap;

public class ChooseGroupActivity extends BaseActivity {

    private String groupsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        //Get groups data
        HashMap<String, HashMap<String, String>> groups = FileManager.fileManager.readFromFileHashMapHash(this, getString(R.string.contact_groups));
        groups.put(getString(R.string.contacts_group), new HashMap<String, String>());
        groups.get(getString(R.string.contacts_group)).put("contacts", "");
        //Take the hash map and turn it into json then add it into data to send later to the fragments on the page
        groupsData = gson.toJson(groups);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_layout, GroupsFragment.newInstance(groupsData));
        fragmentTransaction.commit();

        setTitle("Choose Group");
    }
}
