package milkman.slackbot.preview;

import milkman.domain.RequestContainer;

import java.util.List;
import java.util.Optional;

public class Previews {

    private static List<RequestPreview<?>> previews = List.of(
            new HttpPreview(),
            new JdbcPreview(),
            new GrpcPreview(),
            new GqlPreview(),
            new CqlPreview()
    );

    public static <T2 extends RequestContainer> Optional<RequestPreview> previewFor(Class<T2> requestType) {
        for (RequestPreview<?> preview : previews) {
            if (preview.supports(requestType))
                return Optional.of(preview);
        }
        return Optional.empty();
    }
}
