package milkman.slackbot.oauth;

import com.slack.api.bolt.service.OAuthStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.slackbot.db.StateDatabase;

/**
 * OAuthStateService implementation using local file system.
 */
@Slf4j
@RequiredArgsConstructor
public class JdbcOAuthStateService implements OAuthStateService {
    private final StateDatabase states;

    @Override
    public void addNewStateToDatastore(String state) throws Exception {
        String value = "" + (System.currentTimeMillis() + getExpirationInSeconds() * 1000);
        states.addStateData(state, value);
    }

    @Override
    public boolean isAvailableInDatabase(String state) {
        log.warn("Disabled state param for now");
        return true;
//        Long millisToExpire = states.loadStateData(state)
//                .map(Long::valueOf)
//                .orElseGet(() -> {
//                    log.warn("State {} not found", state);
//                    return null;
//                });
//        return millisToExpire != null && millisToExpire > System.currentTimeMillis();
    }

    @Override
    public void deleteStateFromDatastore(String state) throws Exception {
        states.deleteStateData(state);
    }
}
