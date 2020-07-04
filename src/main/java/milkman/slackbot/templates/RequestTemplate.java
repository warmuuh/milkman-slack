package milkman.slackbot.templates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;

import java.lang.reflect.ParameterizedType;

@RequiredArgsConstructor
public abstract class RequestTemplate<T extends RequestContainer> {

    @Getter
    private final String name;

    public abstract String renderRequest(T requestContainer);

    public boolean supports(Class<? extends RequestContainer> type) {
        return getRequestContainerType().isAssignableFrom(type);
    }

    private Class<T> getRequestContainerType() {
        return ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

}
