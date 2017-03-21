package com.github.ibole.microservice.discovery;

import com.github.ibole.microservice.common.ServerIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServiceDiscovery implements ServiceDiscovery<HostMetadata> {

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
