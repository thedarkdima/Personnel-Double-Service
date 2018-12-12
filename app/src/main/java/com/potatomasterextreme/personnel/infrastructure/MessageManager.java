package com.potatomasterextreme.personnel.infrastructure;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.event.CustomMessagesActivity;
import com.potatomasterextreme.personnel.listeners.IncomingMessageListener;
import com.potatomasterextreme.personnel.listeners.OutgoingMessageListener;
import com.potatomasterextreme.personnel.services.MessagesJobService;
import com.potatomasterextreme.personnel.services.MessagesSendJobService;
import com.potatomasterextreme.personnel.services.MessagesSendService;
import com.potatomasterextreme.personnel.services.MessagesService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MessageManager {

    //Current activity that using messages, I would say context but I will use this manager only with 3 activity's
    //Contexts for listeners
    public static Context incomingMessageContext;
    public static Context outgoingMessageContext;

    public static HashMap<String, HashMap<String, String>> getLastSMS(Context context) {
        //That was sent to the phone only
        incomingMessageContext = context;

        HashMap<String, HashMap<String, String>> messages = new HashMap<>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    //Gets the phone number and sorts it into the correct format
                    String phone = ContactManager.phoneFormat(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                    Date date = new Date(Long.valueOf(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))));
                    //Check if message from the same phone number already in the messages if yes compare their time and put the message the came later only
                    if (messages.containsKey(phone) && Long.parseLong(messages.get(phone).get("complete_time")) > date.getTime()) {
                    } else {
                        switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                                String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                                if (!body.equals("מאשר") && !body.equals("confirm")) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                                    messages.put(phone, new HashMap<String, String>());
                                    messages.get(phone).put("message", body);
                                    messages.get(phone).put("time", timeFormat.format(date));
                                    messages.get(phone).put("date", dateFormat.format(date));
                                    messages.get(phone).put("datetime", dateFormat.format(date) + " " + timeFormat.format(date));
                                    messages.get(phone).put("complete_time", String.valueOf(date.getTime()));
                                    messages.get(phone).put("type", "inbox");
                                }
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT:
                                //messages.get(phone).put("type", "sent");
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                                //messages.get(phone).put("type", "outbox");
                                break;
                            default:
                                break;
                        }
                    }

                    c.moveToNext();
                }
            }
            c.close();
        } else {
            Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show();
        }

        return messages;
    }

    public static HashMap<String, HashMap<String, String>> getMessagesOf(Context context, String phoneNumber) {
        incomingMessageContext = context;

        HashMap<String, HashMap<String, String>> messages = new HashMap<>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    //Gets the phone number and sorts it into the correct format
                    String phoneFull = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    if (phoneFull == null) {
                        phoneFull = "";
                    }
                    //Bug fix sometimes phone number cannot be found
                    String phone = ContactManager.phoneFormat(phoneFull);
                    //Check if message from the same phone number already in the messages if yes compare their time and put the message the came later only
                    if (phoneNumber.equals(phone)) {
                        Date date = new Date(Long.valueOf(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))));
                        String completeTime = String.valueOf(date.getTime());

                        messages.put(completeTime, new HashMap<String, String>());
                        messages.get(completeTime).put("phone", phone);
                        messages.get(completeTime).put("message", c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)));

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                        messages.get(completeTime).put("time", timeFormat.format(date));
                        messages.get(completeTime).put("date", dateFormat.format(date));
                        messages.get(completeTime).put("datetime", dateFormat.format(date) + " " + timeFormat.format(date));

                        switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                                messages.get(completeTime).put("type", "inbox");
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT:
                            case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                                messages.get(completeTime).put("type", "sent");
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                                messages.get(completeTime).put("type", "outbox");
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_FAILED:
                                messages.remove(completeTime);
                                break;
                        }
                    }

                    c.moveToNext();

                }
            }
            c.close();
        } else {
            Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show();
        }

        return messages;
    }

    static public void addSms(Context context, String phoneNo, String msg) {
        if (msg.trim().length() > 0) {
            HashMap<String, HashMap<String, String>> outGoingMessage = new HashMap<>();
            String time = String.valueOf(Calendar.getInstance().getTimeInMillis());
            outGoingMessage.put(time, new HashMap<String, String>());
            outGoingMessage.get(time).put("phone", phoneNo);
            outGoingMessage.get(time).put("message", msg);
            FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.sending_messages) + context.getString(R.string.data_format), outGoingMessage);

            //Intent broadcastIntent = new Intent(context, MessagesSendServiceReceiver.class);
            //context.sendBroadcast(broadcastIntent);

            SharedPreferences preferences;
            preferences = context.getSharedPreferences("PREFS_FILE_NAME", context.MODE_PRIVATE);

            long sleep_time = 0;
            if (preferences.contains("sleep_time")) {
                sleep_time = preferences.getLong("sleep_time", 0) - Long.valueOf(time);
            }

            context.stopService(new Intent(context, MessagesSendService.class));
            MessageManager.startMessageSendService(context, MessageManager.SEND_JOB_ID, sleep_time);
        }
    }

    public final static int WAIT_TIME = 70000;

    static public boolean startSendingSms(Context context) {
        boolean back = false;
        HashMap<String, HashMap<String, String>> messages = FileManager.fileManager.readFromFileHashMapHash(context,
                context.getString(R.string.sending_messages) + context.getString(R.string.data_format));
        SharedPreferences preferences = context.getSharedPreferences("PREFS_FILE_NAME", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean update = false;
        for (String key : messages.keySet()) {
            Long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime > Long.valueOf(key) && preferences.getLong("sleep_time", 0L) < currentTime) {
                //If more than a minute passed change reset the counter
                if (preferences.getLong("sleep_time", 0L) + WAIT_TIME < currentTime) {
                    editor.putLong("message_sent_count", 0L);
                }
                if (!increaseMessageSentCount(context, 1)) {
                    int sent_failed = 0;
                    if (messages.get(key).containsKey("sent_failed")) {
                        sent_failed = Integer.valueOf(messages.get(key).get("sent_failed"));
                    }
                    if (sent_failed < 100) {
                        sendSMS(context, key, messages.get(key).get("phone"), messages.get(key).get("message"), sent_failed);
                        update = true;
                    }
                } else {
                    back = true;
                }
            } else {
                back = true;
            }
        }
        if (update) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        if (MessageManager.outgoingMessageContext != null && MessageManager.outgoingMessageContext instanceof OutgoingMessageListener) {
                            ((OutgoingMessageListener) MessageManager.outgoingMessageContext).updateMessages();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).run();
        }
        return back;
    }

    static private boolean sendSMS(Context context, String time, String phoneNo, String msg, int sendFailed) {
        //Still not done need to add more stuff, like a safe switch if a msg failed to sent
        boolean back = false;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            back = true;
        } catch (Exception ex) {
            Log.d("Tag Error:", ex.toString());
            HashMap<String, HashMap<String, String>> outGoingMessage = new HashMap<>();
            //Adds penalty time to the failed text that way it won't spam the out sending messages
            int penaltyTime = 30000;
            String currentTime = String.valueOf(Calendar.getInstance().getTimeInMillis() + penaltyTime);
            outGoingMessage.put(currentTime, new HashMap<String, String>());
            outGoingMessage.get(currentTime).put("phone", phoneNo);
            outGoingMessage.get(currentTime).put("message", msg);
            outGoingMessage.get(currentTime).put("sent_failed", String.valueOf(sendFailed + 1));
            if (sendFailed + 1 == 100) {
                //Must add thingy here
                Intent intent = new Intent(context, CustomMessagesActivity.class);
                intent.putExtra("outgoing", true);

                NotificationMaster.createNotification(context, context.getString(R.string.notification_title_failed_send_message),
                        context.getString(R.string.notification_message_failed_send_message), NotificationMaster.PRIORITY_MAX, 5, intent);
            }
            FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.sending_messages) + context.getString(R.string.data_format), outGoingMessage);
        }
        //Removes the message
        HashMap<String, HashMap<String, String>> remove = new HashMap<>();
        remove.put(time, new HashMap<String, String>());
        FileManager.fileManager.add(context, FileManager.WhatToDo.REMOVE, context.getString(R.string.sending_messages) + context.getString(R.string.data_format), remove);

        return back;
    }

    public static final int JOB_ID = 1;
    public static final int SEND_JOB_ID = 2;


    //Service stuff
    static public void startMessageService(Context context, int job_id) {
        //Run messages service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isMyJobServiceRunning(context, job_id)) {
            int delay = 60000;
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(new JobInfo.Builder(job_id, new ComponentName(context, MessagesJobService.class))
                    .setMinimumLatency(delay)
                    .setOverrideDeadline(delay * 2)
                    .build());
            }
        } else {
            MessagesService messagesService = new MessagesService();
            Intent messagesServiceIntent = new Intent(context, messagesService.getClass());
            if (!isMyServiceRunning(context, messagesService.getClass())) {
                context.startService(messagesServiceIntent);
            }
        }
    }

    //Service stuff
    static public void forceStartMessageService(Context context, int job_id) {
        //Run messages service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //if (!isMyJobServiceRunning(context, job_id)) {
                int delay = 60000;
                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                jobScheduler.schedule(new JobInfo.Builder(job_id, new ComponentName(context, MessagesJobService.class))
                        .setMinimumLatency(delay)
                        .setOverrideDeadline(delay * 2)
                        .build());
            //}
        } else {
            MessagesService messagesService = new MessagesService();
            Intent messagesServiceIntent = new Intent(context, messagesService.getClass());
            context.startService(messagesServiceIntent);
        }
    }

    static public void startMessageSendService(Context context, int job_id, Long sleep_time) {
        if (sleep_time < 0) {
            sleep_time = 0L;
        }
        //Run messages service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(new JobInfo.Builder(job_id, new ComponentName(context, MessagesSendJobService.class))
                    .setMinimumLatency(sleep_time)
                    .setOverrideDeadline(sleep_time * 2)
                    .build());
        } else {
            MessagesSendService messagesService = new MessagesSendService();
            Intent messagesServiceIntent = new Intent(context, messagesService.getClass());
            messagesServiceIntent.putExtra("sleep_time", sleep_time);
            context.startService(messagesServiceIntent);
        }
    }

    static public boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static public boolean isMyJobServiceRunning(Context context, int job_id) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;

        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == job_id) {
                hasBeenScheduled = true;
                break;
            }
        }

        return hasBeenScheduled;
    }

    static public void Main(Context context) {
        HashMap<String, HashMap<String, String>> events = DataManager.getEvents(context);
        for (String key : events.keySet()) {
            HashMap<String, HashMap<String, String>> groups = DataManager.getGroupsFromFile(context, events.get(key).get("id"));
            for (String key1 : groups.keySet()) {
                if (groups.get(key1).containsKey("group_status")) {
                    sendConfirmationMessage(context, events, groups, key, key1);
                    sendReminders(context, groups, key, key1);
                }
            }
        }
    }

    static private void sendConfirmationMessage(Context context, HashMap<String, HashMap<String, String>> events, HashMap<String, HashMap<String, String>> groups, String key, String key1) {
        if (groups.get(key1).get("group_status").equals("sending")) {
            //Check if the group have designated confirmation message to send
            Date currentTime = Calendar.getInstance().getTime();
            if (currentTime.getTime() > Long.parseLong(groups.get(key1).get("send_date"))) {
                //Check if the message send date have passed and only then send the message
                for (String key2 : groups.get(key1).keySet()) {
                    if (PhoneNumberUtils.isDialable(key2.charAt(0))) {
                        //Send message
                        MessageManager.addSms(context, key2, groups.get(key1).get("group_message"));
                    }
                }
                //Change group status to sent
                HashMap<String, HashMap<String, String>> group = new HashMap<>();
                group.put(key1, new HashMap<String, String>());
                group.get(key1).put("group_status", "sent");
                FileManager.fileManager.add(context, FileManager.WhatToDo.ADD
                        , context.getString(R.string.groups_folder) + key + context.getString(R.string.data_format), group);
                //Add the message to the group chat
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                //Create message
                String currentTimeStr = String.valueOf(currentTime.getTime());
                HashMap<String, HashMap<String, String>> message = new HashMap<>();
                message.put(currentTimeStr, new HashMap<String, String>());
                message.get(currentTimeStr).put("message", groups.get(key1).get("group_message"));
                message.get(currentTimeStr).put("time", timeFormat.format(currentTime.getTime()));
                message.get(currentTimeStr).put("date", dateFormat.format(currentTime.getTime()));
                message.get(currentTimeStr).put("type", "sent");

                FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.messages_folder) + key
                        + key1 + context.getString(R.string.data_format), message);

                //NotificationMaster.createNotification(context, "Service", "Confirmation message for event " + events.get(key).get("name") + " was sent", NotificationMaster.PRIORITY_DEFAULT, 5);
            }
        }
    }

    static private void sendReminders(Context context, HashMap<String, HashMap<String, String>> groups, String key, String key1) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 23 || (hour > 0 && hour < 8)){
            //Won't send messages after midnight till 8 am
            return;
        }
        if (groups.get(key1).get("group_status").equals("sent")) {
            //Check if the group have designated confirmation message to send
            Date currentTime = calendar.getTime();
            if (groups.get(key1).containsKey("next_reminder")) {
                if (currentTime.getTime() > Long.parseLong(groups.get(key1).get("next_reminder")) && Integer.parseInt(groups.get(key1).get("remind_every")) != 0) {
                    //Check if the message next reminder have passed and only then send the message
                    //Get random reminder but not the one that was before
                    String lastReminder = null;
                    if (groups.get(key1).containsKey("last_reminder")) {
                        lastReminder = groups.get(key1).get("last_reminder");
                    }
                    String textMessage = DataManager.getRandomReminder(context, lastReminder);
                    if (textMessage == null) {
                        //If there no reminder to send abort
                        return;
                    }
                    boolean send = false;
                    for (String key2 : groups.get(key1).keySet()) {
                        if (PhoneNumberUtils.isDialable(key2.charAt(0))) {
                            //Send message
                            if (!groups.get(key1).get(key2).equals("true")) {
                                MessageManager.addSms(context, key2, textMessage);
                                send = true;
                            }
                        }
                    }
                    if (send) {
                        //Get the current time and add to it the remind every hours
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(currentTime);
                        cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(groups.get(key1).get("remind_every")));
                        //Then change the next reminder with it
                        HashMap<String, HashMap<String, String>> group = new HashMap<>();
                        group.put(key1, new HashMap<String, String>());
                        group.get(key1).put("next_reminder", String.valueOf(cal.getTimeInMillis()));
                        group.get(key1).put("last_reminder", textMessage);
                        FileManager.fileManager.add(context, FileManager.WhatToDo.ADD
                                , context.getString(R.string.groups_folder) + key + context.getString(R.string.data_format), group);
                        //Add the message to the group chat
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        //Create message
                        String currentTimeStr = String.valueOf(currentTime.getTime());
                        HashMap<String, HashMap<String, String>> message = new HashMap<>();
                        message.put(currentTimeStr, new HashMap<String, String>());
                        message.get(currentTimeStr).put("message", textMessage);
                        message.get(currentTimeStr).put("time", timeFormat.format(currentTime.getTime()));
                        message.get(currentTimeStr).put("date", dateFormat.format(currentTime.getTime()));
                        message.get(currentTimeStr).put("type", "sent");

                        FileManager.fileManager.add(context, FileManager.WhatToDo.ADD, context.getString(R.string.messages_folder) + key
                                + key1 + context.getString(R.string.data_format), message);
                    } else {
                        //If all the people have confirmed their attendance then change the group status to done
                        HashMap<String, HashMap<String, String>> group = new HashMap<>();
                        group.put(key1, new HashMap<String, String>());
                        group.get(key1).put("group_status", "done");
                        FileManager.fileManager.add(context, FileManager.WhatToDo.ADD
                                , context.getString(R.string.groups_folder) + key + context.getString(R.string.data_format), group);
                    }
                    //NotificationMaster.createNotification(context, "Service", "Reminder was sent", NotificationMaster.PRIORITY_MAX, 5);
                }
            }
        }
    }

    public static boolean increaseMessageSentCount(Context context, int by) {
        SharedPreferences.Editor editor;
        SharedPreferences preferences;
        preferences = context.getSharedPreferences("PREFS_FILE_NAME", context.MODE_PRIVATE);
        editor = preferences.edit();
        int count = preferences.getInt("message_sent_count", 0);
        if (count + by > 30) {
            editor.putInt("message_sent_count", 0);
            editor.putLong("sleep_time", Calendar.getInstance().getTimeInMillis() + WAIT_TIME);
            editor.commit();
            return true;
        } else {
            editor.putInt("message_sent_count", count + by);
            editor.commit();
            return false;
        }
    }
}
