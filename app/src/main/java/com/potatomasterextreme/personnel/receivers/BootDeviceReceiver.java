package com.potatomasterextreme.personnel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;

public class BootDeviceReceiver extends BroadcastReceiver {
    //When booting start the messages service
    @Override
    public void onReceive(Context context, Intent intent) {
        MessageManager.forceStartMessageService(context, MessageManager.JOB_ID);
        MessageManager.startMessageSendService(context, MessageManager.SEND_JOB_ID, 0L);
    }
}
