package org.chameleoncloud.representations;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserGroupRoles {

    private static final String POLICY_SEPARATOR = ".";

    public static final String ADMIN = "admin";
    public static final String MANAGER = "manager";
    public static final String MEMBER = "member";

    public static final String ADMIN_GROUP_SUFFIX = "-admins";
    public static final String MANAGER_GROUP_SUFFIX = "-managers";

    public static final Map<String, Integer> POLICY_HIERARCHY = Stream
	    .of(new Object[][] { { ADMIN, 0 }, { MANAGER, 1 }, { MEMBER, 2 } })
	    .collect(Collectors.toMap(policy -> (String) policy[0], rank -> (Integer) rank[1]));

    protected String groupId;
    protected String groupName;
    protected String policy;
    protected Set<String> scopes;

    public void setGroupId(String groupId) {
	this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
	this.groupName = groupName;
    }

    public void setPolicy(String policy) {
	if (!POLICY_HIERARCHY.keySet().contains(policy)) {
	    this.policy = null;
	} else {
	    this.policy = policy;
	}
    }

    public void setScopes(Set<String> scopes) {
	this.scopes = scopes;
    }

    public String getGroupId() {
	return this.groupId;
    }

    public String getGroupName() {
	return this.groupName;
    }

    public String getPolicy() {
	return this.policy;
    }

    public Set<String> getScopes() {
	return this.scopes;
    }

    public static String[] parsePolicyName(String policyName) {
	return policyName.split(Pattern.quote(POLICY_SEPARATOR), 2);
    }

    public static String formatPolicyName(String policy, String groupId) {
	return String.join(POLICY_SEPARATOR, policy, groupId);
    }

}
