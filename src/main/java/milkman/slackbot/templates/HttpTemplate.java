package milkman.slackbot.templates;

import milkman.ui.plugin.rest.curl.HttpTextExport;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class HttpTemplate extends RequestTemplate<RestRequestContainer> {

    public HttpTemplate() {
        super("Http");
    }

    @Override
    public String renderRequest(RestRequestContainer requestContainer) {
        var exporter = new HttpTextExport();
        return exporter.export(false, requestContainer, s -> s);
    }

}
