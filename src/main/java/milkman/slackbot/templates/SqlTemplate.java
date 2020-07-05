package milkman.slackbot.templates;

import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;

public class SqlTemplate extends RequestTemplate<JdbcRequestContainer> {

    public SqlTemplate() {
        super("Sql");
    }

    @Override
    public String renderRequest(JdbcRequestContainer requestContainer) {
        StringBuilder b = new StringBuilder();
        b.append("-- ").append(requestContainer.getJdbcUrl()).append("\n");

        requestContainer.getAspect(JdbcSqlAspect.class).ifPresent(a -> {
            b.append(a.getSql());
        });

        return b.toString();
    }
}
