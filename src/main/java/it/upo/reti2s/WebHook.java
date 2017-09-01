package it.upo.reti2s;

import ai.api.model.AIResponse;

import com.google.gson.Gson;


import ai.api.GsonFactory;
import ai.api.model.Fulfillment;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;

import it.upo.reti2s.Oauth2.Oauth2Client;
import it.upo.reti2s.fitbit.ServerFitbit;
import it.upo.reti2s.hue.Rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

/**
 * api.ai Webhook example.
 * It gets all tasks, a specified task from a database and provides the information to
 * the api.ai service. It also allows users to create a new task, through the api.ai
 * conversational interface.
 *
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.0 (21/05/2017)
 */
public class WebHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHook.class);

    private static final String IP_ADDRESS = "172.30.1.137";
    private static final String USER       = "admin";
    private static final String PASSWORD   = "raz4reti2";

    /***********************************/
    /* ID DEVICE                       */
    /***********************************/

    private static final int ID_DEVICE_LUCE = 20;

    private static final IZWayApi zwayApi = new ZWayApiHttp(IP_ADDRESS, 8083, "http", USER, PASSWORD, 0, false, new ZWaySimpleCallback());


    public static void main(String[] args)
    {
        // init gson, from the api.ai factory
        Gson gson = GsonFactory.getDefaultFactory().getGson();


        //start server for heartRate fitbit
        ServerFitbit.startServer();

        // start webhook sulla richiesta
        post("/", (request, response) -> {
            Fulfillment output = new Fulfillment();

            doWebhook(gson.fromJson(request.body(), AIResponse.class), output);

            response.type("application/json");

            return output;
        }, gson::toJson);
    }

    /**
     * The webhook method. It is where the "magic" happens.
     * Please, notice that in this version we ignore the "urgent" field of tasks.
     *
     * @param input  the request body that comes from api.ai
     * @param output the @link(Fulfillment) response to be sent to api.ai
     */
    private static void doWebhook(AIResponse input, Fulfillment output) {

        //<editor-fold desc = "LUCE">

        final String UrlHue = "http://172.30.1.138";

        final String usernameHue = "SqdltmqZU0bXpeXxdYb65nxlmZcyn39t7ctibKvl";

        final String lightsURL = UrlHue + "/api/" + usernameHue + "/lights/";

        final List<MusicThread> threads = new ArrayList<>();

        final Map<String, ?> allLights = Rest.get(lightsURL);


        /***********************************/
        /* ACCENDI LUCE                    */
        /***********************************/

        if(input.getResult().getAction().equalsIgnoreCase("lightsOn"))
        {
            for (String light : allLights.keySet()) {
                String callURL = lightsURL + light + "/state";
                String body = "{ \"on\" : true, \"xy\":[0.41,0.51721] }";
                Rest.put(callURL, body, "application/json");
            }
        }

        /***********************************/
        /* SPEGNI LUCE                     */
        /***********************************/

        if(input.getResult().getAction().equalsIgnoreCase("lightsOff"))
        {
            for (String light : allLights.keySet()) {
                String callURL = lightsURL + light + "/state";
                String body = "{ \"on\" : false }";
                Rest.put(callURL, body, "application/json");
            }
        }


        //</editor-fold desc = LUCE>

        /***********************************/
        /* INFORMAZIONI PAZIENTE           */
        /***********************************/
        if(input.getResult().getAction().equalsIgnoreCase("askInfoHeartRate"))
        {
            output.setSpeech(String.valueOf(Oauth2Client.getHeartRate()));
            output.setDisplay(String.valueOf(Oauth2Client.getHeartRate()));
        }

        /***********************************/
        /* MUSIC                           */
        /***********************************/
        if(input.getResult().getAction().equalsIgnoreCase("playMusic"))
        {
            MusicThread musicThread = new MusicThread();
            threads.add(musicThread);
            musicThread.run();
            output.setSpeech("Musica accesa");
            LOGGER.info("Music on");
        }
        if(input.getResult().getAction().equalsIgnoreCase("stopMusic"))
        {
            for (MusicThread thread : threads)
            {
                thread.interrupt();
            }
            output.setSpeech("Musica spenta");
            LOGGER.info("Music off");
        }

    }

    private static class MusicThread extends Thread
    {
        @Override
        public void run() {
            File clip = new File("src/main/resources/relax.waw");
            try
            {
                Clip music = AudioSystem.getClip();
                music.open(AudioSystem.getAudioInputStream(clip));
                music.start();
            }
            catch (Exception e)
            {
                LOGGER.error("Unable to play sound since ", e);
            }
        }
    }

}
