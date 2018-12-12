package com.potatomasterextreme.personnel.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.potatomasterextreme.personnel.MainActivity;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.ContactManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;

import java.util.HashMap;

public class EventGroupFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public HashMap<String, HashMap<String, String>> group = new HashMap<>();
    private HashMap<String, HashMap<String, String>> searchGroup;
    public HashMap<String, String> tempGroup;

    Gson gson;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        //Sets on click listener for the tab button
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        gson = ((BaseActivity) getActivity()).gson;

        tempGroup = (gson.fromJson(getActivity().getIntent().getStringExtra("group"), HashMap.class));
        if (tempGroup != null) {
            if (getActivity().getClass() == MainActivity.class) {
                tempGroup = ((MainActivity) getActivity()).messages;
            }
            setGroup();
        }

        // specify an adapter (see also next example)
        if (getActivity().getClass() == MainActivity.class) {
            //group = MessageManager.getLastSMS(getActivity());
            group = FileManager.fileManager.readFromFileHashMapHash(getActivity(), getString(R.string.messages_file) + getString(R.string.data_format));
            mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.incoming_messages_view, getActivity());
            MessageManager.incomingMessageContext = getContext();
        } else {
            mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.contact_card_view, getActivity());
        }
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public void setGroup() {
        HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(getActivity());
        for (String key : tempGroup.keySet()) {
            if (contacts.containsKey(key)) {
                group.put(key, contacts.get(key));
            }
        }
    }

    public void search(String newText) {
        searchGroup = new HashMap<>();
        for (String key : group.keySet()) {
            if (group.get(key).get("name").toLowerCase().contains(newText.toLowerCase())
                    || group.get(key).get("phone").toLowerCase().contains(newText.toLowerCase())) {
                searchGroup.put(key, new HashMap<String, String>());
                searchGroup.get(key).putAll(group.get(key));
            }
        }

        //Reload the adapter
        mAdapter = new RecyclerViewAdapter(gson.toJson(searchGroup), R.layout.contact_card_view, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    public void reload() {
        if (gson != null) {
            mAdapter = new RecyclerViewAdapter(gson.toJson(group), R.layout.contact_card_view, getActivity());
            mRecyclerView.setAdapter(mAdapter);
        }
    }
}
