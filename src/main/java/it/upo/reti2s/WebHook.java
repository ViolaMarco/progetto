package it.upo.reti2s;

import ai.api.model.AIResponse;

import com.google.gson.Gson;


import ai.api.GsonFactory;
import ai.api.model.Fulfillment;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;

import it.upo.reti2s.hue.Rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
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
    private static final List<MyThread> threads = new ArrayList<>();

    private static final IZWayApi zwayApi = new ZWayApiHttp(IP_ADDRESS, 8083, "http", USER, PASSWORD, 0, false, new ZWaySimpleCallback());


    public static void main(String[] args)
    {
        // init gson, from the api.ai factory
        Gson gson = GsonFactory.getDefaultFactory().getGson();


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
    private static void doWebhook(AIResponse input, Fulfillment output) throws InterruptedException {

        //<editor-fold desc = "LUCE">

        final String UrlHue = "http://172.30.1.138";

        final String usernameHue = "SqdltmqZU0bXpeXxdYb65nxlmZcyn39t7ctibKvl";

        final String lightsURL = UrlHue + "/api/" + usernameHue + "/lights/";

        //todo:da rimettere final Map<String, ?> allLights = Rest.get(lightsURL);


        /***********************************/
        /* ACCENDI LUCE                    */
        /***********************************/


        if(input.getResult().getAction().equalsIgnoreCase("lightsOn"))
        {
            /*
            for (String light : allLights.keySet()) {
                String callURL = lightsURL + light + "/state";
                String body = "{ \"on\" : true, \"xy\":[0.41,0.51721] }";
                Rest.put(callURL, body, "application/json");
            }
            */
            output.setSpeech("luci onnnnnnn");
            System.out.println("luci ok");
        }


        /***********************************/
        /* SPEGNI LUCE                     */
        /***********************************/
        /*
        if(input.getResult().getAction().equalsIgnoreCase("lightsOff"))
        {
            for (String light : allLights.keySet()) {
                String callURL = lightsURL + light + "/state";
                String body = "{ \"on\" : false }";
                Rest.put(callURL, body, "application/json");
            }
        }
        */

        //</editor-fold desc = LUCE>

        //<editor-fold desc = "FITBIT">


        /***********************************/
        /* INFORMAZIONI PAZIENTE           */
        /***********************************/
        if(input.getResult().getAction().equalsIgnoreCase("askInfoHeartRate"))
        {
            output.setSpeech     ("azione controllo vecchio");

            /*
            https://www.fitbit.com/oauth2/authorize?response_type=code&client_id=228GYV&scope=weight
             */
        }
        //</editor-fold desc = FITBIT>


    if(input.getResult().getAction().equalsIgnoreCase("playMusic"))
    {
        MyThread thread = new MyThread();
        threads.add(thread);
        thread.start();
        output.setSpeech("musica accesa");
        System.out.print("music on");

    }
    if(input.getResult().getAction().equalsIgnoreCase("stopMusic"))
    {
        for (MyThread t : threads)
        {
            t.interrupt();
        }
        System.out.print("music off");

    }

    }

    public static class MyThread extends Thread {

        public void run(){
            File clip = new File("src/main/resources/relax.wav");
            try{
                Clip music =  AudioSystem.getClip();
                music.open(AudioSystem.getAudioInputStream(clip));
                music.start();
                //this.sleep(music.getMicrosecondLength()/1000);
            }catch (Exception e)
            {
                LOGGER.error("unable to play sound since:",e);
            }
        }
    }
}
