package com.karakostas.disasterreport;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private EarthquakeRepository mRepository;

    @NonNull
    @Override
    public Result doWork() {

        return null;
    }
}
