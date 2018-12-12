package com.potatomasterextreme.personnel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

import com.potatomasterextreme.personnel.MainActivity;
import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.infrastructure.ContactManager;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.listeners.IncomingMessageListener;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;

import java.util.Calendar;
import java.util.HashMap;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO Auto-generated method stub
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    int end;
                    if (msgs.length > 0) {
                        //The receiver need to get only one message from the same phone number
                        end = 1;
                    } else {
                        end = 0;
                    }
                    for (int i = 0; i < end; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = ContactManager.phoneFormat(msgs[i].getOriginatingAddress());
                        String msg_body = msgs[i].getMessageBody();
                        //Confirm for all events if the message says confirm
                        boolean flag = false;
                        if (msg_body.equals("מאשר") || msg_body.toLowerCase().equals("confirm")) {
                            flag = true;
                            //Loop all the events
                            for (String event_id : DataManager.getEvents(context).keySet()) {
                                //Get all the groups from the current event
                                HashMap<String, HashMap<String, String>> groups = DataManager.getGroupsFromFile(context, event_id);
                                HashMap<String, HashMap<String, String>> groupToSave = new HashMap<>();
                                //Loop all the groups of the current event
                                for (String group_name : groups.keySet()) {
                                    //Loop all the phone numbers of the current group
                                    for (String phone_number : groups.get(group_name).keySet()) {
                                        //If the phone number is present then change true to its state
                                        if (phone_number.equals(msg_from)) {
                                            if (!groupToSave.containsKey(group_name)) {
                                                groupToSave.put(group_name, new HashMap<String, String>());
                                            }
                                            groupToSave.get(group_name).put(phone_number, "true");
                                        }
                                    }
                                }
                                //Save the change
                                FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.groups_folder) + event_id + context.getString(R.string.data_format), groupToSave);
                            }
                        }
                        if (!flag) {
                            //Calendar for current time
                            Calendar calendar = Calendar.getInstance();

                            //Get all the incoming messages if there any
                            HashMap<String, HashMap<String, String>> messages = FileManager.fileManager.readFromFileHashMapHash(context, "messages" + context.getString(R.string.data_format));

                            HashMap<String, HashMap<String, String>> message = new HashMap<>();
                            message.put(msg_from, new HashMap<String, String>());

                            HashMap<String, HashMap<String, String>> contacts = ContactManager.getContacts(context);
                            String ntfMessage;
                            String fromName;

                            //Set the name to the title of notification
                            if (contacts.containsKey(msg_from)) {
                                fromName = contacts.get(msg_from).get("name");
                            } else {
                                fromName = msg_from;
                            }

                            //Add count for incoming messages
                            int addSize = 0;
                            if (messages.containsKey(msg_from)) {
                                int count = (Integer.parseInt(messages.get(msg_from).get("msg_count"))) + 1;
                                if (count == 1) {
                                    //The last message was read so we need to update the new one
                                    message.get(msg_from).put("message", msg_body);
                                    message.get(msg_from).put("complete_time", String.valueOf(calendar.getTimeInMillis()));
                                }
                                message.get(msg_from).put("msg_count", count + "");
                                ntfMessage = count + " new messages";
                            } else {
                                message.get(msg_from).put("msg_count", "1");
                                message.get(msg_from).put("message", msg_body);
                                message.get(msg_from).put("phone", msg_from);
                                message.get(msg_from).put("complete_time", String.valueOf(calendar.getTimeInMillis()));

                                ntfMessage = msg_body;
                                addSize++;
                            }

                            Intent mainIntent = new Intent(context, MainActivity.class);
                            intent.putExtra("messages", true);

                            if (messages.size() + addSize > 1) {
                                int sum = 1;
                                int people_count = 0;
                                for (String key : messages.keySet()) {
                                    sum += Integer.parseInt(messages.get(key).get("msg_count"));
                                    if (Integer.parseInt(messages.get(key).get("msg_count")) > 0 || (key.equals(msg_from) && Integer.parseInt(messages.get(key).get("msg_count")) == 0)) {
                                        people_count++;
                                    }
                                }
                                String people;
                                if ((people_count + addSize) > 1) {
                                    people = " " + context.getString(R.string.people);
                                    ntfMessage = context.getString(R.string.message_ntf_msgr_1) + " " + sum + " " + context.getString(R.string.message_ntf_msgr_2) + " " + (people_count + addSize) + people;

                                } else {
                                    people = " " + context.getString(R.string.person);
                                    ntfMessage = context.getString(R.string.message_ntf_msgr_1) + " " + sum + " " + context.getString(R.string.message_ntf_msgr_2) + people;

                                }
                                NotificationMaster.createNotification(context, sum + " " + context.getString(R.string.message_ntf_msgr_3), ntfMessage, NotificationMaster.PRIORITY_LOW, 0, mainIntent);
                            } else {
                                NotificationMaster.createNotification(context, fromName, ntfMessage, NotificationMaster.PRIORITY_LOW, 0, mainIntent);
                            }

                            FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.messages_file) + context.getString(R.string.data_format), message);
                        }
                        update();
                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private void update() {
        //Give some time for the file to save
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    if (MessageManager.incomingMessageContext != null && MessageManager.incomingMessageContext instanceof IncomingMessageListener) {
                        ((IncomingMessageListener) MessageManager.incomingMessageContext).updateMessages();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }
}
