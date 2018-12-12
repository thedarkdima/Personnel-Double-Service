package com.potatomasterextreme.personnel.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.services.MessagesJobService;
import com.potatomasterextreme.personnel.services.MessagesService;

public class MessagesServiceReceiver extends BroadcastReceiver {
    //Runs when the messages service stops
    @Override
    public void onReceive(Context context, Intent intent) {
        MessageManager.startMessageService(context, MessageManager.JOB_ID);
    }
}
