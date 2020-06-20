package milkman.slackbot;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.view.View;
import com.slack.api.webhook.WebhookResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;


public class Application {

    private final static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        // App expects env variables (SLACK_BOT_TOKEN, SLACK_SIGNING_SECRET)
        App app = new App();
        app.use(new DebugMiddleware());

        app.command("/milkman", (req, ctx) -> {


            URI privateBinUrl;
            var text = req.getPayload().getText();
            try {
                privateBinUrl = new URI(text);
            } catch (URISyntaxException e) {
                log.error("Failed to read url from command: " + e);
                return ctx.ack("'" + text + "' not a valid privatebin url");
            }

            WebhookResponse result = ctx.respond(res -> res
                    .responseType("ephemeral") // or "in_channnel"
                    .blocks(asBlocks(
                            context(asContextElements(markdownText("Request stored at <" + privateBinUrl + "|" + privateBinUrl.getHost() + ">"))),
                            section(s -> s.text(plainText("Request Preview"))),
                            section(Application::buildRequestHeader),
                            divider(),
                            actions(actions -> actions
                                    .elements(asElements(
                                            button(b -> b.actionId("share-request-share").text(plainText(pt -> pt.text("Share"))).value(text)),
                                            button(b -> b.actionId("share-request-dismiss").text(plainText(pt -> pt.text("Dismiss"))).value("dismiss"))
                                    ))
                            )
                    ))
            );

            return ctx.ack();
        });

        app.blockAction(Pattern.compile("share-request-.*"), (req, ctx) -> {
            boolean isShare = "share-request-share".equals(req.getPayload().getActions().get(0).getActionId());
            String privatebinUrl = req.getPayload().getActions().get(0).getValue();
            if (isShare) {
                ctx.respond(res -> res
                        .deleteOriginal(true)
                        .responseType("in_channel")
                        .blocks(asBlocks(
                                section(Application::buildRequestHeader),
                                actions(actions -> actions.elements(getRequestActions(privatebinUrl)))
                        ))
                );
            } else {
                ctx.respond(res -> res.deleteOriginal(true));
            }

            return ctx.ack();
        });

        app.blockAction(Pattern.compile("render-request-.*"), (req, ctx) -> {
            String renderingMethod = req.getPayload().getActions().get(0).getSelectedOption().getText().getText();
            String privatebinUrlEnc = req.getPayload().getActions().get(0).getActionId().substring("render-request-".length());
            String privatebinUrl = new String(Base64.getDecoder().decode(privatebinUrlEnc));

            ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    .view(buildView(renderingMethod, privatebinUrl)));

            return ctx.ack();
        });

        SlackAppServer server = new SlackAppServer(app, getPort());
        server.start();
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


    private static int getPort() {
        var port = System.getenv("PORT");
        if (port == null)
            return 3000;

        return Integer.parseInt(port);
    }

    private static SectionBlock.SectionBlockBuilder buildRequestHeader(SectionBlock.SectionBlockBuilder section) {
        return section
                .text(markdownText("Name: `new user`\nRequest: `POST /user`\nHost: `www.myservice.de`"));
    }

    private static SectionBlock.SectionBlockBuilder buildRequestBody(SectionBlock.SectionBlockBuilder section, String renderingMethod) {
        return section
                .text(markdownText("```\n" + renderingMethod + " /asdasd asd \n asdasd \n asdasd```"));
    }


    private static View buildView(String renderingMethod, String privateBinUrl) {
        return view(view -> view
                .callbackId("meeting-arrangement")
                .type("modal")
                .notifyOnClose(false)
                .title(viewTitle(title -> title.type("plain_text").text("Request: " + renderingMethod).emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Close").emoji(true)))
                .blocks(asBlocks(
                        section(Application::buildRequestHeader),
                        section(s -> s.text(plainText(privateBinUrl))),
                        divider(),
                        section(s -> buildRequestBody(s, renderingMethod))
                ))
        );
    }

}
