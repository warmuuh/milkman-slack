package milkman.slackbot;

import com.slack.api.bolt.App;
import lombok.Getter;
import milkman.slackbot.db.Database;
import milkman.slackbot.db.StateDatabase;
import milkman.slackbot.oauth.JdbcOAuthStateService;

@Getter
public class OauthSlackApp {

    private final App app;

    public OauthSlackApp(Database db) {
        this.app = new App().asOAuthApp(true);
        var states = new StateDatabase(db);
        app.service(new JdbcOAuthStateService(states));
    }
}
