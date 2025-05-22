package org.chameleoncloud;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    public int compareTo(ChameleonProject o) {
        if (o == null) {
            return 1;
        }
        return this.id.compareTo(o.id);
    }
}
