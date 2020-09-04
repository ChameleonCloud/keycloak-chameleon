package org.chameleoncloud;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChameleonProject {
  @JsonProperty("id")
  protected String id;

  @JsonProperty("nickname")
  protected Optional<String> nickname;

  public ChameleonProject(final String id, final Optional<String> nickname) {
    this.id = id;
    this.nickname = nickname;
  }
}
