package it.upo.reti2s.fitbit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by marco on 01/09/2017.
 */
public class JsonHeartRateIntra {

    private static final long serialVersionUID = 1L;


    @SerializedName("dataset")
    private List<HeartRate> heartRates;
    @SerializedName("datasetInterval")
    private int interval;
    @SerializedName("datasetType")
    private String unit;


    public List<HeartRate> getHeartRates() {
        return heartRates;
    }

    public int getInterval() {
        return interval;
    }

    public String getUnit() {
        return unit;
    }
}
