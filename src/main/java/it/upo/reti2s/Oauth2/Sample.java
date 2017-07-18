package it.upo.reti2s.Oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
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
import java.io.IOException;
import java.util.Arrays;


/**
 * A sample application that demonstrates how the Google OAuth2 library can be used to authenticate
 * against Daily Motion.
 *
 * @author Ravi Mistry
 */
public class Sample {

    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/dailymotion_sample");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * OAuth 2 scope.
     */
    private static final String SCOPE = "weight";

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


//    private static void run(HttpRequestFactory requestFactory) throws IOException {
//        DailyMotionUrl url = new DailyMotionUrl("https://api.dailymotion.com/videos/favorites");
//        url.setFields("id,tags,title,url");
//
//        HttpRequest request = requestFactory.buildGetRequest(url);
//        VideoFeed videoFeed = request.execute().parseAs(VideoFeed.class);
//        if (videoFeed.list.isEmpty()) {
//            System.out.println("No favorite videos found.");
//        } else {
//            if (videoFeed.hasMore) {
//                System.out.print("First ");
//            }
//            System.out.println(videoFeed.list.size() + " favorite videos found:");
//            for (Video video : videoFeed.list) {
//                System.out.println();
//                System.out.println("-----------------------------------------------");
//                System.out.println("ID: " + video.id);
//                System.out.println("Title: " + video.title);
//                System.out.println("Tags: " + video.tags);
//                System.out.println("URL: " + video.url);
//            }
//        }
//    }

    private static void run(HttpRequestFactory requestFactory) throws IOException {


        System.out.print("RUN");
    }

    public static void main(String[] args) {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            final Credential credential = authorize();
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                            credential.initialize(request);
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });

            run(requestFactory);

            // Success!
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(1);
    }
}
