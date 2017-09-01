package it.upo.reti2s.fitbit;

import com.google.gson.annotations.SerializedName;

import java.time.LocalTime;

/**
 * Created by marco on 01/09/2017.
 */
public class HeartRate {

    private static final long serialVersionUID = 1L;


    @SerializedName("time")
    private String timeStamp;
    @SerializedName("value")
    private int heartRate;

    public String getTimeStamp() {
        return timeStamp;
    }

    public int getHeartRate() {
        return heartRate;
    }

}
