package com.potatomasterextreme.personnel.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;
import com.potatomasterextreme.personnel.receivers.MessagesServiceReceiver;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MessagesJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        MessageManager.Main(this);
        //NotificationMaster.createNotification(MessagesJobService.this, "Service", "Job service works: ", NotificationMaster.PRIORITY_MAX, 5);
        jobFinished(jobParameters, true);
        return true;
    }

    @Override
    public void onDestroy() {
        Intent broadcastIntent = new Intent(this, MessagesServiceReceiver.class);
        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Intent broadcastIntent = new Intent(this, MessagesServiceReceiver.class);
        sendBroadcast(broadcastIntent);

        return true;
    }
}
