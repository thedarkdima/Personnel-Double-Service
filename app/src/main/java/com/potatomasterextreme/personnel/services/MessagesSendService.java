package com.potatomasterextreme.personnel.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.infrastructure.FileManager;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;
import com.potatomasterextreme.personnel.receivers.MessagesSendServiceReceiver;
import com.potatomasterextreme.personnel.receivers.MessagesServiceReceiver;

import java.util.HashMap;

public class MessagesSendService extends Service {
    boolean repeat = false;

    public MessagesSendService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Long sleep_time = intent.getLongExtra("sleep_time", 0);
        //NotificationMaster.createNotification(this, "asd", "asd", NotificationMaster.PRIORITY_MAX, 5151, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sleep_time);
                    repeat = MessageManager.startSendingSms(MessagesSendService.this);
                    MessagesSendService.this.stopSelf();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (repeat) {
            Intent broadcastIntent = new Intent(this, MessagesSendServiceReceiver.class);
            broadcastIntent.putExtra("sleep_time",  new Long(MessageManager.WAIT_TIME));
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
