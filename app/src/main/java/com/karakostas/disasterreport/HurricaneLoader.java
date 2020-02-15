package com.karakostas.disasterreport;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.File;

public class HurricaneLoader extends AsyncTaskLoader<File> {
    public HurricaneLoader(@NonNull Context context) {
        super(context);
    }
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
    @Nullable
    @Override
    public File loadInBackground() {
        DisasterUtils.getHurricaneData(getContext());
        return new File(getContext().getFilesDir() + "/active.csv");
    }
}
