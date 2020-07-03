package milkman.slackbot;

import com.slack.api.bolt.jetty.SlackAppServer;
import lombok.extern.slf4j.Slf4j;
import milkman.slackbot.db.Database;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Application {

    public static void main(String[] args) throws Exception {
        var db = new Database();
        db.executeInitScript();

        SlackApp slackApp = new SlackApp(db);
        OauthSlackApp oauthSlackApp = new OauthSlackApp(db);
        SlackAppServer server = new SlackAppServer(
                new HashMap<>(Map.of(
                        "/slack/events", slackApp.getApp(),
                        "/slack/oauth", oauthSlackApp.getApp())),
                getPort());

        var field = slackApp.getApp().getClass().getDeclaredField("installationService");
        field.setAccessible(true);
        log.info("Installation-service: " + field.get(slackApp.getApp()).getClass());
        server.start();
    }


    private static int getPort() {
        var port = System.getenv("PORT");
        if (port == null)
            return 3000;

        return Integer.parseInt(port);
    }


}
