package milkman.slackbot;

import com.slack.api.bolt.App;
import lombok.Getter;
import milkman.slackbot.db.BotDatabase;
import milkman.slackbot.db.Database;
import milkman.slackbot.db.InstallerDatabase;
import milkman.slackbot.db.StateDatabase;
import milkman.slackbot.oauth.JdbcInstallationService;
import milkman.slackbot.oauth.JdbcOAuthStateService;

@Getter
public class OauthSlackApp {

    private final App app;

    public OauthSlackApp(Database db) {
        this.app = new App().asOAuthApp(true);

        var installers = new InstallerDatabase(db);
        var bots = new BotDatabase(db);
        app.service(new JdbcInstallationService(app.config(), installers, bots));

        var states = new StateDatabase(db);
        app.service(new JdbcOAuthStateService(states));
    }
}
