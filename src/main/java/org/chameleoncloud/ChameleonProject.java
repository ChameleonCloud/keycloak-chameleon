package org.chameleoncloud;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChameleonProject implements Comparable {
  @JsonProperty("id")
  protected String id;

  @JsonProperty("nickname")
  protected Optional<String> nickname;

  public ChameleonProject(final String id, final Optional<String> nickname) {
    this.id = id;
    this.nickname = nickname;
  }

  @Override
  public int compareTo(Object o) {
    if (!(o instanceof ChameleonProject)) {
      return -1;
    }
    final ChameleonProject other = (ChameleonProject)o;
    return this.id.compareTo(other.id);
  }
}
