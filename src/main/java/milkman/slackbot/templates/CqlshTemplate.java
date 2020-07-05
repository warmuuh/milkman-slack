package milkman.slackbot.templates;

import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.plugin.cassandra.export.CqlshTextExport;

public class CqlshTemplate extends RequestTemplate<CassandraRequestContainer> {
    public CqlshTemplate() {
        super("Cqlsh");
    }

    @Override
    public String renderRequest(CassandraRequestContainer requestContainer) {
        var export = new CqlshTextExport();
        return export.export(false, requestContainer, s -> s);
    }
}
