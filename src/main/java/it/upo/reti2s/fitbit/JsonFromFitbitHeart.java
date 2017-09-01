package it.upo.reti2s.fitbit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by marco on 01/09/2017.
 */
public class JsonFromFitbitHeart {
    private static final long serialVersionUID = 1L;


//    @SerializedName("activities-heart")
//    private String activitiesHeart;

    @SerializedName("activities-heart-intraday")
    private JsonHeartRateIntra jsonHeartRateIntra;


    public int getLastHeartRate()
    {
        List<HeartRate> heartRates =  jsonHeartRateIntra.getHeartRates();
        return heartRates.get(heartRates.size()-1).getHeartRate();
    }

}
