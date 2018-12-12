package com.potatomasterextreme.personnel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;

public class MessagesServiceReceiver extends BroadcastReceiver {
    //Runs when the messages service stops
    @Override
    public void onReceive(Context context, Intent intent) {
        MessageManager.forceStartMessageService(context, MessageManager.JOB_ID);
    }
}
