package com.potatomasterextreme.personnel.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.potatomasterextreme.personnel.adapters.MessagesRecyclerViewAdapter;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.contact.ContactActivity;
import com.potatomasterextreme.personnel.contact.ContactAddEventActivity;
import com.potatomasterextreme.personnel.event.EventGroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MessagesFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    EditText chatEditText;
    HashMap<String, HashMap<String, String>> messages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages, container, false);

        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        //Add
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);

        // use a linear layout manager
        mLayoutManager = layoutManager;
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (getActivity().getClass() == ContactActivity.class) {
            messages = MessageManager.getMessagesOf(getActivity(), getActivity().getIntent().getStringExtra("phone"));
        } else {
            if (getActivity().getClass() == ContactAddEventActivity.class) {
                messages = MessageManager.getMessagesOf(getActivity(), getActivity().getIntent().getStringExtra("phone"));
            } else {
                messages = FileManager.fileManager.readFromFileHashMapHash(getActivity(), "messages/" + getActivity().getIntent().getStringExtra("event_id")
                        + ((EventGroupActivity) getActivity()).groupName + getString(R.string.data_format));
            }
        }

        // specify an adapter (see also next example)
        mAdapter = new MessagesRecyclerViewAdapter(messages, R.layout.message_view, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        view.findViewById(R.id.activity_chat_send_image_view).setOnClickListener(this);
        chatEditText = view.findViewById(R.id.activity_chat_message_edit_text);

        final View mRootView = getActivity().findViewById(R.id.main_layout);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                mRootView.getWindowVisibleDisplayFrame(measureRect);
                // measureRect.bottom is the position above soft keypad
                int keypadHeight = mRootView.getRootView().getHeight() - measureRect.bottom;

                BaseActivity baseActivity = (BaseActivity) getActivity();
                if (baseActivity != null) {
                    if (keypadHeight > mRootView.getRootView().getHeight() / 3) {
                        // keyboard is opened
                        if (baseActivity.isOnMessanger) {
                            baseActivity.hideTitleBar();
                        }
                    } else {
                        //store keyboard state to use in onBackPress if you need to
                        baseActivity.showTitleBar();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        if (chatEditText.getText().toString().trim().length() > 0) {
            Calendar calendar = Calendar.getInstance();
            String key = String.valueOf(calendar.getTimeInMillis());

            HashMap<String, HashMap<String, String>> message = new HashMap<>();
            message.put(key, new HashMap<String, String>());
            message.get(key).put("message", chatEditText.getText().toString());

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            message.get(key).put("time", timeFormat.format(calendar.getTime()));
            message.get(key).put("date", dateFormat.format(calendar.getTime()));
            message.get(key).put("type", "sent");

            messages.putAll(message);

            mAdapter.notifyItemInserted(messages.size());
            mRecyclerView.smoothScrollToPosition(messages.size());

            HashMap<String, HashMap<String, String>> sentTo;

            if (getActivity().getClass() == EventGroupActivity.class) {
                FileManager.fileManager.add(getActivity(), FileManager.WhatToDo.ADD, "messages/" + getActivity().getIntent().getStringExtra("event_id")
                        + ((EventGroupActivity) getActivity()).groupName + getString(R.string.data_format), message);
                sentTo = ((EventGroupActivity) getActivity()).eventGroupFragment.group;
            } else {
                sentTo = new HashMap<>();
                sentTo.put(getActivity().getIntent().getStringExtra("phone"), null);
            }

            //Send the messages for all the people in the group
            //Shouldn't be null in the app but happens few times in debug mode
            if (sentTo != null) {
                for (String key1 : sentTo.keySet()) {
                    MessageManager.addSms(getActivity(), key1, chatEditText.getText().toString());
                }
                Toast.makeText(getActivity(), getActivity().getString(R.string.message_sent_shortly), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), "Error: 123 call the mothership for repairs.", Toast.LENGTH_SHORT).show();
            }

            //Empty the chat box
            chatEditText.setText("");
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.message_must_not_be_empty), Toast.LENGTH_SHORT).show();
        }
    }
}
