package com.katsuna.launcher.katsuna.dashboard.tasks;

import com.katsuna.launcher.katsuna.utils.DownloadResponse;

public interface IAsyncTaskFinished {
    void onFinish(DownloadResponse downloadResponse);
}