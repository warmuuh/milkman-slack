package milkman.slackbot.preview;

import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;

import java.util.List;

public class GrpcPreview extends RequestPreview<GrpcRequestContainer> {

    public GrpcPreview() {
        super("gRPC");
    }

    @Override
    public List<PreviewEntry> getPreview(GrpcRequestContainer request) {
        var aspect = request.getAspect(GrpcOperationAspect.class)
                .orElseThrow(() -> new IllegalArgumentException("Missing Aspect"));

        return List.of(
                new PreviewEntry("Name", request.getName()),
                new PreviewEntry("Operation", aspect.getOperation()),
                new PreviewEntry("Endpoint", request.getEndpoint())
        );
    }
}
