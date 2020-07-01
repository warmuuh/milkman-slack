package milkman.slackbot.db;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Optional;

@RequiredArgsConstructor
public class BotDatabase {

    private final Database db;

    @SneakyThrows
    public Optional<String> loadBotsData(String clientId, String enterpriseId, String teamId) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("SELECT data FROM bots WHERE client_id = ? and team_id = ? and enterprise_id = ?");
            stmt.setString(1, clientId);
            stmt.setString(2, teamId);
            stmt.setString(3, enterpriseId);

            var resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(resultSet.getString(0));
        }
    }

    @SneakyThrows
    public void updateBotsData(String clientId, String enterpriseId, String teamId, String data) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("UPDATE bots SET data = ? WHERE client_id = ? and team_id = ? and enterprise_id = ?");
            stmt.setString(1, data);
            stmt.setString(2, clientId);
            stmt.setString(3, teamId);
            stmt.setString(4, enterpriseId);

            if (!stmt.execute()) {
                throw new IllegalArgumentException("Nothing updated");
            }
        }
    }

    @SneakyThrows
    public void addBotsData(String clientId, String enterpriseId, String teamId, String data) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("INSERT INTO bots (data, client_id, team_id, enterprise_id) VALUES (?,?,?,?)");
            stmt.setString(1, data);
            stmt.setString(2, clientId);
            stmt.setString(3, teamId);
            stmt.setString(4, enterpriseId);

            if (!stmt.execute()) {
                throw new IllegalArgumentException("Nothing inserted");
            }
        }
    }

    @SneakyThrows
    public void deleteBotsData(String clientId, String enterpriseId, String teamId) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("DELETE FROM bots WHERE client_id = ? and team_id = ? and enterprise_id = ?");
            stmt.setString(1, clientId);
            stmt.setString(2, teamId);
            stmt.setString(3, enterpriseId);

            if (!stmt.execute()) {
                throw new IllegalArgumentException("Nothing deleted");
            }
        }
    }

}
