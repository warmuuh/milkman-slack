package milkman.slackbot.templates;

import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.plugin.graphql.export.HttpTextExport;

public class GqlHttpTemplate extends RequestTemplate<GraphqlRequestContainer> {

    public GqlHttpTemplate() {
        super("Http");
    }

    @Override
    public String renderRequest(GraphqlRequestContainer requestContainer) {
        var exporter = new HttpTextExport();
        return exporter.export(false, requestContainer, s -> s);
    }

}
