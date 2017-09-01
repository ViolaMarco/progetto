package it.upo.reti2s.Oauth2;

import ai.api.GsonFactory;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;
import it.upo.reti2s.fitbit.JsonFromFitbitHeart;


import java.io.IOException;
import java.util.Arrays;


/**
 * A sample application that demonstrates how the Google OAuth2 library can be used to authenticate
 * against Daily Motion.
 *
 * @author Ravi Mistry
 */
public class Oauth2Client {

    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/fitbit");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * OAuth 2 scope.
     */
    private static final String SCOPE = "heartrate";

    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Global instance of the JSON factory.
     */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final String TOKEN_SERVER_URL         = "https://api.fitbit.com/oauth2/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://api.fitbit.com/oauth2/authorize";

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {
        OAuth2ClientCredentials.errorIfNotSpecified();
        // set up authorization code flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
                .authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new BasicAuthentication(
                        OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
                OAuth2ClientCredentials.API_KEY,
                AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(SCOPE))
                .setDataStoreFactory(DATA_STORE_FACTORY).build();
        // authorize
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
                OAuth2ClientCredentials.DOMAIN).setPort(OAuth2ClientCredentials.PORT).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

//    private static void run(HttpRequestFactory requestFactory) throws IOException
//    {
////        GenericUrl url = new GenericUrl("https://api.fitbit.com/1/user/-/activities/date/today.json");
////        GenericUrl url = new GenericUrl("https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json");
//        GenericUrl url = new GenericUrl("https://api.fitbit.com/1/user/-/activities/heart/date/today/1d/1min.json");
//        HttpRequest request = requestFactory.buildGetRequest(url);
//        Gson gson = GsonFactory.getDefaultFactory().getGson();
//        jsonFromFitbitHeart fitbitObject = gson.fromJson(request.execute().parseAsString(), jsonFromFitbitHeart.class);
//        System.out.print(fitbitObject.getLastHeartRate());
//        System.out.println(request.execute().parseAsString());
//
//    }
    private static JsonFromFitbitHeart sendHttpRequest(HttpRequestFactory requestFactory) throws IOException
    {
        GenericUrl url = new GenericUrl("https://api.fitbit.com/1/user/-/activities/heart/date/today/1d/1sec.json");
        HttpRequest request = requestFactory.buildGetRequest(url);
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        return gson.fromJson(request.execute().parseAsString(), JsonFromFitbitHeart.class);
    }

//    public static void main(String[] args) {
//        try {
//            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
//            final Credential credential = authorize();
//            HttpRequestFactory requestFactory =
//                    HTTP_TRANSPORT.createRequestFactory(request ->
//                    {
//                        credential.initialize(request);
//                        request.setParser(new JsonObjectParser(JSON_FACTORY));
//                    });
//
//            run(requestFactory);
//
//            // Success!
//            return;
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//        System.exit(1);
//    }

    public static int getHeartRate()
    {
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            final Credential credential = authorize();
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(request ->
                    {
                        credential.initialize(request);
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    });

            return sendHttpRequest(requestFactory).getLastHeartRate();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 0;
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }
}
