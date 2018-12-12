package com.potatomasterextreme.personnel.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.infrastructure.NotificationMaster;
import com.potatomasterextreme.personnel.receivers.MessagesServiceReceiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MessagesJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        //12:28
        MessageManager.Main(this);
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        NotificationMaster.createNotification(MessagesJobService.this, "Service", "Job service works: " + formatter.format(currentTime), NotificationMaster.PRIORITY_LOW, 10, null);
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
