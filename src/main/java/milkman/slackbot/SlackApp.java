package milkman.slackbot;

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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.slackbot.db.BotDatabase;
import milkman.slackbot.db.Database;
import milkman.slackbot.db.InstallerDatabase;
import milkman.slackbot.oauth.JdbcInstallationService;
import milkman.slackbot.templates.Templates;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;

@Slf4j
public class SlackApp {

  private final App app;
  private final RequestRenderer renderer;
  private final RequestLoader loader;
  private final ExecutorService executor = Executors.newCachedThreadPool();

  public SlackApp(Database db) {

    loader = new RequestLoader();
    renderer = new RequestRenderer();
    app = new App();

    app.endpoint("/", this::serveIndexPage);

    var installers = new InstallerDatabase(db);
    var bots = new BotDatabase(db);
    app.service(new JdbcInstallationService(app.config(), installers, bots));

    app.command("/milkman", this::handleSlashCommand);
    app.blockAction(Pattern.compile("share-request-.*"), this::handleShareRequestAction);
    app.blockAction(Pattern.compile("render-request-.*"), this::handleRenderRequestAction);
    app.blockAction("dismiss-request", this::handleDismissRequestAction);

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

    executor.submit(t(() -> {
      RequestContainer request = loader.loadRequest(privateBinUrl);

      List<LayoutBlock> blocks = new LinkedList<>();
      blocks.addAll(renderer.renderRequestPreview(ctx.getRequestUserId(), privateBinUrl, request));
      blocks.add(divider());
      blocks.add(actions(actions -> actions.elements(getRequestPreviewActions(privateBinUrl))));

      WebhookResponse result = ctx.respond(res -> res
              .responseType("ephemeral") // or "in_channnel"
              .blocks(blocks)
      );
    }));
    return ctx.ack();
  }


  @SneakyThrows
  private Response handleShareRequestAction(BlockActionRequest req, ActionContext ctx) {
    boolean isShare = "share-request-share"
            .equals(req.getPayload().getActions().get(0).getActionId());
    String privatebinUrlStr = req.getPayload().getActions().get(0).getValue();
    var privatebinUrl = new URI(privatebinUrlStr);

    executor.submit(t(() -> {
      if (isShare) {
        RequestContainer request = loader.loadRequest(privatebinUrl);
        ctx.respond(res -> res
                .deleteOriginal(true)
                .responseType("in_channel")
                .blocks(asBlocks(
                        renderer.header(ctx.getRequestUserId(), privatebinUrl, request),
                        renderer.renderRequestSimplePreview(request),
                        actions(actions -> actions.elements(getRequestActions(privatebinUrlStr, request))),
                        renderer.footer()
                ))
        );
      } else {
        ctx.respond(res -> res.deleteOriginal(true));
      }
    }));

    return ctx.ack();
  }

  @SneakyThrows
  private Response handleRenderRequestAction(BlockActionRequest req, ActionContext ctx) {
    String renderingMethod = req.getPayload().getActions().get(0).getSelectedOption().getText()
            .getText();
    String privatebinUrlEnc = req.getPayload().getActions().get(0).getActionId()
            .substring("render-request-".length());
    String privatebinUrl = new String(Base64.getDecoder().decode(privatebinUrlEnc));

    executor.submit(t(() -> {
      RequestContainer request = loader.loadRequest(new URI(privatebinUrl));
      ctx.client().viewsOpen(r -> r
              .triggerId(ctx.getTriggerId())
              .view(renderer.buildRequestView(renderingMethod, privatebinUrl, request)));
    }));


    return ctx.ack();
  }


  @SneakyThrows
  private Response handleDismissRequestAction(BlockActionRequest req, ActionContext ctx) {
    executor.submit(t(() -> {
      ctx.respond(r -> r.deleteOriginal(true));
    }));

    return ctx.ack();
  }

  @NotNull
  private static List<BlockElement> getRequestActions(String privatebinUrl, RequestContainer request) {
    String encUrl = Base64.getEncoder().encodeToString(privatebinUrl.getBytes());

    var options = Templates.templateFor(request.getClass()).stream()
            .map(t -> option(plainText(t.getName()), t.getName()))
            .collect(Collectors.toList());

    if (options.isEmpty()) {
      return asElements(
              button(b -> b.actionId("dismiss-request")
                      .text(plainText(pt -> pt.text("Dismiss"))).value("dismiss"))
      );
    }

    return asElements(
            staticSelect(s -> s.actionId("render-request-" + encUrl)
                    .placeholder(plainText("Show as ..."))
                    .options(options)),
            button(b -> b.actionId("dismiss-request")
                    .text(plainText(pt -> pt.text("Dismiss"))).value("dismiss"))
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

  private static Callable t(ThrowingRunnable r) {
    return () -> {
      r.run();
      return null;
    };
  }


  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }

}
