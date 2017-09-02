package it.upo.reti2s.fitbit;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.google.gson.Gson;
import it.upo.reti2s.Oauth2.Oauth2Client;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by marco on 01/09/2017.
 */
public class ServerFitbit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFitbit.class);

    private static final int HEARTRATE_THRESHOLH = 20;
    private static final String PROPERTY_NAME_NGROCK = "ngrock";
    private static final Properties properties = new Properties();

    static
    {
        try {
            properties.load(new FileInputStream("src/main/resources/Properties.properties"));
        } catch (IOException e) {
            LOGGER.error("Unable to read Properties file");
        }
    }

    private final ScheduledExecutorService scheduler ;


    private ServerFitbit() {
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public static void startServer()
    {
        ServerFitbit server = new ServerFitbit();
        server.scheduler.scheduleAtFixedRate(new TaskHeartRate(), 0, 5, TimeUnit.SECONDS);
    }

    private static void analyzeHeartRate(final int heartRate)
            throws IOException
    {
        if(heartRate >= HEARTRATE_THRESHOLH)
        {
            sendActions("lightsOn","playMusic");
        }
        else
        {
            sendActions("lightsOff","stopMusic");
        }
    }

    private static void sendActions(final String lights, final String music)
            throws IOException
    {
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        HttpClient httpClient = HttpClientBuilder.create().build();

        AIResponse response = new AIResponse();
        response.setResult(new Result());
        HttpPost request = new HttpPost(properties.getProperty(PROPERTY_NAME_NGROCK));
        request.addHeader("content-type", "application/json; charset=utf-8");

        response.getResult().setAction(lights);
        String json = gson.toJson(response);
        StringEntity params =new StringEntity(json);
        request.setEntity(params);
        httpClient.execute(request);

        response.getResult().setAction(music);
        json = gson.toJson(response);
        params =new StringEntity(json);
        request.setEntity(params);
        httpClient.execute(request);
    }

    private static class TaskHeartRate implements Runnable
    {
        @Override
        public void run() {
            int heartRate = Oauth2Client.getHeartRate();
            try {
                LOGGER.info(LocalTime.now() + "   info:"+heartRate);
                analyzeHeartRate(heartRate);
            } catch (IOException ioe) {
                LOGGER.error("unable to analyzeHeartRate, since ", ioe);
            }
        }
    }
}
