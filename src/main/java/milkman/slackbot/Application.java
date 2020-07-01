package milkman.slackbot;

import com.slack.api.bolt.jetty.SlackAppServer;

import java.util.Map;


public class Application {

    public static void main(String[] args) throws Exception {
        SlackApp slackApp = new SlackApp();
        OauthSlackApp oauthSlackApp = new OauthSlackApp();
        SlackAppServer server = new SlackAppServer(Map.of(
                "/slack/events", slackApp.getApp(),
                "/slack/oauth", oauthSlackApp.getApp()),
                getPort());
        server.start();
    }


    private static int getPort() {
        var port = System.getenv("PORT");
        if (port == null)
            return 3000;

        return Integer.parseInt(port);
    }


}
