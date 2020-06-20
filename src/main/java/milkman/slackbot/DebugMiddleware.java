package milkman.slackbot;

import com.slack.api.bolt.middleware.Middleware;
import com.slack.api.bolt.middleware.MiddlewareChain;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.JsonOps;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class DebugMiddleware implements Middleware {

    private static class DebugResponseBody {
        String responseType; // ephemeral, in_channel
        String text;
    }

    boolean isDebugMode;

    public DebugMiddleware() {
        this.isDebugMode = "1".equals(System.getenv("SLACK_APP_DEBUG_MODE"));
    }

    @Override
    public Response apply(Request req, Response _resp, MiddlewareChain chain) throws Exception {
        Response resp = chain.next(req);
        if (isDebugMode && resp.getStatusCode() != 200) {
            resp.getHeaders().put("content-type", Arrays.asList(resp.getContentType()));
            // dump all the headers as a single string
            String headers = resp.getHeaders().entrySet().stream()
                    .map(e -> e.getKey() +  ": " + e.getValue() + "\n").collect(joining());

            // set an ephemeral message with useful information
            DebugResponseBody body = new DebugResponseBody();
            body.responseType = "ephemeral";
            body.text =
                    ":warning: *[DEBUG MODE] Something is technically wrong* :warning:\n" +
                            "Below is a response the Slack app was going to send...\n" +
                            "*Status Code*: " + resp.getStatusCode() + "\n" +
                            "*Headers*: ```" + headers + "```" + "\n" +
                            "*Body*: ```" + resp.getBody() + "```";

            resp.setBody(JsonOps.toJsonString(body));

            resp.setStatusCode(200);
        }
        return resp;
    }
}
