package milkman.slackbot.templates;

import milkman.ui.plugin.rest.curl.CurlTextExport;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class CurlTemplate extends RequestTemplate<RestRequestContainer> {

    public CurlTemplate() {
        super("Curl");
    }

    @Override
    public String renderRequest(RestRequestContainer requestContainer) {
        var exporter = new CurlTextExport();
        return exporter.export(false, requestContainer, s -> s);
    }

}
