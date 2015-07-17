package com.droidsonroids.bootcamp.soccertablemanager.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Table {

    @SerializedName(value = "time")
    String mTime;

    @SerializedName(value = "id")
    int mTableId;

    @SerializedName(value = "free_spots_number")
    int mFreeSpotsNumber;

    @SerializedName(value = "user_names")
    List<String> mUserNameList;

    @SerializedName(value = "created_at_timestamp")
    long mCreatedTimestamp;

    public String getTime() {
        return mTime;
    }

    public int getTableId() {
        return mTableId;
    }

    public int getFreeSpotsNumber() {
        return mFreeSpotsNumber;
    }

    public List<String> getUserNameList() {
        return mUserNameList;
    }

    public long getCreatedTimestamp() {
        return mCreatedTimestamp;
    }

    public String toStringShort() {
        return "Time: " + mTime + " Free Spots: " + mFreeSpotsNumber;
    }

    public String toStringLong() {
        String message = "";
        message += "Id: " + mTableId + "\nTime: " + mTime + "\nFree Spots: " + mFreeSpotsNumber + "\nPlayers: ";
        for (String name : mUserNameList)
            message += name + " ";
        return message;
    }
}
