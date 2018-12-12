package com.potatomasterextreme.personnel.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter;

public class GroupsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerViewAdapter(getArguments().getString("data"), R.layout.group_card_view, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public static GroupsFragment newInstance (String data) {
        GroupsFragment myFragment = new GroupsFragment();

        Bundle args = new Bundle();
        args.putString("data", data);
        myFragment.setArguments(args);

        return myFragment;
    }
}
