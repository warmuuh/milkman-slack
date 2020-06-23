package milkman.slackbot;

import com.slack.api.bolt.jetty.SlackAppServer;


public class Application {

    public static void main(String[] args) throws Exception {
        SlackApp slackApp = new SlackApp();
        SlackAppServer server = new SlackAppServer(slackApp.getApp(), getPort());
        server.start();
    }


    private static int getPort() {
        var port = System.getenv("PORT");
        if (port == null)
            return 3000;

        return Integer.parseInt(port);
    }


}
