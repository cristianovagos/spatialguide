package com.paydayme.spatialguide.model.download;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Download implements Parcelable {

    private int progress;
    private int currentFileSize;
    private int totalFileSize;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(progress);
        dest.writeInt(currentFileSize);
        dest.writeInt(totalFileSize);
    }

    private Download(Parcel in) {
        progress = in.readInt();
        currentFileSize = in.readInt();
        totalFileSize = in.readInt();
    }

    public static final Creator<Download> CREATOR = new Creator<Download>() {

        @Override
        public Download createFromParcel(Parcel source) {
            return new Download(source);
        }

        @Override
        public Download[] newArray(int size) {
            return new Download[size];
        }
    };
}
