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
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by marco on 01/09/2017.
 */
public class ServerFitbit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFitbit.class);

    private static final int HEARTRATE_THRESHOLH = 110;
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


    public ServerFitbit() {
        scheduler = Executors.newScheduledThreadPool(1);
    }


    public static void startServer()
    {
        ServerFitbit server = new ServerFitbit();
        server.scheduler.scheduleAtFixedRate(new TaskHeartRate(), 0, 5, TimeUnit.MINUTES);
    }

    private static void analyzeHeartRate(final int heartRate)
            throws IOException
    {
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        HttpClient httpClient = HttpClientBuilder.create().build();

        if(heartRate >= HEARTRATE_THRESHOLH)
        {
            AIResponse response = new AIResponse();
            response.setResult(new Result());
            response.getResult().setAction("lightsOn");
            String json = gson.toJson(response);


            HttpPost request = new HttpPost(properties.getProperty(PROPERTY_NAME_NGROCK));
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

    private static class TaskHeartRate implements Runnable
    {
        @Override
        public void run() {
            int heartRate = Oauth2Client.getHeartRate();
            try {
                analyzeHeartRate(heartRate);
            } catch (IOException ioe) {
                LOGGER.error("unable to analyzeHeartRate, since ", ioe);
            }
        }
    }
}
