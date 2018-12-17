package com.katsuna.launcher.katsuna.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Locale;

public class DownloadResponse implements Parcelable {
    public static final Creator<DownloadResponse> CREATOR =
        new Creator<DownloadResponse>() {
            @Override
            public DownloadResponse createFromParcel(Parcel source) {
                return new DownloadResponse(source);
            }

            @Override
            public DownloadResponse[] newArray(int size) {
                return new DownloadResponse[size];
            }
        };
    public String response;
    public int responceCode;
    public Exception exception;

    public DownloadResponse() {
    }

    private DownloadResponse(Parcel in) {
        this.response = in.readString();
        this.responceCode = in.readInt();
        this.exception = (Exception) in.readSerializable();
    }

    public boolean allGood() {
        return responceCode == 200;
    }

    public String problems() {
        return String.format(Locale.getDefault(), "responseCode= %d exception= %s", responceCode,
            exception);
    }

    @NonNull
    @Override
    public String toString() {
        return response;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.response);
        dest.writeInt(this.responceCode);
        dest.writeSerializable(this.exception);
    }
}
