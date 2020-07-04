package milkman.slackbot;

import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.SectionBlock.SectionBlockBuilder;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.view.View;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.slackbot.preview.Previews;
import milkman.slackbot.preview.RequestPreview;
import milkman.slackbot.templates.Templates;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asContextElements;
import static com.slack.api.model.view.Views.*;

public class RequestRenderer {

    @NotNull
    public SectionBlock renderRequestSimplePreview(RequestContainer request) {
        return section(section -> buildRequestHeader(section, request));
    }

    @NotNull
    public List<LayoutBlock> renderRequestPreview(
            String requestUserId,
            URI privateBinUrl,
            RequestContainer request) {
        return asBlocks(
                header(requestUserId, privateBinUrl, request),
                section(s -> s.text(plainText("Request Preview"))),
                section(s -> buildRequestHeader(s, request)));
    }

    public ContextBlock header(String requestUserId, URI privateBinUrl, RequestContainer request) {
        var prevType = Previews.previewFor(request.getClass())
                .map(RequestPreview::getType)
                .orElse("Unknown");

        return context(asContextElements(
                markdownText(prevType + " request Shared by <@" + requestUserId + ">"),
                markdownText("stored at <" + privateBinUrl + "|" + privateBinUrl.getHost() + ">")
        ));
    }


    public ContextBlock footer() {
        return context(asContextElements(
            markdownText("powered by <https://github.com/warmuuh/milkman|Milkman>")
    ));
  }

    public View buildRequestView(String renderingMethod, String privateBinUrl, RequestContainer request) {
        return view(view -> view
                .callbackId("unused")
                .type("modal")
                .notifyOnClose(false)
                .title(viewTitle(
                        title -> title.type("plain_text").text("Request: " + renderingMethod).emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Close").emoji(true)))
                .blocks(asBlocks(
                        section(section -> buildRequestHeader(section, request)),
                        divider(),
                    section(s -> buildRequestBody(s, renderingMethod, request))
            ))
    );
  }

    @SneakyThrows
    private SectionBlockBuilder buildRequestHeader(SectionBlockBuilder section,
                                                   RequestContainer request) {

        return Previews.previewFor(request.getClass())
                .map(p -> p.getPreview(request))
                .map(this::toMarkDown)
                .map(section::text)
                .orElseGet(() -> section.text(plainText("Unknown request-type")));
    }

    private MarkdownTextObject toMarkDown(List<RequestPreview.PreviewEntry> requestPreview) {
        return markdownText(requestPreview.stream()
                .map(e -> e.getName() + ": `" + StringUtils.abbreviate(e.getValue(), 100) + "`")
                .collect(Collectors.joining("\n"))
        );
    }

    private SectionBlockBuilder buildRequestBody(SectionBlockBuilder section,
                                                 String renderingMethod,
                                                 RequestContainer request) {
        return Templates.templateFor(renderingMethod, request.getClass())
                .map(t -> t.renderRequest(request))
                .map(content -> markdownText("```\n" + content + "```"))
                .map(section::text)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported rendering method"));
    }

}
