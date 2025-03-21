package com.weare5stones.keycloak.tokenmapper.group;

/** Duplicate mapper using the old id to prevent losing configuration on update. */
public class LegacyGroupMapper extends GroupMapper {
	public static final String PROVIDER_ID = "oidc-lucky-number-mapper";

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Group Property Mapper (legacy)";
	}
}
