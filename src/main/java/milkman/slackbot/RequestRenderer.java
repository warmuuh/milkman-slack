package milkman.slackbot;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asContextElements;
import static com.slack.api.model.view.Views.view;
import static com.slack.api.model.view.Views.viewClose;
import static com.slack.api.model.view.Views.viewTitle;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.SectionBlock.SectionBlockBuilder;
import com.slack.api.model.view.View;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.jetbrains.annotations.NotNull;

public class RequestRenderer {

  @NotNull
  public SectionBlock renderRequestSimplePreview(RestRequestContainer request) {
    return section(section -> buildRequestHeader(section, request));
  }

  @NotNull
  public List<LayoutBlock> renderRequestPreview(URI privateBinUrl,
      RestRequestContainer request) {
    return asBlocks(
        context(asContextElements(markdownText(
            "Request stored at <" + privateBinUrl + "|" + privateBinUrl.getHost() + ">"))),
        section(s -> s.text(plainText("Request Preview"))),
        section(s -> buildRequestHeader(s, request)));
  }

  public View buildRequestView(String renderingMethod, String privateBinUrl, RestRequestContainer request) {
    return view(view -> view
        .callbackId("meeting-arrangement")
        .type("modal")
        .notifyOnClose(false)
        .title(viewTitle(
            title -> title.type("plain_text").text("Request: " + renderingMethod).emoji(true)))
        .close(viewClose(close -> close.type("plain_text").text("Close").emoji(true)))
        .blocks(asBlocks(
            section(section -> buildRequestHeader(section, request)),
            section(s -> s.text(plainText(privateBinUrl))),
            divider(),
            section(s -> buildRequestBody(s, renderingMethod))
        ))
    );
  }

  @SneakyThrows
  private SectionBlockBuilder buildRequestHeader(SectionBlockBuilder section,
      RestRequestContainer request) {
    URI uri = new URI(request.getUrl());

    StringBuilder b = new StringBuilder();
    if (uri.getPath() != null) {
      b.append(uri.getPath());
    } else {
      b.append("/");
    }

    if (uri.getQuery() != null) {
      b.append(uri.getQuery());
    }
    if (uri.getFragment() != null) {
      b.append(uri.getFragment());
    }

    return section
        .text(markdownText("Name: `" + request.getName() + "`\n"
            + "Request: `" + request.getHttpMethod() + " " + b + "`\n"
            + "Host: `" + uri.getHost() + "`"));
  }

  private SectionBlockBuilder buildRequestBody(SectionBlockBuilder section,
      String renderingMethod) {
    return section
        .text(markdownText("```\n" + renderingMethod + " /asdasd asd \n asdasd \n asdasd```"));
  }


}
