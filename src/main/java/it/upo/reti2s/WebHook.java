package it.upo.reti2s;

import ai.api.model.AIResponse;

import com.google.gson.Gson;


import ai.api.GsonFactory;
import ai.api.model.Fulfillment;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String USER = "admin";
    private static final String PASSWORD = "raz4reti2";

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

        final DeviceList allDevices = zwayApi.getDevices();

        //<editor-fold desc = "LUCE">
        /***********************************/
        /* ACCENDI LUCE                    */
        /***********************************/

        if(input.getResult().getAction().equalsIgnoreCase("lightsOn"))
        {
            for (Device dev : allDevices.getAllDevices()) {
                if (dev.getDeviceType().equalsIgnoreCase("SwitchBinary") && dev.getNodeId() == 20) {
                    // turn it on
                    LOGGER.info("Turn device " + dev.getNodeId() + " ON");
                    dev.on();
                }
            }
        }

        /***********************************/
        /* ACCENDI LUCE                    */
        /***********************************/
        if(input.getResult().getAction().equalsIgnoreCase("lightsOff"))
        {
            for (Device dev : allDevices.getAllDevices()) {
                if (dev.getDeviceType().equalsIgnoreCase("SwitchBinary") && dev.getNodeId() == 20) {
                    // turn it on
                    LOGGER.info("Turn device " + dev.getNodeId() + " OFF");
                    dev.off();
                }
            }
        }
        //</editor-fold desc = LUCE>

    }

}
