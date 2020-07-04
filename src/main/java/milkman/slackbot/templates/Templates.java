package milkman.slackbot.templates;

import milkman.domain.RequestContainer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Templates {

    private static List<RequestTemplate> templates = List.of(
            new CurlTemplate(),
            new HttpTemplate()
    );

    public static <T extends RequestContainer> Optional<RequestTemplate> templateFor(String name, Class<T> type) {
        for (RequestTemplate template : templates) {
            if (template.getName().equals(name) && template.supports(type)) {
                return Optional.of(template);
            }
        }
        return Optional.empty();
    }

    public static <T extends RequestContainer> List<RequestTemplate> templateFor(Class<T> type) {
        return templates.stream()
                .filter(t -> t.supports(type))
                .collect(Collectors.toList());
    }


}
