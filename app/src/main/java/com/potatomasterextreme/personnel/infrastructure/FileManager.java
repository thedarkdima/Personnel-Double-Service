package com.potatomasterextreme.personnel.infrastructure;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileManager {

    public static final FileManager fileManager = new FileManager();

    public boolean working = false;

    private HashMap<String[], ArrayList<Object>> toDo = new HashMap<>();

    private HashMap<String[], ArrayList<Object>> toDoLater = new HashMap<>();

    private FileManager() {
    }

    private WhatToDo lastToDo = WhatToDo.ADD;

    public enum WhatToDo {
        ADD,
        REMOVE,
        REMOVE_INSIDE
    }

    public void add(@NonNull Context context, WhatToDo whatToDo, String fileName, Object object) {
        String[] key = new String[]{whatToDo.toString(), fileName};
        //ArrayList<Object> data = toDo.get(fileName);
        //Should be not needed
        //if(data == null){
        //    data = new ArrayList<>();
        //}
        //data.add(object);

        if (!working) {
            if (!toDo.containsKey(key)) {
                toDo.put(key, new ArrayList<Object>());
            }
            working = true;
            toDo.get(key).add(object);
            start(context);
        } else {
            if (!toDoLater.containsKey(key)) {
                toDoLater.put(key, new ArrayList<Object>());
            }
            toDoLater.get(key).add(object);
        }
    }

    private void start(Context context) {
        HashMap<String, ArrayList<Object>> doThis = new HashMap<>();
        if (toDo.size() > 0) {
            for (String[] key : toDo.keySet()) {
                doThis.put(key[1], toDo.get(key));
                switch (key[0]) {
                    case "ADD":
                        add_start(context, doThis);
                        break;
                    case "REMOVE":
                        remove_start(context, doThis);
                        break;
                    case "REMOVE_INSIDE":
                        remove_inside_start(context, doThis);
                        break;
                }
                toDo.remove(key);
                break;
            }
        }
    }

    private void add_start(Context context, HashMap<String, ArrayList<Object>> doThis) {
        //Goes through all the toDo hash map and saves the data to files
        //key[0] = WhatToDo
        //key[1] = FileName
        for (String key : doThis.keySet()) {
            HashMap<String, HashMap<String, String>> fileObject;
            //fileObject = (HashMap<String, HashMap<String, String>>) readFromFile(context, key, fileObject.getClass());
            fileObject = readFromFileHashMapHash(context, key);
            for (Object object : doThis.get(key)) {
                //Loads the data from a file of choice and then puts the values of the added data into it, and then saves it.
                HashMap<String, HashMap<String, String>> temp = (HashMap<String, HashMap<String, String>>) object;
                for (String key1 : temp.keySet()) {
                    //Adds the data into file, it dons't matter that the data gets overwritten because to add data the application must use old data as reference
                    if (fileObject == null) {
                        fileObject = new HashMap<>();
                    }
                    if (!fileObject.containsKey(key1)) {
                        fileObject.put(key1, temp.get(key1));
                    } else {
                        fileObject.get(key1).putAll(temp.get(key1));
                    }
                }
            }
            writeToFile(context, key, fileObject);
        }
        if (!toDoLater.isEmpty()) {
            //Checks if while saving new data to a file there were added new data to the manager if so add that data as well.
            //Can create problems if the last context already got dumped but something like that shouldn't be happening in this application.
            toDo.putAll(toDoLater);
            toDoLater = new HashMap<>();
            start(context);
        } else {
            working = false;
        }
    }

    private void remove_start(Context context, HashMap<String, ArrayList<Object>> doThis) {
        //Goes through all the toDo hash map and saves the data to files
        //key[0] = WhatToDo
        //key[1] = FileName
        for (String key : doThis.keySet()) {
            HashMap<String, HashMap<String, String>> fileObject;
            fileObject = readFromFileHashMapHash(context, key);
            for (Object object : doThis.get(key)) {
                //Loads the data from a file of choice and then puts the values of the added data into it, and then saves it.
                HashMap<String, HashMap<String, String>> temp = (HashMap<String, HashMap<String, String>>) object;
                for (String key1 : temp.keySet()) {
                    fileObject.remove(key1);
                }
            }
            writeToFile(context, key, fileObject);
        }
        if (!toDoLater.isEmpty()) {
            //Checks if while saving new data to a file there were added new data to the manager if so add that data as well.
            //Can create problems if the last context already got dumped but something like that shouldn't be happening in this application.
            toDo.putAll(toDoLater);
            toDoLater = new HashMap<>();
            start(context);
        } else {
            working = false;
        }
    }

    private void remove_inside_start(Context context, HashMap<String, ArrayList<Object>> doThis) {
        //Goes through all the toDo hash map and saves the data to files
        //key[0] = WhatToDo
        //key[1] = FileName
        for (String key : doThis.keySet()) {
            HashMap<String, HashMap<String, String>> fileObject;
            fileObject = readFromFileHashMapHash(context, key);
            for (Object object : doThis.get(key)) {
                //Loads the data from a file of choice and then puts the values of the added data into it, and then saves it.
                HashMap<String, HashMap<String, String>> temp = (HashMap<String, HashMap<String, String>>) object;
                for (String key1 : temp.keySet()) {
                    for (String key2 : temp.get(key1).keySet()) {
                        fileObject.get(key1).remove(key2);
                    }
                }
            }
            writeToFile(context, key, fileObject);
        }
        if (!toDoLater.isEmpty()) {
            //Checks if while saving new data to a file there were added new data to the manager if so add that data as well.
            //Can create problems if the last context already got dumped but something like that shouldn't be happening in this application.
            toDo.putAll(toDoLater);
            toDoLater = new HashMap<>();
            start(context);
        } else {
            working = false;
        }
    }

    /*private void add_start(Context context, HashMap<String, ArrayList<Object>> doThis) {
        //Goes through all the toDo hash map and saves the data to files
        //key[0] = WhatToDo
        //key[1] = FileName
        while (!toDo.isEmpty()) {
            for (String[] key : toDo.keySet()) {
                HashMap<String, HashMap<String, String>> fileObject;
                //fileObject = (HashMap<String, HashMap<String, String>>) readFromFile(context, key, fileObject.getClass());
                fileObject = readFromFileHashMapHash(context, key[1]);
                for (Object object : toDo.get(key)) {
                    //Loads the data from a file of choice and then puts the values of the added data into it, and then saves it.
                    HashMap<String, HashMap<String, String>> temp = (HashMap<String, HashMap<String, String>>) object;
                    for (String key1 : temp.keySet()) {
                        //Adds the data into file, it dons't matter that the data gets overwritten because to add data the application must use old data as reference
                        if(fileObject == null){
                            fileObject = new HashMap<>();
                        }
                        if (!fileObject.containsKey(key1)){
                            fileObject.put(key1, temp.get(key1));
                        } else {
                            fileObject.get(key1).putAll(temp.get(key1));
                        }
                    }
                }
                writeToFile(context, "", key[1], fileObject);
                toDo.remove(key);
            }
        }
        if (!toDoLater.isEmpty()) {
            //Checks if while saving new data to a file there were added new data to the manager if so add that data as well.
            //Can create problems if the last context already got dumped but something like that shouldn't be happening in this application.
            toDo.putAll(toDoLater);
            toDoLater = new HashMap<>();
            start(context);
        } else {
            working = false;
        }
    }*/

    private void writeToFile(Context context, String fileName, Object object) {
        String fileDir = "";
        int index = fileName.indexOf("/");
        if (index != -1) {
            fileDir = fileName.substring(0, index);
            fileName = fileName.substring(index + 1, fileName.length());
        }
        //Json the object
        Gson gson = new Gson();
        String json = gson.toJson(object);
        try {
            File newDir = context.getFileStreamPath(fileDir);
            if (!newDir.exists()) {
                newDir.mkdirs();
            }
            File fileWithinMyDir = new File(newDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(fileWithinMyDir);
            //FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object readFromFile(Context context, String fileName, Type type) {
        String fileDir = "";
        int index = fileName.indexOf("/");
        if (index != -1) {
            fileDir = fileName.substring(0, index);
            fileName = fileName.substring(index + 1, fileName.length());
        }
        try {
            FileInputStream in = new FileInputStream(new File(context.getFileStreamPath(fileDir), fileName));
            byte[] buffer = new byte[1024];
            int actuallyRead;
            StringBuilder sb = new StringBuilder();
            while ((actuallyRead = in.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, actuallyRead));
            }
            //Return the object from json
            Gson gson = new Gson();
            return gson.fromJson(sb.toString(), type);
            //return gson.fromJson(sb.toString(), objectClass);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, HashMap<String, String>> readFromFileHashMapHash(Context context, String fileName) {
        HashMap<String, HashMap<String, String>> map = (HashMap<String, HashMap<String, String>>) readFromFile(context, fileName, GetHsHssType());
        if (map == null) {
            return new HashMap<>();
        } else {
            return map;
        }
    }

    public static Type GetHsHssType() {
        return new TypeToken<HashMap<String, HashMap<String, String>>>() {
        }.getType();
    }

    public static void removeFile(Context context, String fileName) {
        String fileDir = "";
        int index = fileName.indexOf("/");
        if (index != -1) {
            fileDir = fileName.substring(0, index);
            fileName = fileName.substring(index + 1, fileName.length());
        }
        File dir = context.getFileStreamPath(fileDir);
        File file = new File(dir, fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}


