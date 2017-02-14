package io.ibole.microservice.discovery;

import io.ibole.microservice.common.ServerIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServiceDiscovery implements ServiceDiscovery<InstanceMetadata> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private ServerIdentifier identifier;

  protected AbstractServiceDiscovery(ServerIdentifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public ServerIdentifier getIdentifier() {
    return this.identifier;
  }

  protected String buildBasePath() {
    return this.getIdentifier().getRootPath().getPath();
  }
}
