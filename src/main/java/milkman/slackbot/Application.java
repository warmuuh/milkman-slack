package milkman.slackbot;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.webhook.WebhookResponse;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;


public class Application {
    public static void main(String[] args) throws Exception {
        // App expects env variables (SLACK_BOT_TOKEN, SLACK_SIGNING_SECRET)
        App app = new App();

        app.command("/milkman", (req, ctx) -> {
            return ctx.ack(asBlocks(
                    section(section -> section.text(markdownText(":wave: pong"))),
                    actions(actions -> actions
                            .elements(asElements(
                                    button(b -> b.actionId("ping-again").text(plainText(pt -> pt.text("Ping"))).value("ping"))
                            ))
                    )
            ));
        });

        app.blockAction("ping-again", (req, ctx) -> {
            WebhookResponse result = ctx.respond(res -> res
                    .responseType("in_channel") // or "in_channnel"
                    .text("Hi there! you said: " + req.getPayload().getActions().get(0).getValue())
            );

            return ctx.ack();
        });

        SlackAppServer server = new SlackAppServer(app, getPort());
        server.start();
    }


    private static int getPort(){
        var port = System.getenv("PORT");
        if (port == null)
            return 3000;

        return Integer.parseInt(port);
    }
}
