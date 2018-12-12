package com.potatomasterextreme.personnel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;
import com.potatomasterextreme.personnel.services.MessagesService;

public class BootDeviceReceiver extends BroadcastReceiver {
    //When booting start the messages service
    @Override
    public void onReceive(Context context, Intent intent) {
        MessageManager.startMessageService(context, MessageManager.JOB_ID);
        MessageManager.startMessageSendService(context, MessageManager.SEND_JOB_ID, 0L);
    }
}
