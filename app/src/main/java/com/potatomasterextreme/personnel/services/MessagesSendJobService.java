package com.potatomasterextreme.personnel.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.potatomasterextreme.personnel.infrastructure.MessageManager;
import com.potatomasterextreme.personnel.receivers.MessagesServiceReceiver;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MessagesSendJobService extends JobService {

    boolean repeat = false;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        repeat = MessageManager.startSendingSms(this);
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
        if (repeat) {
            Intent broadcastIntent = new Intent(this, MessagesServiceReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        return true;
    }
}
