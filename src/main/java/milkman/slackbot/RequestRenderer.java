package milkman.slackbot;

import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.SectionBlock.SectionBlockBuilder;
import com.slack.api.model.view.View;
import lombok.SneakyThrows;
import milkman.ui.plugin.rest.curl.CurlTextExport;
import milkman.ui.plugin.rest.curl.HttpTextExport;
import milkman.ui.plugin.rest.curl.TextExport;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asContextElements;
import static com.slack.api.model.view.Views.*;

public class RequestRenderer {

  private Map<String, TextExport> exporters = Map.of(
          "curl", new CurlTextExport(),
          "http", new HttpTextExport()
  );

  @NotNull
  public SectionBlock renderRequestSimplePreview(RestRequestContainer request) {
    return section(section -> buildRequestHeader(section, request));
  }

  @NotNull
  public List<LayoutBlock> renderRequestPreview(
          String requestUserId,
          URI privateBinUrl,
          RestRequestContainer request) {
    return asBlocks(
            header(requestUserId, privateBinUrl),
            section(s -> s.text(plainText("Request Preview"))),
            section(s -> buildRequestHeader(s, request)));
  }

  public ContextBlock header(String requestUserId, URI privateBinUrl) {
    return context(asContextElements(
            markdownText("Request Shared by <@" + requestUserId + ">"),
            markdownText("stored at <" + privateBinUrl + "|" + privateBinUrl.getHost() + ">")
    ));
  }


  public ContextBlock footer() {
    return context(asContextElements(
            markdownText("powered by <https://github.com/warmuuh/milkman|Milkman>")
    ));
  }

  public View buildRequestView(String renderingMethod, String privateBinUrl, RestRequestContainer request) {
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
      RestRequestContainer request) {
    URI uri = new URI(request.getUrl());

    StringBuilder b = new StringBuilder();
    if (StringUtils.isNotBlank(uri.getPath())) {
      b.append(uri.getPath());
    } else {
      b.append("/");
    }

    if (StringUtils.isNotBlank(uri.getQuery())) {
      b.append(uri.getQuery());
    }

    return section
        .text(markdownText("Name: `" + request.getName() + "`\n"
            + "Request: `" + request.getHttpMethod() + " " + b + "`\n"
            + "Host: `" + uri.getHost() + "`"));
  }

  private SectionBlockBuilder buildRequestBody(SectionBlockBuilder section,
                                               String renderingMethod,
                                               RestRequestContainer request) {

    var export = exporters.get(renderingMethod.toLowerCase());
    var exported = export == null
            ? "undefined export"
            : export.export(false, request, s -> s);

    return section
            .text(markdownText("```\n" + exported + "```"));
  }


}
