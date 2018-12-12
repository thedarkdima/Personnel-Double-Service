package com.potatomasterextreme.personnel.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.services.MessagesSendJobService;
import com.potatomasterextreme.personnel.services.MessagesSendService;

public class MessagesSendServiceReceiver extends BroadcastReceiver {
    //Runs when the messages service stops
    @Override
    public void onReceive(Context context, Intent intent) {
        Long sleep_time = intent.getLongExtra("sleep_time", 0);
        //MessageManager.startMessageSendService(context, MessageManager.SEND_JOB_ID, sleep_time);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(new JobInfo.Builder(MessageManager.SEND_JOB_ID, new ComponentName(context, MessagesSendJobService.class))
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
}
