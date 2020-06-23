package milkman.slackbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import lombok.SneakyThrows;
import milkman.persistence.UnknownPluginHandler;
import milkman.plugin.privatebin.PrivateBinApi;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class RequestLoader {
  PrivateBinApi api = new PrivateBinApi("");

  @SneakyThrows
  public RestRequestContainer loadRequest(URI privatebinUrl){
    String json = api.readPaste(privatebinUrl.toString());
    ObjectMapper mapper = new ObjectMapper();
    mapper.addHandler(new UnknownPluginHandler());

    return mapper.readValue(json, RestRequestContainer.class);
  }

}
