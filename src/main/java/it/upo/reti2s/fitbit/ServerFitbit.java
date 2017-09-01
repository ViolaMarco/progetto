package it.upo.reti2s.fitbit;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;


/**
 * Created by marco on 01/09/2017.
 */
public class ServerFitbit {

    private static final int HEARTRATE_THRESHOLH = 110;
    private static final String URL_WEBHOOK = "";

    public  static  void main(String[] args) throws IOException {
        ServerFitbit server = new ServerFitbit();
        server.analyzeHeartRate(120);
    }

    private void analyzeHeartRate(final int heartRate)
            throws IOException {
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        HttpClient httpClient = HttpClientBuilder.create().build();

        if(heartRate >= HEARTRATE_THRESHOLH)
        {
            AIResponse response = new AIResponse();
            response.setResult(new Result());
            response.getResult().setAction("lightsOn");
            String json = gson.toJson(response);

            HttpPost request = new HttpPost("https://6e8a19a9.ngrok.io");
            StringEntity params =new StringEntity(json);
            request.addHeader("content-type", "application/json; charset=utf-8");
            request.setEntity(params);
            httpClient.execute(request);

            response.getResult().setAction("playMusic");
            json = gson.toJson(response);
            params =new StringEntity(json);
            request.setEntity(params);
            httpClient.execute(request);

        }
    }
}
