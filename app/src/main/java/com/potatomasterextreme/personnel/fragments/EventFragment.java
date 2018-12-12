package com.potatomasterextreme.personnel.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.potatomasterextreme.personnel.MainActivity;
import com.potatomasterextreme.personnel.adapters.EventsRecyclerViewAdapter;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.contact.ContactActivity;
import com.potatomasterextreme.personnel.contact.ContactAddEventActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;

import java.util.HashMap;

public class EventFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        reload();
        return view;
    }

    HashMap<String, HashMap<String, String>> searchEvents;
    HashMap<String, HashMap<String, String>> events;

    public void search(String newText){
        searchEvents = new HashMap<>();
        for (String key: events.keySet()){
            if(events.get(key).get("name").toLowerCase().contains(newText.toLowerCase())
                    || events.get(key).get("date").toLowerCase().contains(newText.toLowerCase())){
                searchEvents.put(key, new HashMap<String, String>());
                searchEvents.get(key).putAll(events.get(key));
            }
        }

        //Reload the adapter
        mAdapter = new EventsRecyclerViewAdapter(searchEvents, R.layout.event_card_view, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    public void reload(){
        if (getActivity().getClass() == ContactActivity.class){
            events = ((ContactActivity)getActivity()).events;
            mAdapter = new EventsRecyclerViewAdapter(events, R.layout.event_card_view, getActivity());
        } else {
            if (getActivity().getClass() == ContactAddEventActivity.class){
                events = ((ContactAddEventActivity)getActivity()).events;
                mAdapter = new EventsRecyclerViewAdapter(events, R.layout.event_card_view, getActivity());
            } else {
                events = ((MainActivity) getActivity()).events;
                mAdapter = new EventsRecyclerViewAdapter(events, R.layout.event_card_view, getActivity());
            }
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    public static EventFragment newInstance(String data) {
        EventFragment myFragment = new EventFragment();

        Bundle args = new Bundle();
        args.putString("data", data);
        myFragment.setArguments(args);

        return myFragment;
    }
}
