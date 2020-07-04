package milkman.slackbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.persistence.UnknownPluginHandler;
import milkman.plugin.privatebin.PrivateBinApi;
import milkman.plugin.privatebin.RequestDataContainer;

import java.net.URI;

public class RequestLoader {
  PrivateBinApi api = new PrivateBinApi("");

  @SneakyThrows
  public RequestContainer loadRequest(URI privatebinUrl) {
    String json = api.readPaste(privatebinUrl.toString());
    ObjectMapper mapper = new ObjectMapper();
    mapper.addHandler(new UnknownPluginHandler());

    RequestDataContainer container = mapper.readValue(json, RequestDataContainer.class);

    return container.getRequest();
  }

}
