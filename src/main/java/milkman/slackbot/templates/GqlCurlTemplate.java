package milkman.slackbot.templates;

import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.plugin.graphql.export.CurlTextExport;

public class GqlCurlTemplate extends RequestTemplate<GraphqlRequestContainer> {

    public GqlCurlTemplate() {
        super("Curl");
    }

    @Override
    public String renderRequest(GraphqlRequestContainer requestContainer) {
        var exporter = new CurlTextExport();
        return exporter.export(false, requestContainer, s -> s);
    }

}
