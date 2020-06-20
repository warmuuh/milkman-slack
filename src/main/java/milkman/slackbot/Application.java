package milkman.slackbot;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;

public class Application {
    public static void main(String[] args) throws Exception {
        // App expects env variables (SLACK_BOT_TOKEN, SLACK_SIGNING_SECRET)
        App app = new App();

        app.command("/milkman", (req, ctx) -> {
            return ctx.ack(":wave: Hello!");
        });

        SlackAppServer server = new SlackAppServer(app, getPort());
        server.start();
    }


    private static int getPort(){
        var port = System.getenv("PORT");
        if (port == null)
            return 3000;

        return Integer.parseInt(port);
    }
}
