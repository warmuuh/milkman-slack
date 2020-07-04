package milkman.slackbot.preview;

import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.List;

public class HttpPreview extends RequestPreview<RestRequestContainer> {

    public HttpPreview() {
        super("Http");
    }

    @Override
    public List<PreviewEntry> getPreview(RestRequestContainer request) {
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
                new PreviewEntry("Request", request.getHttpMethod() + " " + b),
                new PreviewEntry("Host", uri.getHost())
        );
    }
}
