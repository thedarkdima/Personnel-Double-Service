package com.potatomasterextreme.personnel.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.contact.ContactActivity;
import com.potatomasterextreme.personnel.contact.ContactAddEventActivity;
import com.potatomasterextreme.personnel.event.EventActivity;
import com.potatomasterextreme.personnel.group.GroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter.LayoutViewHolder;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<LayoutViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private HashMap<String, HashMap<String, String>> events;
    private String[] sortedDataKeys;
    private Context context;
    private int layout;

    public EventsRecyclerViewAdapter(HashMap<String, HashMap<String, String>> data, int layout, Context context) {
        this.events = data;
        this.layout = layout;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LayoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(context).inflate(layout, parent, false);

        LayoutViewHolder vh = new LayoutViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(LayoutViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.main.findViewById(R.id.main_card).setOnClickListener(this);
        holder.main.findViewById(R.id.main_card).setOnLongClickListener(this);
        holder.main.findViewById(R.id.main_card).setTag(position);

        HashMap<String, String> event = events.get(sortedDataKeys[position]);

        TextView title = holder.main.findViewById(R.id.title);
        TextView desc = holder.main.findViewById(R.id.event_desc);
        TextView time = holder.main.findViewById(R.id.time);
        TextView countMessage = holder.main.findViewById(R.id.workers_count);
        TextView confirmationMessage = holder.main.findViewById(R.id.confirmed_workers);

        if (event.containsKey("name")) {
            title.setText(event.get("name"));
        }
        if (event.containsKey("desc")) {
            desc.setText(event.get("desc"));
        } else {
            desc.setVisibility(View.GONE);
        }

        if (event.containsKey("date")) {
            if (position == 0 || !events.get(String.valueOf(sortedDataKeys[position - 1])).get("date").equals(event.get("date"))) {
                CardView date = holder.main.findViewById(R.id.date_card);
                ((TextView) date.findViewById(R.id.date)).setText(RecyclerViewAdapter.getDate(context, event.get("date"), false));
                date.setVisibility(View.VISIBLE);
            }
        }

        if (event.containsKey("time")) {
            time.setText(event.get("time"));
        } else {
            time.setText("");
        }

        //If the confirmation have started
        boolean started = false;

        if (event.containsKey("id")) {
            ((EditText) holder.main.findViewById(R.id.event_id)).setText(event.get("id"));
            int people_count = 0;
            int confirmed_count = 0;
            DataManager.updateGroups(context, event.get("id"));
            HashMap<String, HashMap<String, String>> groups = DataManager.getGroups(context, event.get("id"));
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
                countMessage.setText(people_count + " " + context.getString(R.string.message_workers_count_1) + " " + neededCount + " " + context.getString(R.string.message_workers_count_2));
                //Set the text color
                if (neededCount / 2 < people_count) {
                    countMessage.setTextColor(ContextCompat.getColor(context, R.color.warningColor));
                }
                if (neededCount / 2 > people_count) {
                    countMessage.setTextColor(ContextCompat.getColor(context, R.color.urgentColor));
                }
                if (neededCount <= people_count) {
                    countMessage.setTextColor(ContextCompat.getColor(context, R.color.goodColor));
                }
            } else {
                countMessage.setText(people_count + " " + context.getString(R.string.message_workers_count_3) + " " + context.getString(R.string.message_workers_count_2));
                countMessage.setTextColor(ContextCompat.getColor(context, R.color.warningColor));
            }

            //Confirmation message
            if (people_count > 0 && started) {
                confirmationMessage.setText(confirmed_count + " " + context.getString(R.string.message_event_confirmation_1) + " " + people_count + " " + context.getString(R.string.message_event_confirmation_2));
                //Set the text color
                if (confirmed_count / 2 < people_count) {
                    confirmationMessage.setTextColor(ContextCompat.getColor(context, R.color.warningColor));
                }
                if (confirmed_count / 2 > people_count) {
                    confirmationMessage.setTextColor(ContextCompat.getColor(context, R.color.urgentColor));
                }
                if (confirmed_count >= people_count) {
                    confirmationMessage.setTextColor(ContextCompat.getColor(context, R.color.goodColor));
                }
            } else {
                confirmationMessage.setVisibility(View.GONE);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        sortedDataKeys = RecyclerViewAdapter.sortByKey(events, "date", true, true);
        return sortedDataKeys.length;
    }

    @Override
    public void onClick(final View v) {
        final Intent intent;
        String extra;
        switch (layout) {
            case R.layout.event_card_view:
                intent = new Intent(context, EventActivity.class);
                //Gets an event from messages with the help of the sorted keys with position
                extra = BaseActivity.gson.toJson(events.get(sortedDataKeys[(int) v.getTag()]));
                intent.putExtra("event", extra);
                if (context.getClass() == ContactAddEventActivity.class) {
                    final String phone = ((ContactAddEventActivity) context).getIntent().getStringExtra("phone");

                    final String[] options = {context.getString(R.string.contact_group), context.getString(R.string.event_group)};
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, options);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.add_to));
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    HashMap<String, HashMap<String, String>> groups = FileManager.fileManager.readFromFileHashMapHash(context, context.getString(R.string.contact_groups));
                                    final List<String> included = new ArrayList<>();
                                    for (String key : groups.keySet()) {
                                        if (groups.get(key).containsKey(phone)) {
                                            included.add(key);
                                        }
                                    }
                                    if (included.size() == 0) {
                                        Toast.makeText(context, context.getString(R.string.message_contact_abssent_in_group), Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (included.size() == 1) {
                                            String name = ((ContactAddEventActivity) context).getIntent().getStringExtra("name");
                                            HashMap<String, HashMap<String, String>> group = new HashMap<>();
                                            group.put(included.get(0), new HashMap<String, String>());
                                            group.get(included.get(0)).put(phone, name);
                                            FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.groups_folder)
                                                    + events.get(sortedDataKeys[(int) v.getTag()]).get("id") + context.getString(R.string.data_format), group);
                                            Toast.makeText(context, "Added " + name + " to " + events.get(sortedDataKeys[(int) v.getTag()]).get("name") + ":" + included.get(0) + " group", Toast.LENGTH_LONG).show();
                                        } else {
                                            final String[] includedArray = new String[included.size()];
                                            included.toArray(includedArray);
                                            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, includedArray);
                                            builder.setTitle("Add to");
                                            builder.setAdapter(adapter1, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    String name = ((ContactAddEventActivity) context).getIntent().getStringExtra("name");
                                                    HashMap<String, HashMap<String, String>> group = new HashMap<>();
                                                    group.put(included.get(i), new HashMap<String, String>());
                                                    group.get(included.get(i)).put(phone, name);
                                                    FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.groups_folder)
                                                            + events.get(sortedDataKeys[(int) v.getTag()]).get("id") + context.getString(R.string.data_format), group);
                                                    Toast.makeText(context, "Added " + name + " to " + events.get(sortedDataKeys[(int) v.getTag()]).get("name") + ":" + included.get(i) + " group", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            builder.create().show();
                                        }
                                        ((ContactAddEventActivity) context).events.remove(sortedDataKeys[(int) v.getTag()]);
                                        ((ContactAddEventActivity) context).eventFragment.reload();
                                    }
                                    break;
                                case 1:
                                    intent.putExtra("add_contact", true);
                                    intent.putExtra("phone", phone);
                                    ((BaseActivity) context).startActivityForResult(intent, 1);
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                    return;
                }
                ((BaseActivity) context).startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (context.getClass() == ContactActivity.class) {
            final AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context);
            final String[] options = {"Remove contact from event"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, options);
            builder.setTitle(events.get(sortedDataKeys[(int) view.getTag()]).get("name"));
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            ContactActivity contactActivity = (ContactActivity) context;
                            String id = sortedDataKeys[(int) view.getTag()];
                            HashMap<String, HashMap<String, String>> groupsToRemove = new HashMap<>();
                            HashMap<String, HashMap<String, String>> groups = DataManager.getGroupsFromFile(context, id);
                            for (String key1 : groups.keySet()) {
                                if (groups.get(key1).containsKey(contactActivity.phone)) {
                                    groupsToRemove.put(key1, null);
                                }
                            }
                            if (groupsToRemove.size() > 0) {
                                FileManager.fileManager.add(context, FileManager.WhatToDo.REMOVE, context.getString(R.string.groups_folder) + id + context.getString(R.string.data_format), groupsToRemove);
                            }
                            contactActivity.events.remove(id);
                            contactActivity.reload();
                            break;
                    }
                }
            });
            builder.create().show();
        }
        return false;
    }
}