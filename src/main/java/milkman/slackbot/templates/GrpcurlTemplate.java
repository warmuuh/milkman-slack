package milkman.slackbot.templates;

import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.export.GrpcurlTextExport;

public class GrpcurlTemplate extends RequestTemplate<GrpcRequestContainer> {
    public GrpcurlTemplate() {
        super("Cqlsh");
    }

    @Override
    public String renderRequest(GrpcRequestContainer requestContainer) {
        var export = new GrpcurlTextExport();
        return export.export(false, requestContainer, s -> s);
    }
}
