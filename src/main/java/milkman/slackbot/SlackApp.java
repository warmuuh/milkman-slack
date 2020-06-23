package milkman.slackbot;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.asOptions;
import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static com.slack.api.model.block.element.BlockElements.staticSelect;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.WebEndpointContext;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.WebEndpointRequest;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.webhook.WebhookResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class SlackApp {

  private final App app;
  private final RequestRenderer renderer;
  private final RequestLoader loader;

  public SlackApp() {

    loader = new RequestLoader();
    renderer = new RequestRenderer();
    app = new App()
        .use(new DebugMiddleware());

    app.endpoint("/", this::serveIndexPage);
    app.command("/milkman", this::handleSlashCommand);
    app.blockAction(Pattern.compile("share-request-.*"), this::handleShareRequestAction);
    app.blockAction(Pattern.compile("render-request-.*"), this::handleRenderRequestAction);

  }

  @SneakyThrows
  private Response serveIndexPage(WebEndpointRequest webEndpointRequest, WebEndpointContext webEndpointContext) {
    return new Response(200, "text/html", Map.of(), IOUtils.toString(getClass().getResource("/index.html")));
  }

  public App getApp() {
    return app;
  }

  @SneakyThrows
  private Response handleSlashCommand(SlashCommandRequest req, SlashCommandContext ctx) {
    var text = req.getPayload().getText();

    URI privateBinUrl;
    try {
      privateBinUrl = new URI(text);
    } catch (URISyntaxException e) {
      log.error("Failed to read url from command: " + e);
      return ctx.ack("'" + text + "' not a valid privatebin url");
    }

    RestRequestContainer request = loader.loadRequest(privateBinUrl);

    List<LayoutBlock> blocks = new LinkedList<>();
    blocks.addAll(renderer.renderRequestPreview(privateBinUrl, request));
    blocks.add(divider());
    blocks.add(actions(actions -> actions.elements(getRequestPreviewActions(privateBinUrl))));

    WebhookResponse result = ctx.respond(res -> res
        .responseType("ephemeral") // or "in_channnel"
        .blocks(blocks)
    );

    return ctx.ack();
  }


  @SneakyThrows
  private Response handleShareRequestAction(BlockActionRequest req, ActionContext ctx) {
    boolean isShare = "share-request-share"
        .equals(req.getPayload().getActions().get(0).getActionId());
    String privatebinUrl = req.getPayload().getActions().get(0).getValue();
    RestRequestContainer request = loader.loadRequest(new URI(privatebinUrl));

    if (isShare) {
      ctx.respond(res -> res
          .deleteOriginal(true)
          .responseType("in_channel")
          .blocks(asBlocks(
              renderer.renderRequestSimplePreview(request),
              actions(actions -> actions.elements(getRequestActions(privatebinUrl)))
          ))
      );
    } else {
      ctx.respond(res -> res.deleteOriginal(true));
    }

    return ctx.ack();
  }

  @SneakyThrows
  private Response handleRenderRequestAction(BlockActionRequest req, ActionContext ctx) {
    String renderingMethod = req.getPayload().getActions().get(0).getSelectedOption().getText()
        .getText();
    String privatebinUrlEnc = req.getPayload().getActions().get(0).getActionId()
        .substring("render-request-".length());
    String privatebinUrl = new String(Base64.getDecoder().decode(privatebinUrlEnc));

    RestRequestContainer request = loader.loadRequest(new URI(privatebinUrl));

    ctx.client().viewsOpen(r -> r
        .triggerId(ctx.getTriggerId())
        .view(renderer.buildRequestView(renderingMethod, privatebinUrl, request)));

    return ctx.ack();
  }



  @NotNull
  private static List<BlockElement> getRequestActions(String privatebinUrl) {
    String encUrl = Base64.getEncoder().encodeToString(privatebinUrl.getBytes());
    return asElements(
        staticSelect(s -> s.actionId("render-request-" + encUrl)
            .placeholder(plainText("Select Rendering method"))
            .options(asOptions(
                option(plainText("Http"), "Http"),
                option(plainText("Curl"), "Curl")
            )))
    );
  }

  @NotNull
  private static List<BlockElement> getRequestPreviewActions(URI privatebinUrl) {
    return asElements(
        button(b -> b.actionId("share-request-share")
            .text(plainText(pt -> pt.text("Share"))).value(privatebinUrl.toString())),
        button(b -> b.actionId("share-request-dismiss")
            .text(plainText(pt -> pt.text("Dismiss"))).value("dismiss"))
    );
  }
}
