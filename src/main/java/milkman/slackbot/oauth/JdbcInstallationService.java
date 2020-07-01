package milkman.slackbot.oauth;

import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.model.builtin.DefaultInstaller;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.bolt.util.JsonOps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.slackbot.db.BotDatabase;
import milkman.slackbot.db.InstallerDatabase;

@Slf4j
@RequiredArgsConstructor
public class JdbcInstallationService implements InstallationService {

    private final AppConfig config;
    private final InstallerDatabase installers;
    private final BotDatabase bots;

    private boolean historicalDataEnabled;

    @Override
    public boolean isHistoricalDataEnabled() {
        return historicalDataEnabled;
    }

    @Override
    public void setHistoricalDataEnabled(boolean isHistoricalDataEnabled) {
        this.historicalDataEnabled = isHistoricalDataEnabled;
    }

    @Override
    public void saveInstallerAndBot(Installer installer) throws Exception {
        installers.addInstallerData(config.getClientId(), installer.getEnterpriseId(), installer.getTeamId(), JsonOps.toJsonString(installer));
        bots.addBotsData(config.getClientId(), installer.getEnterpriseId(), installer.getTeamId(), JsonOps.toJsonString(installer));
    }

    @Override
    public void deleteBot(Bot bot) throws Exception {
        bots.deleteBotsData(config.getClientId(), bot.getEnterpriseId(), bot.getTeamId());
    }

    @Override
    public void deleteInstaller(Installer installer) throws Exception {
        installers.deleteInstallerData(config.getClientId(), installer.getEnterpriseId(), installer.getTeamId());
        bots.deleteBotsData(config.getClientId(), installer.getEnterpriseId(), installer.getTeamId());
    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        return bots.loadBotsData(config.getClientId(), enterpriseId, teamId)
                .map(json -> JsonOps.fromJson(json, DefaultBot.class))
                .orElseGet(() -> {
                    log.warn("Failed to load bot for enterprise id {}, team id {}", enterpriseId, teamId);
                    return null;
                });
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String userId) {
        return installers.loadInstallerData(config.getClientId(), enterpriseId, teamId)
                .map(json -> JsonOps.fromJson(json, DefaultInstaller.class))
                .orElseGet(() -> {
                    log.warn("Failed to load bot for enterprise id {}, team id {}", enterpriseId, teamId);
                    return null;
                });
    }

}
