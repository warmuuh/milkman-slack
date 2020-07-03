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

import static java.util.Optional.ofNullable;

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
        installers.addInstallerData(config.getClientId(), ofNullable(installer.getEnterpriseId()), installer.getTeamId(), JsonOps.toJsonString(installer));
        bots.addBotsData(config.getClientId(), ofNullable(installer.getEnterpriseId()), installer.getTeamId(), JsonOps.toJsonString(installer));
    }

    @Override
    public void deleteBot(Bot bot) throws Exception {
        bots.deleteBotsData(config.getClientId(), ofNullable(bot.getEnterpriseId()), bot.getTeamId());
    }

    @Override
    public void deleteInstaller(Installer installer) throws Exception {
        installers.deleteInstallerData(config.getClientId(), ofNullable(installer.getEnterpriseId()), installer.getTeamId());
        bots.deleteBotsData(config.getClientId(), ofNullable(installer.getEnterpriseId()), installer.getTeamId());
    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        log.info("Finding bot: " + enterpriseId + " / " + teamId);
        System.out.println("SOUT: Finding bot: " + enterpriseId + " / " + teamId);
        return bots.loadBotsData(config.getClientId(), ofNullable(enterpriseId), teamId)
                .map(json -> JsonOps.fromJson(json, DefaultBot.class))
                .orElseGet(() -> {
                    log.warn("Failed to load bot for enterprise id {}, team id {}", enterpriseId, teamId);
                    return null;
                });
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String userId) {
        log.info("Finding installer: " + enterpriseId + " / " + teamId);
        System.out.println("SOUT: Finding installer: " + enterpriseId + " / " + teamId);
        return installers.loadInstallerData(config.getClientId(), ofNullable(enterpriseId), teamId)
                .map(json -> JsonOps.fromJson(json, DefaultInstaller.class))
                .orElseGet(() -> {
                    log.warn("Failed to load bot for enterprise id {}, team id {}", enterpriseId, teamId);
                    return null;
                });
    }

    @Override
    public String getInstallationGuideText(String enterpriseId, String teamId, String userId) {
        log.warn("failed to find bot/installer for enterprise: " + enterpriseId + ", team: " + teamId);
        return "failed to find bot/installer for enterprise: " + enterpriseId + ", team: " + teamId;
    }
}
