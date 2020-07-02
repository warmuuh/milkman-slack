package milkman.slackbot.db;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Optional;

@RequiredArgsConstructor
public class StateDatabase {

    private final Database db;

    @SneakyThrows
    public Optional<String> loadStateData(String state) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("SELECT data FROM states WHERE state = ?");
            stmt.setString(1, state);

            var resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(resultSet.getString(0));
        }
    }

    @SneakyThrows
    public void updateStateData(String state, String data) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("UPDATE states SET data = ? WHERE state = ?");
            stmt.setString(1, data);
            stmt.setString(2, state);
            stmt.execute();
        }
    }

    @SneakyThrows
    public void addStateData(String state, String data) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("INSERT INTO states (state, data) VALUES (?,?)");
            stmt.setString(1, state);
            stmt.setString(2, data);
            stmt.execute();
        }
    }

    @SneakyThrows
    public void deleteStateData(String state) {
        try (var connection = db.getConnection()) {
            var stmt = connection.prepareStatement("DELETE FROM states WHERE state = ?");
            stmt.setString(1, state);
            stmt.execute();
        }
    }

}
