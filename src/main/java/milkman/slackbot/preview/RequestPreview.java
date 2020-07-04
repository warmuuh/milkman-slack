package milkman.slackbot.preview;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import milkman.domain.RequestContainer;

import java.lang.reflect.ParameterizedType;
import java.util.List;

@RequiredArgsConstructor
public abstract class RequestPreview<T extends RequestContainer> {

    @Getter
    private final String type;

    public abstract List<PreviewEntry> getPreview(T requestContainer);

    public boolean supports(Class<? extends RequestContainer> type) {
        return getRequestContainerType().isAssignableFrom(type);
    }

    private Class<T> getRequestContainerType() {
        return ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    @Value
    public static class PreviewEntry {
        String name;
        String value;
    }

}
