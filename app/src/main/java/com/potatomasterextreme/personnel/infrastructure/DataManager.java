package com.potatomasterextreme.personnel.infrastructure;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.Nullable;

import com.potatomasterextreme.personnel.R;

import java.util.HashMap;
import java.util.Random;

public class DataManager {

    //Events
    private static HashMap<String, HashMap<String, String>> events;

    public static HashMap<String, HashMap<String, String>> getEvents(Context context) {
        if (events == null) {
            updateEvents(context);
        }
        return events;
    }

    public static void updateEvents(Context context) {
        events = FileManager.fileManager.readFromFileHashMapHash(context, context.getString(R.string.events_file));
    }

    //Event Groups
    private static HashMap<String,HashMap<String, HashMap<String, String>>> groups = new HashMap<>();

    public static HashMap<String, HashMap<String, String>> getGroups(Context context, String eventID) {
        if (groups == null || !groups.containsKey(eventID)) {
            updateGroups(context, eventID);
        }
        if (!groups.containsKey(eventID)){
            groups.put(eventID, new HashMap<String, HashMap<String, String>>());
        }
        return groups.get(eventID);
    }

    public static void updateGroups(Context context, String eventID) {
        groups.put(eventID, new HashMap<String, HashMap<String, String>>());
        groups.get(eventID).putAll(FileManager.fileManager.readFromFileHashMapHash(context, context.getString(R.string.groups_folder) + eventID + context.getString(R.string.data_format)));
    }

    public static HashMap<String, HashMap<String, String>> getGroupsFromFile(Context context, String eventID) {
        return FileManager.fileManager.readFromFileHashMapHash(context, context.getString(R.string.groups_folder) + eventID + context.getString(R.string.data_format));
    }

    //Get random reminder
    public static String getRandomReminder(Context context, @Nullable String lastReminder) {
        //Get random reminder if he is not equals to the last reminder, if he is then take the next reminder
        String back = null;
        HashMap<String, HashMap<String, String>> messages =
                FileManager.fileManager.readFromFileHashMapHash(context, context.getString(R.string.reminders) + context.getString(R.string.data_format));
        if (messages.size() > 0) {
            String[] textMessages = messages.keySet().toArray(new String[messages.size()]);
            Random random = new Random();
            int min = 0;
            int max = messages.size() - 1;
            int randNumber = random.nextInt(max - min + 1) + min;
            if (lastReminder != null && textMessages[randNumber].equals(lastReminder)) {
                if (randNumber != textMessages.length - 1) {
                    randNumber++;
                } else {
                    randNumber = 0;
                }
            }
            back = textMessages[randNumber];
        }
        return back;
    }
}
