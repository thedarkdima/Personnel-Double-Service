package com.potatomasterextreme.personnel.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.event.EventActivity;
import com.potatomasterextreme.personnel.group.GroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.adapters.RecyclerViewAdapter.LayoutViewHolder;


import java.util.Arrays;
import java.util.HashMap;

public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<LayoutViewHolder> implements View.OnLongClickListener {

    private HashMap<String, HashMap<String, String>> messages;
    private Long[] sortedDataKeys;
    private Context context;
    private int layout;

    public MessagesRecyclerViewAdapter(HashMap<String, HashMap<String, String>> data, int layout, Context context) {
        this.messages = data;
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
        RelativeLayout relativeLayout;

        holder.main.setOnLongClickListener(this);
        holder.main.setTag(position);

        final HashMap<String, String> message = messages.get(String.valueOf(sortedDataKeys[position]));
        boolean isIncomingMessage = message.containsKey("type") ? ((message.get("type") == "inbox") ? false : true) : false;
        //View view = View.inflate(context,isIncomingMessage ? R.layout.message_in : R.layout.message_out, null );
        //View view = ((EventGroupActivity) context).getLayoutInflater().inflate(isIncomingMessage ? R.layout.message_in : R.layout.message_out, null);

        if (!isIncomingMessage) {
            relativeLayout = holder.main.findViewById(R.id.message_out);
            String name = ((Activity) context).getIntent().getStringExtra("name");
            String phone = message.get("phone");
            ((TextView) relativeLayout.findViewById(R.id.message_text_name)).setText(name);
            if (name == null || !phone.equals(name)) {
                ((TextView) relativeLayout.findViewById(R.id.message_text_phone)).setText(phone);
            }

        } else {
            relativeLayout = holder.main.findViewById(R.id.message_in);
        }
        relativeLayout.setVisibility(View.VISIBLE);

        if (position == 0 || !messages.get(String.valueOf(sortedDataKeys[position - 1])).get("date").equals(message.get("date"))) {
            RelativeLayout date = holder.main.findViewById(R.id.message_date);
            ((TextView) date.findViewById(R.id.message_text_view)).setText(RecyclerViewAdapter.getDate(context, message.get("date"), true));
            date.setVisibility(View.VISIBLE);
        }

        TextView messageTextView = relativeLayout.findViewById(R.id.message_text_view);
        TextView time = relativeLayout.findViewById(R.id.message_text_time);

        //Adds backspace to the message that way the it will create a new line and move the time lower a line and will not go over it
        messageTextView.setText(message.get("message"));
        // + "          \b");
        time.setText(message.get("time"));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        sortedDataKeys = new Long[messages.size()];
        int count = 0;
        for (String key : messages.keySet()) {
            sortedDataKeys[count++] = Long.parseLong(key.trim());
        }
        Arrays.sort(sortedDataKeys);

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

    /*@Override
    public void onClick(View v) {
        Intent intent;
        String extra;
        switch (layout) {
            case R.layout.event_card_view:
                intent = new Intent(context, EventActivity.class);
                //Gets an event from messages with the help of the sorted keys with position
                extra = BaseActivity.gson.toJson(messages.get(sortedDataKeys[(int) v.getTag()]));
                intent.putExtra("event", extra);
                context.startActivity(intent);
                break;
            case R.layout.group_card_view:
                intent = new Intent(context, GroupActivity.class);
                extra = BaseActivity.gson.toJson(messages.get(sortedDataKeys[(int) v.getTag()]));
                intent.putExtra("group", extra);
                intent.putExtra("group_name", sortedDataKeys[(int) v.getTag()]);
                context.startActivity(intent);
                break;
        }
    }*/

    @Override
    public boolean onLongClick(final View view) {
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        final String[] options = {"Copy"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, options);
        builder.setTitle(context.getString(R.string.message));
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Message", messages.get(String.valueOf(sortedDataKeys[(int) view.getTag()])).get("message"));
                        clipboard.setPrimaryClip(clip);
                        break;
                }
            }
        });
        builder.create().show();
        return true;
    }
}