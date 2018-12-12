package com.potatomasterextreme.personnel.infrastructure;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;

import java.util.HashMap;

public class ContactManager {

    public static boolean checkForContactPermissions(Context context) {
        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // no permission!!!
            // request permission..
            //ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // will call if permission already granted!
            return true;
        }
        return false;
    }

    public static HashMap<String, HashMap<String, String>> getContacts(Context context) {
        HashMap<String, HashMap<String, String>> contacts = new HashMap<>();
        if (checkForContactPermissions(context)) {
            //Gets the contacts names and phone numbers from the phone
            Cursor phoneContacts = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (phoneContacts.moveToNext()) {
                //Place the contact name and phone number into data
                String phone = phoneContacts.getString(phoneContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone = phoneFormat(phone);
                contacts.put(phone, new HashMap<String, String>());
                contacts.get(phone).put("id", phoneContacts.getString(phoneContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
                contacts.get(phone).put("name", phoneContacts.getString(phoneContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contacts.get(phone).put("phone", phone);
            }
            phoneContacts.close();
        }
        return contacts;
    }

    public static final String COUNTRY_CODE = "+972";

    public static String phoneFormat(String phoneNumber) {
        //String back = PhoneNumberUtils.formatNumber(PhoneNumberUtils.stripSeparators(phoneNumber));
        String back = PhoneNumberUtils.stripSeparators(phoneNumber);
        if (!back.contains("+") && back.length() > 8) {
            if (!back.substring(0, 3).contains(COUNTRY_CODE.substring(1, 3))) {
                if (back.length() > 0 && back.charAt(0) == '0') {
                    back = back.substring(1, back.length());
                }
                back = COUNTRY_CODE + back;
            }
        }
        return back;
    }

}
