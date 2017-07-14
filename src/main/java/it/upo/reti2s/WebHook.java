package it.upo.reti2s;

import ai.api.model.AIResponse;

import com.google.gson.Gson;


import ai.api.GsonFactory;
import ai.api.model.Fulfillment;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import it.upo.reti2s.hue.rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        //final DeviceList allDevices = zwayApi.getDevices();

        final String baseURL = "http://172.30.1.138";

        final String username = "SqdltmqZU0bXpeXxdYb65nxlmZcyn39t7ctibKvl";

        final String lightsURL = baseURL + "/api/" + username + "/lights/";

        final Map<String, ?> allLights = rest.get(lightsURL);


        //<editor-fold desc = "LUCE">
        /***********************************/
        /* ACCENDI LUCE                    */
        /***********************************/

        if(input.getResult().getAction().equalsIgnoreCase("lightsOn"))
        {
            /*
            for (Device dev : allDevices.getAllDevices()) {
                if (dev.getDeviceType().equalsIgnoreCase("SwitchBinary") && dev.getNodeId() == ID_DEVICE_LUCE) {
                    // turn it on
                    LOGGER.info("Turn device " + dev.getNodeId() + " ON");
                    dev.on();
                }
            }
            */

            for (String light : allLights.keySet()) {
                String callURL = lightsURL + light + "/state";
                String body = "{ \"on\" : true, \"xy\":[0.41,0.51721] }";
                rest.put(callURL, body, "application/json");
            }

        }

        /***********************************/
        /* SPEGNI LUCE                    */
        /***********************************/
        if(input.getResult().getAction().equalsIgnoreCase("lightsOff"))
        {
            /*
            for (Device dev : allDevices.getAllDevices()) {
                if (dev.getDeviceType().equalsIgnoreCase("SwitchBinary") && dev.getNodeId() == ID_DEVICE_LUCE) {
                    // turn it on
                    LOGGER.info("Turn device " + dev.getNodeId() + " OFF");
                    dev.off();
                }
            }
            */
            for (String light : allLights.keySet()) {
                String callURL = lightsURL + light + "/state";
                String body = "{ \"on\" : false }";
                rest.put(callURL, body, "application/json");
            }
        }

        //</editor-fold desc = LUCE>

    }

}
