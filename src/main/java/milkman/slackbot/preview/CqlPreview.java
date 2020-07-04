package milkman.slackbot.preview;

import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;

import java.util.List;

public class CqlPreview extends RequestPreview<CassandraRequestContainer> {

    public CqlPreview() {
        super("Cql");
    }

    @Override
    public List<PreviewEntry> getPreview(CassandraRequestContainer request) {
        var aspect = request.getAspect(JdbcSqlAspect.class)
                .orElseThrow(() -> new IllegalArgumentException("Missing Aspect"));

        return List.of(
                new PreviewEntry("Name", request.getName()),
                new PreviewEntry("Sql", aspect.getSql()),
                new PreviewEntry("Database", request.getCassandraUrl())
        );
    }
}
