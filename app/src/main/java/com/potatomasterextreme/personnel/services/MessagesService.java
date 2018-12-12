package com.potatomasterextreme.personnel.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;
import com.potatomasterextreme.personnel.receivers.MessagesServiceReceiver;

public class MessagesService extends Service {
    boolean working = true;

    public MessagesService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (working) {
                    try {
                        MessageManager.Main(MessagesService.this);
                        //NotificationMaster.createNotification(MessagesService.this, "Service", "Service works: ", NotificationMaster.PRIORITY_MAX, 5);
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        working = false;

        Intent broadcastIntent = new Intent(this, MessagesServiceReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
