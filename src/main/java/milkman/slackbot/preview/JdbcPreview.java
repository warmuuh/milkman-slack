package milkman.slackbot.preview;

import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;

import java.util.List;

public class JdbcPreview extends RequestPreview<JdbcRequestContainer> {

    public JdbcPreview() {
        super("Sql");
    }

    @Override
    public List<PreviewEntry> getPreview(JdbcRequestContainer request) {
        var aspect = request.getAspect(JdbcSqlAspect.class)
                .orElseThrow(() -> new IllegalArgumentException("Missing Aspect"));

        return List.of(
                new PreviewEntry("Name", request.getName()),
                new PreviewEntry("Sql", aspect.getSql()),
                new PreviewEntry("Database", request.getJdbcUrl())
        );
    }
}
