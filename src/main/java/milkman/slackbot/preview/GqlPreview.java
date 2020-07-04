package milkman.slackbot.preview;

import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.List;

public class GqlPreview extends RequestPreview<GraphqlRequestContainer> {

    public GqlPreview() {
        super("GraphQl");
    }

    @Override
    public List<PreviewEntry> getPreview(GraphqlRequestContainer request) {
        URI uri = URI.create(request.getUrl());

        StringBuilder b = new StringBuilder();
        if (StringUtils.isNotBlank(uri.getPath())) {
            b.append(uri.getPath());
        } else {
            b.append("/");
        }

        if (StringUtils.isNotBlank(uri.getQuery())) {
            b.append("?").append(uri.getQuery());
        }

        return List.of(
                new PreviewEntry("Name", request.getName()),
                new PreviewEntry("Request", "POST " + b),
                new PreviewEntry("Host", uri.getHost())
        );
    }
}
