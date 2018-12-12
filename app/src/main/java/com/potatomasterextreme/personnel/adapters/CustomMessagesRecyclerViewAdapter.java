package com.potatomasterextreme.personnel.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter.CardViewHolder;
import com.potatomasterextreme.personnel.event.CustomMessagesActivity;
import com.potatomasterextreme.personnel.event.EventGroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CustomMessagesRecyclerViewAdapter extends RecyclerView.Adapter<CardViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private HashMap<String, HashMap<String, String>> messages;
    private String[] sortedDataKeys;
    private Context context;
    private int layout;

    public CustomMessagesRecyclerViewAdapter(HashMap<String, HashMap<String, String>> data, int layout, Context context) {
        this.messages = data;
        this.layout = layout;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(context).inflate(layout, parent, false);
        CardViewHolder vh = new CardViewHolder(v);
        return vh;
    }

    TextView textView;

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CardViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        RelativeLayout relativeLayout;

        textView = holder.main.findViewById(R.id.message_text_view);

        if (((CustomMessagesActivity)context).outgoing){
            String text = messages.get(sortedDataKeys[position]).get("message") + "\n\n" + "To: " + messages.get(sortedDataKeys[position]).get("phone");
            if (messages.get(sortedDataKeys[position]).containsKey("sent_failed")){
                text += "\nNumber Of Tries: " + messages.get(sortedDataKeys[position]).get("sent_failed");
            }
            textView.setText(text);

        } else {
            holder.main.setOnClickListener(this);
            holder.main.setOnLongClickListener(this);
            holder.main.setTag(position);
            textView.setText(sortedDataKeys[position]);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Map<String, HashMap<String, String>> treeMap = new TreeMap<>(messages);
        sortedDataKeys = new String[messages.size()];
        int count = 0;
        for (String key : treeMap.keySet()) {
            sortedDataKeys[count++] = key;
        }

        if (sortedDataKeys != null) {
            return sortedDataKeys.length;
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("message", ((TextView) v.findViewById(R.id.message_text_view)).getText().toString());
        BaseActivity baseActivity = ((BaseActivity) context);
        baseActivity.setResult(baseActivity.RESULT_OK, intent);
        baseActivity.finish();
    }

    @Override
    public boolean onLongClick(final View view) {
        final CustomMessagesActivity customMessagesActivity = (CustomMessagesActivity)context;
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        final String[] options = {context.getString(R.string.message_remove_contact)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, options);
        builder.setTitle("Message");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        HashMap<String, HashMap<String, String>> message = new HashMap<>();
                        message.put(sortedDataKeys[(int) view.getTag()], new HashMap<String, String>());
                        if (!customMessagesActivity.reminders) {
                            FileManager.fileManager.add(customMessagesActivity, FileManager.WhatToDo.REMOVE,
                                    customMessagesActivity.getString(R.string.custom_folder) +
                                            customMessagesActivity.getIntent().getStringExtra("name") +
                                            customMessagesActivity.getString(R.string.custom_messages_file), message);
                        } else {
                            FileManager.fileManager.add(customMessagesActivity, FileManager.WhatToDo.REMOVE
                                    , customMessagesActivity.getString(R.string.reminders)
                                            + customMessagesActivity.getString(R.string.data_format), message);
                        }
                        messages.putAll(message);
                        customMessagesActivity.reload();
                        break;
                }
            }
        });
        builder.create().show();


        return true;
    }
}