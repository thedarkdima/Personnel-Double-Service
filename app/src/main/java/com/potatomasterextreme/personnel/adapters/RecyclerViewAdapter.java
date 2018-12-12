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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.potatomasterextreme.personnel.MainActivity;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.contact.ContactActivity;
import com.potatomasterextreme.personnel.event.ChooseGroupActivity;
import com.potatomasterextreme.personnel.event.CustomMessagesActivity;
import com.potatomasterextreme.personnel.event.EventActivity;
import com.potatomasterextreme.personnel.event.EventGroupActivity;
import com.potatomasterextreme.personnel.group.AddGroupActivity;
import com.potatomasterextreme.personnel.group.GroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.ContactManager;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private HashMap<String, HashMap<String, String>> unpackedData;
    private String[] sortedDataKeys;
    private String data;
    private Context context;
    private int layout;
    private String event_id;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView main;

        public CardViewHolder(CardView v) {
            super(v);
            main = v;
        }
    }

    public static class LayoutViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout main;

        public LayoutViewHolder(LinearLayout v) {
            super(v);
            main = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapter(String data, int layout, Context context) {
        this.data = data;
        this.layout = layout;
        this.context = context;
    }

    public RecyclerViewAdapter(String data, int layout, Context context, String event_id) {
        this.data = data;
        this.layout = layout;
        this.context = context;
        this.event_id = event_id;
    }

    public RecyclerViewAdapter(HashMap<String, HashMap<String, String>> data, int layout, Context context) {
        this.unpackedData = data;
        this.layout = layout;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        //context = parent.getContext();
        //CardView v = (CardView) LayoutInflater.from(context).inflate(R.layout.event_card_view, parent, false);
        CardView v = (CardView) LayoutInflater.from(context)
                .inflate(layout, parent, false);
        //...
        CardViewHolder vh = new CardViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CardViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        holder.main.setOnClickListener(this);
        holder.main.setOnLongClickListener(this);
        holder.main.setTag(position);
        switch (layout) {
            case R.layout.group_card_view:
            case R.layout.event_group_card_view:
                TextView group_name = holder.main.findViewById(R.id.group_name);
                TextView group_count = holder.main.findViewById(R.id.group_count);
                TextView group_status = holder.main.findViewById(R.id.group_status);
                String group_name_text = sortedDataKeys[position];

                int count = 0;
                int confirmed_count = 0;
                if (!sortedDataKeys[position].equals(context.getString(R.string.contacts_group))) {
                    for (String key : unpackedData.get(sortedDataKeys[position]).keySet()) {
                        if (PhoneNumberUtils.isDialable(key.charAt(0))) {
                            count++;
                            if (unpackedData.get(sortedDataKeys[position]).get(key).equals("true")) {
                                confirmed_count++;
                            }
                        }
                    }
                    group_count.setText(count + "");
                } else {
                    group_count.setText(String.valueOf(ContactManager.getContacts(context).size()));
                }

                if (layout == R.layout.event_group_card_view) {
                    if (context.getClass() == EventActivity.class) {
                        if (unpackedData.get(sortedDataKeys[position]).containsKey("group_status")) {
                            if (confirmed_count == count && unpackedData.get(sortedDataKeys[position]).get("group_status").equals("sent")) {
                                HashMap<String, HashMap<String, String>> group = new HashMap<>();
                                group.put(sortedDataKeys[position], new HashMap<String, String>());
                                group.get(sortedDataKeys[position]).put("group_status", "done");
                                FileManager.fileManager.add(context, FileManager.WhatToDo.ADD
                                        , context.getString(R.string.groups_folder) + ((EventActivity) context).event.get("id") + context.getString(R.string.data_format), group);
                                unpackedData.get(sortedDataKeys[position]).put("group_status", "done");
                            }
                        }
                    }

                    if (unpackedData.get(sortedDataKeys[position]).containsKey("group_status")) {
                        switch (unpackedData.get(sortedDataKeys[position]).get("group_status")) {
                            case "sending":
                                group_status.setText(context.getString(R.string.message_group_status_sending));
                                break;
                            case "sent":
                                group_status.setText(context.getString(R.string.message_group_status_sent));
                                group_count.setText(confirmed_count + "/" + count);
                                if (confirmed_count < count / 2) {
                                    group_count.setTextColor(ContextCompat.getColor(context, R.color.urgentColor));
                                }
                                if (confirmed_count > count / 2) {
                                    group_count.setTextColor(ContextCompat.getColor(context, R.color.warningColor));
                                }
                                if (confirmed_count >= count) {
                                    group_count.setTextColor(ContextCompat.getColor(context, R.color.goodColor));
                                }
                                break;
                            case "cancel":
                                group_status.setText(context.getString(R.string.message_group_status_cancel));
                                group_status.setTextColor(ContextCompat.getColor(context, R.color.warningColor));
                                break;
                            case "done":
                                group_count.setText(confirmed_count + "/" + count);
                                group_status.setText(context.getString(R.string.message_group_status_done));
                                group_status.setTextColor(ContextCompat.getColor(context, R.color.goodColor));
                                break;
                        }
                    } else {
                        group_status.setVisibility(View.GONE);
                    }
                }

                //Removes the star from contacts
                if (group_name_text.substring(0, 1).equals("*")) {
                    group_name_text = group_name_text.substring(1, group_name_text.length());
                }
                group_name.setText(group_name_text);
                break;
            case R.layout.contact_card_view:
                TextView name = holder.main.findViewById(R.id.contact_name);
                TextView phone = holder.main.findViewById(R.id.contact_phone);
                TextView message = holder.main.findViewById(R.id.contact_message);

                HashMap<String, String> group = unpackedData.get(sortedDataKeys[position]);

                if (group.containsKey("name")) {
                    name.setText(group.get("name"));
                }
                if (group.containsKey("phone")) {
                    phone.setText(group.get("phone"));
                }
                if (group.containsKey("message")) {
                    message.setText(group.get("message"));
                } else {
                    message.setVisibility(View.GONE);
                }

                if (context.getClass() == EventGroupActivity.class) {
                    EventGroupActivity eventGroupActivity = (EventGroupActivity) context;
                    HashMap<String, HashMap<String, String>> allOutsideGroups = DataManager.getGroups(context, eventGroupActivity.getIntent().getStringExtra("event_id"));
                    HashMap<String, String> outsideGroup = allOutsideGroups.get(eventGroupActivity.groupName);
                    String worker = outsideGroup.get(group.get("phone"));
                    if (worker != null) {
                        if (worker.equals("true")) {
                            TextView confirm = holder.main.findViewById(R.id.confirm);
                            confirm.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            case R.layout.add_contact_card_view:
                TextView add_name = holder.main.findViewById(R.id.contact_name);
                TextView add_phone = holder.main.findViewById(R.id.contact_phone);
                CheckBox add_checkBox = holder.main.findViewById(R.id.contact_checkbox);
                HashMap<String, String> add_group = unpackedData.get(sortedDataKeys[position]);

                if (add_group.containsKey("name")) {
                    add_name.setText(add_group.get("name"));
                }

                if (add_group.containsKey("phone")) {
                    add_phone.setText(add_group.get("phone"));
                }

                add_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (!((AddGroupActivity) context).added.contains(sortedDataKeys[position])) {
                                ((AddGroupActivity) context).added.add(sortedDataKeys[position]);
                            }
                        } else {
                            if (((AddGroupActivity) context).added.contains(sortedDataKeys[position])) {
                                ((AddGroupActivity) context).added.remove(sortedDataKeys[position]);
                            }
                        }
                    }
                });

                if (((AddGroupActivity) context).added.contains(sortedDataKeys[position])) {
                    add_checkBox.setChecked(true);
                } else {
                    add_checkBox.setChecked(false);
                }
                break;
            case R.layout.incoming_messages_view:
                TextView name1 = holder.main.findViewById(R.id.contact_name);
                TextView message1 = holder.main.findViewById(R.id.contact_message);
                TextView date1 = holder.main.findViewById(R.id.date);
                TextView countView = holder.main.findViewById(R.id.message_count);

                HashMap<String, String> incomingMessage = unpackedData.get(sortedDataKeys[position]);
                if (ContactManager.getContacts(context).containsKey(sortedDataKeys[position])) {
                    if (ContactManager.getContacts(context).get(sortedDataKeys[position]).containsKey("name")) {
                        name1.setText(ContactManager.getContacts(context).get(sortedDataKeys[position]).get("name"));
                    } else {
                        name1.setText(sortedDataKeys[position]);
                    }
                } else {
                    name1.setText(sortedDataKeys[position]);
                }

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                if (dateFormat.format(calendar.getTime()).equals(dateFormat.format(Long.parseLong(incomingMessage.get("complete_time"))))) {
                    date1.setText(timeFormat.format(Long.parseLong(incomingMessage.get("complete_time"))));
                } else {
                    date1.setText(getDate(context, dateFormat.format(Long.parseLong(incomingMessage.get("complete_time"))), true));
                }

                if (incomingMessage.containsKey("message")) {
                    message1.setText(incomingMessage.get("message"));
                }

                if (incomingMessage.containsKey("msg_count")) {
                    if (Integer.parseInt(incomingMessage.get("msg_count")) > 0) {
                        countView.setText(incomingMessage.get("msg_count"));
                        countView.setVisibility(View.VISIBLE);
                        date1.setTextColor(ContextCompat.getColor(context, R.color.goodColor));
                    }
                }
                break;
        }
    }

    public static String getDate(Context context, String date, boolean simple) {
        int dayOfWeek = -1;
        try {
            Calendar c = Calendar.getInstance();
            Date theDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);
            c.setTime(theDate);
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dayOfWeek != -1) {
            String back = "";
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            calendar.add(Calendar.DATE, -1);
            String dateNow = dateFormat.format(calendar.getTime());
            if (date.equals(dateNow)) {
                back += context.getString(R.string.yesterday);
            }

            calendar.add(Calendar.DATE, 1);
            dateNow = dateFormat.format(calendar.getTime());
            if (date.equals(dateNow)) {
                back += context.getString(R.string.today);
            }

            calendar.add(Calendar.DATE, 1);
            dateNow = dateFormat.format(calendar.getTime());
            if (date.equals(dateNow)) {
                back += context.getString(R.string.tomorrow);
            }

            calendar.add(Calendar.DATE, 1);
            dateNow = dateFormat.format(calendar.getTime());
            if (date.equals(dateNow)) {
                back += context.getString(R.string.in_two_days);
            }

            if (!simple || back.isEmpty()) {
                if (back.length() > 0) {
                    //Adds space between words if a word really was added, because in english language there no such thing in two days
                    back += " ";
                }
                back += days(context, dayOfWeek) + " " + date;
            }
            return back;
        }


        return date;
    }

    static String days(Context context, int day) {
        switch (day) {
            case 1:
                return context.getString(R.string.day1);
            case 2:
                return context.getString(R.string.day2);
            case 3:
                return context.getString(R.string.day3);
            case 4:
                return context.getString(R.string.day4);
            case 5:
                return context.getString(R.string.day5);
            case 6:
                return context.getString(R.string.day6);
            case 7:
                return context.getString(R.string.day7);
        }
        return "";
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        switch (layout) {
            case R.layout.group_card_view:
            case R.layout.event_group_card_view:
                if (unpackedData == null && data != null) {
                    //Sort unpackedData alphabetically
                    unpackedData = BaseActivity.gson.fromJson(data, FileManager.GetHsHssType());
                    Map<String, HashMap<String, String>> map = new TreeMap<>(unpackedData);
                    sortedDataKeys = new String[map.size()];
                    int count = 0;
                    for (String key : map.keySet()) {
                        sortedDataKeys[count++] = key;
                    }
                }
                break;
            case R.layout.contact_card_view:
            case R.layout.add_contact_card_view:
                if (unpackedData == null && data != null) {
                    unpackedData = BaseActivity.gson.fromJson(data, FileManager.GetHsHssType());
                    sortedDataKeys = sortByKey(unpackedData, "name", true, false);
                }
                break;
            case R.layout.message_view:
                sortedDataKeys = sortByKey(unpackedData, "time", true, false);
                break;
            case R.layout.incoming_messages_view:
                if (unpackedData == null && data != null) {
                    unpackedData = BaseActivity.gson.fromJson(data, FileManager.GetHsHssType());
                    sortedDataKeys = sortByKey(unpackedData, "complete_time", false, false);
                }
                break;
        }
        if (sortedDataKeys != null) {
            return sortedDataKeys.length;
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        String extra;
        switch (layout) {
            case R.layout.group_card_view:
                if (context.getClass() == ChooseGroupActivity.class) {
                    ChooseGroupActivity chooseGroupActivity = (ChooseGroupActivity) context;
                    Intent intent1 = new Intent();
                    if (unpackedData.get(sortedDataKeys[(int) v.getTag()]).containsKey("contacts")) {
                        extra = BaseActivity.gson.toJson(ContactManager.getContacts((Activity) context));
                    } else {
                        extra = BaseActivity.gson.toJson(unpackedData.get(sortedDataKeys[(int) v.getTag()]));
                    }
                    intent1.putExtra("group", extra);

                    chooseGroupActivity.setResult(chooseGroupActivity.RESULT_OK, intent1);
                    chooseGroupActivity.finish();
                    return;
                } else {
                    intent = new Intent(context, GroupActivity.class);
                }
                extra = BaseActivity.gson.toJson(unpackedData.get(sortedDataKeys[(int) v.getTag()]));
                intent.putExtra("group", extra);
                intent.putExtra("group_name", sortedDataKeys[(int) v.getTag()]);
                context.startActivity(intent);
                break;
            case R.layout.add_contact_card_view:
                CheckBox checkBox = v.findViewById(R.id.contact_checkbox);
                checkBox.setChecked(!checkBox.isChecked());
                break;
            case R.layout.event_group_card_view:
                BaseActivity baseActivity = (BaseActivity) context;
                if ((baseActivity.getIntent().getBooleanExtra("add_contact", false))) {
                    HashMap<String, HashMap<String, String>> groupToSave = new HashMap<>();
                    String group_name = sortedDataKeys[(int) v.getTag()];

                    HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(context);
                    groupToSave.put(group_name, new HashMap<String, String>());
                    HashMap<String, String> contact = contacts.get(baseActivity.getIntent().getStringExtra("phone"));
                    groupToSave.get(group_name).put(contact.get("phone"), contact.get("name"));

                    //Save
                    FileManager.fileManager.add(baseActivity, FileManager.WhatToDo.ADD, context.getString(R.string.groups_folder)
                            + event_id + baseActivity.getString(R.string.data_format), groupToSave);

                    intent = new Intent();
                    intent.putExtra("remove_event", event_id);
                    baseActivity.setResult(baseActivity.RESULT_OK, intent);

                    baseActivity.finish();
                } else {
                    intent = new Intent(context, EventGroupActivity.class);
                    extra = BaseActivity.gson.toJson(unpackedData.get(sortedDataKeys[(int) v.getTag()]));
                    intent.putExtra("group", extra);
                    intent.putExtra("event_id", event_id);
                    intent.putExtra("group_name", sortedDataKeys[(int) v.getTag()]);
                    context.startActivity(intent);
                }
                break;
            case R.layout.contact_card_view:
            case R.layout.incoming_messages_view:
                intent = new Intent(context, ContactActivity.class);
                String phone = sortedDataKeys[(int) v.getTag()];
                intent.putExtra("phone", phone);
                HashMap<String, String> contact = ContactManager.getContacts(context).get(phone);
                //No need for to check if contains because if phone is not found in contacts contact will be null
                intent.putExtra("name", contact != null ? contact.get("name") : phone);

                HashMap<String, HashMap<String, String>> messages = FileManager.fileManager.readFromFileHashMapHash(context,
                        "messages" + context.getString(R.string.data_format));
                if (messages.containsKey(sortedDataKeys[(int) v.getTag()])) {
                    //Change the count to 0 = Read
                    HashMap<String, HashMap<String, String>> message = new HashMap<>();
                    message.put(sortedDataKeys[(int) v.getTag()], new HashMap<String, String>());
                    message.get(sortedDataKeys[(int) v.getTag()]).put("msg_count", "0");
                    FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.messages_file) + context.getString(R.string.data_format), message);
                }
                context.startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        switch (layout) {
            case R.layout.contact_card_view:
                if (context.getClass() == GroupActivity.class) {
                    if (((GroupActivity) context).tempGroup.containsKey("contacts")) {
                        return false;
                    }
                    final AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(context);
                    final String[] options = {context.getString(R.string.message_remove_contact)};
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, options);
                    builder.setTitle(unpackedData.get(sortedDataKeys[(int) view.getTag()]).get("name"));
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    //Get group name
                                    GroupActivity groupActivity = (GroupActivity) context;
                                    String group_name = groupActivity.groupName;
                                    //Set to remove
                                    HashMap<String, HashMap<String, String>> contactToRemove = new HashMap<>();
                                    contactToRemove.put(group_name, new HashMap<String, String>());
                                    contactToRemove.get(group_name).put(sortedDataKeys[(int) view.getTag()], null);
                                    //Remove
                                    FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.contact_groups), contactToRemove);
                                    groupActivity.tempGroup.remove(sortedDataKeys[(int) view.getTag()]);

                                    groupActivity.reload();

                                    break;
                            }
                        }
                    });
                    builder.create().show();
                } else {
                    final AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(context);
                    final String[] options = {context.getString(R.string.message_remove_contact)};
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, options);
                    builder.setTitle(unpackedData.get(sortedDataKeys[(int) view.getTag()]).get("name"));
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    //Get group name
                                    EventGroupActivity groupActivity = (EventGroupActivity) context;
                                    String group_name = groupActivity.groupName;
                                    //Set to remove
                                    HashMap<String, HashMap<String, String>> contactToRemove = new HashMap<>();
                                    contactToRemove.put(group_name, new HashMap<String, String>());
                                    contactToRemove.get(group_name).put(sortedDataKeys[(int) view.getTag()], null);
                                    //Remove
                                    FileManager.fileManager.add(groupActivity, FileManager.WhatToDo.REMOVE_INSIDE, groupActivity.getString(R.string.groups_folder)
                                            + groupActivity.getIntent().getStringExtra("event_id") + groupActivity.getString(R.string.data_format), contactToRemove);

                                    DataManager.getGroups(context, groupActivity.getIntent().getStringExtra("event_id")).get(group_name).remove(sortedDataKeys[(int) view.getTag()]);
                                    groupActivity.eventGroupFragment.group.remove(sortedDataKeys[(int) view.getTag()]);
                                    groupActivity.eventGroupFragment.reload();

                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
                break;
        }
        return false;
    }

    public static String[] sortByKey(HashMap<String, HashMap<String, String>> map, String secondKey, boolean ascending, boolean date) {
        if (date) {
            return sortDateByKey(map, secondKey, ascending);
        } else {
            return sortStringByKey(map, secondKey, ascending);
        }
    }

    private static String[] sortDateByKey(HashMap<String, HashMap<String, String>> map, String secondKey, boolean ascending) {
        //Takes date format and sort by its value then changes it back to string from long and returns it
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<Long> backDates = new ArrayList<>();
        String[] back = new String[map.size()];

        HashMap<Long, List<String>> toSort = new HashMap<>();

        int count = 0;
        for (String key : map.keySet()) {
            try {
                //Saves all the dates to hashmap and to array
                Date date = dateFormat.parse(map.get(key).get(secondKey));
                if (!backDates.contains(date.getTime())) {
                    backDates.add(date.getTime());
                }
                if (!toSort.containsKey(date.getTime())) {
                    toSort.put(date.getTime(), new ArrayList<String>());
                }
                toSort.get(date.getTime()).add(key);
            } catch (ParseException e) {
                e.printStackTrace();
                return new String[]{};
            }
        }

        //sorting
        if (ascending) {
            Collections.sort(backDates);
        } else {
            Collections.sort(backDates, Collections.reverseOrder());
        }

        //returning all the data back to string array
        count = 0;
        for (Long date : backDates) {
            for (String key1 : toSort.get(date)) {
                back[count++] = key1;
            }
        }

        return back;
    }

    private static String[] sortStringByKey(HashMap<String, HashMap<String, String>> map, String secondKey, boolean ascending) {
        String[] backKeys = new String[map.size()];

        //Get all the wanted values from the hash map
        HashMap<String, List<String>> toSort = new HashMap<>();
        for (String key : map.keySet()) {
            String newKey = map.get(key).get(secondKey);
            if (toSort.get(newKey) == null) {
                toSort.put(newKey, new ArrayList<String>());
            }
            toSort.get(newKey).add(key);
        }

        //Sort all the keys by value
        Map<String, List<String>> treeMap = new TreeMap<>(toSort);
        int count = 0;
        if (ascending) {
            for (String key : treeMap.keySet()) {
                for (String value : treeMap.get(key)) {
                    backKeys[count++] = value;
                }
            }
        } else {
            count = treeMap.size() - 1;
            for (String key : treeMap.keySet()) {
                for (String value : treeMap.get(key)) {
                    backKeys[count--] = value;
                }
            }
        }

        return backKeys;
    }
}