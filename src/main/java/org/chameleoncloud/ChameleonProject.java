package org.chameleoncloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public class ChameleonProject implements Comparable<ChameleonProject> {
    @JsonProperty("id")
    protected String id;

    @JsonProperty("nickname")
    protected String nickname;

    public ChameleonProject(final String id, final String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    @Override
    public int compareTo(@NotNull ChameleonProject o) {
        return this.id.compareTo(o.id);
    }
}
