package com.weare5stones.keycloak.tokenmapper.group;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupMapper extends AbstractOIDCProtocolMapper
	implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

	public static final String PROVIDER_ID = "oidc-group-mapper";

	protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	static final String GROUP_ID = "idEnabled";
	static final String GROUP_NAME = "nameEnabled";
	static final String GROUP_PATH = "pathEnabled";
	static final String GROUP_REALM_ROLES = "realmRolesEnabled";
	static final String GROUP_CLIENT_ROLES = "clientRolesEnabled";
	static final String GROUP_ATTRIBUTES = "attributesEnabled";

	static {
		// group id
		ProviderConfigProperty groupIdProp = new ProviderConfigProperty();
		groupIdProp.setName(GROUP_ID);
		groupIdProp.setLabel("Group ID");
		groupIdProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		groupIdProp.setDefaultValue("true");
		groupIdProp.setHelpText("Include group ID, false will exclude the group ID");
		configProperties.add(groupIdProp);

		// group name
		ProviderConfigProperty groupNameProp = new ProviderConfigProperty();
		groupNameProp.setName(GROUP_NAME);
		groupNameProp.setLabel("Group name");
		groupNameProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		groupNameProp.setDefaultValue("true");
		groupNameProp.setHelpText("Include group name i.e. /group_name, false will exclude the group name");
		configProperties.add(groupNameProp);

		// group path
		ProviderConfigProperty groupPathProp = new ProviderConfigProperty();
		groupPathProp.setName(GROUP_PATH);
		groupPathProp.setLabel("Group path");
		groupPathProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		groupPathProp.setDefaultValue("true");
		groupPathProp.setHelpText("Include group path");
		configProperties.add(groupPathProp);

		// group realm roles
		ProviderConfigProperty groupRealmRolesProp = new ProviderConfigProperty();
		groupRealmRolesProp.setName(GROUP_REALM_ROLES);
		groupRealmRolesProp.setLabel("Group realm roles");
		groupRealmRolesProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		groupRealmRolesProp.setDefaultValue("true");
		groupRealmRolesProp.setHelpText("Include group realm roles");
		configProperties.add(groupRealmRolesProp);

		// group client roles
		ProviderConfigProperty groupClientRolesProp = new ProviderConfigProperty();
		groupClientRolesProp.setName(GROUP_CLIENT_ROLES);
		groupClientRolesProp.setLabel("Group client roles");
		groupClientRolesProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		groupClientRolesProp.setDefaultValue("true");
		groupClientRolesProp.setHelpText("Include group client roles");
		configProperties.add(groupClientRolesProp);

		// group attributes
		ProviderConfigProperty groupAttributesProp = new ProviderConfigProperty();
		groupAttributesProp.setName(GROUP_ATTRIBUTES);
		groupAttributesProp.setLabel("Group attributes");
		groupAttributesProp.setType(ProviderConfigProperty.BOOLEAN_TYPE);
		groupAttributesProp.setDefaultValue("true");
		groupAttributesProp.setHelpText("Include group attributes");
		configProperties.add(groupAttributesProp);

		OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
		OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, GroupMapper.class);
	}

	public static boolean isTrue(String value) {
		return "true".equals(value);
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayCategory() {
		return TOKEN_MAPPER_CATEGORY;
	}

	@Override
	public String getDisplayType() {
		return "Group Property Mapper";
	}

	@Override
	public String getHelpText() {
		return "Map Group properties to the JWT";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	@Override
	protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
		Map<String, String> config = mappingModel.getConfig();
		boolean addGroupId = isTrue(config.get(GROUP_ID));
		boolean addGroupName = isTrue(config.get(GROUP_NAME));
		boolean addGroupPath = isTrue(config.get(GROUP_PATH));
		boolean addGroupRealmRoles = isTrue(config.get(GROUP_REALM_ROLES));
		boolean addGroupClientRoles = isTrue(config.get(GROUP_CLIENT_ROLES));
		boolean addGroupAttributes = isTrue(config.get(GROUP_ATTRIBUTES));

		List<GroupRepresentation> membership = userSession
			.getUser()
			.getGroupsStream()
			.map(group -> toRepresentation(
				group,
				addGroupId,
				addGroupName,
				addGroupPath,
				addGroupRealmRoles,
				addGroupClientRoles,
				addGroupAttributes
			))
			.collect(Collectors.toList())
		;

		String protocolClaim = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
		token.getOtherClaims().put(protocolClaim, membership);
	}

	public static GroupRepresentation toRepresentation(
		GroupModel group,
		boolean addId,
		boolean addName,
		boolean addPath,
		boolean addRealmRoles,
		boolean addClientRoles,
		boolean addAttributes
	) {
		GroupRepresentation rep = new GroupRepresentation();

		if (addId) {
			rep.setId(group.getId());
		}

		if (addName) {
			rep.setName(group.getName());
		}

		if (addPath) {
			rep.setPath(ModelToRepresentation.buildGroupPath(group));
		}

		if (addRealmRoles || addClientRoles) {
			// Role mappings
			Set<RoleModel> roles = group.getRoleMappingsStream().collect(Collectors.toSet());
			List<String> realmRoleNames = new ArrayList<>();
			Map<String, List<String>> clientRoleNames = new HashMap<>();
			for (RoleModel role : roles) {
				if (role.getContainer() instanceof RealmModel) {
					realmRoleNames.add(role.getName());
				} else {
					ClientModel client = (ClientModel)role.getContainer();
					String clientId = client.getClientId();
					List<String> currentClientRoles = clientRoleNames.computeIfAbsent(clientId, k -> new ArrayList<>());
					currentClientRoles.add(role.getName());
				}
			}

			if (addRealmRoles) {
				rep.setRealmRoles(realmRoleNames);
			}

			if (addClientRoles) {
				rep.setClientRoles(clientRoleNames);
			}
		}

		if (addAttributes) {
			Map<String, List<String>> attributes = group.getAttributes();
			rep.setAttributes(attributes);
		}

		return rep;
	}
}
