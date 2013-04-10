/**
 * 
 */
package client.user.v1;

import java.util.ArrayList;
import java.util.List;

import models.ApplicationException;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import securesocial.provider.SocialUser;
import client.Constants;
import client.platform.v1.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author chamerling
 * 
 */
public class UserClient {

	private UserClient() {
	}

	public static User getUserFromID(String id) throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "users/%s", id).setHeader(
				"Authorization", "Bearer " + getToken());

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			System.out.println(response.toString());
			Gson gson = new Gson();
			return gson.fromJson(json, User.class);
		} else {
			Logger.debug("Output status %s", response.getStatusText());
			return null;
		}
	}

	public static User userpass(String username, String password) throws ApplicationException {
		WSRequest request = WS.url(
				getEndpoint() + "users/basicauth?login=%s&password=%s",
				username, password).setHeader("Authorization",
				"Bearer " + getToken());

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, User.class);
		} else {
			Logger.debug("Output status %s", response.getStatusText());
			return null;
		}
	}

	public static User getUserFromProvider(String login, String provider) throws ApplicationException {
		WSRequest request = WS.url(
				getEndpoint() + "users/query?login=%s&provider=%s", login,
				provider).setHeader("Authorization", "Bearer " + getToken());
		
		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, User.class);
		} else {
			Logger.debug("Output status %s", response.getStatusText());
		}
		return null;
	}

	/**
	 * Save a user
	 * 
	 * @param user
	 */
	public static boolean save(User user) throws ApplicationException {
		boolean result = true;
		Gson gson = new Gson();

		WSRequest request = WS.url(getEndpoint() + "users")
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(user))
				.setHeader("Authorization", "Bearer " + getToken());

		HttpResponse response = null;
		try {
			response = request.post();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}		
		if (response.getStatus() == 201) {
			Logger.debug("Saved!");
		} else {
			Logger.debug("Not saved");
			result = false;
		}
		return result;
	}

	/**
	 * 
	 * @param userId
	 * @param account
	 * @return
	 */
	public static User addAccount(User user, Account account) throws ApplicationException {
		User result = null;
		
		Gson gson = new Gson();
		user.accounts.add(account);

		String endpoint = getEndpoint() + "users";
		WSRequest request = WS.url(endpoint)
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(user))
				.setHeader("Authorization", "Bearer " + getToken());
		
		HttpResponse response = null;
		try {
			response = request.put();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			// updated, got the user as response
			result = gson.fromJson(response.getJson(), User.class);
		} else {
			Logger.debug("Status %s", response.getStatusText());
		}

		return result;
	}

	/**
	 * Delete an account from the user.
	 * 
	 * @param user
	 *            the user to remove account from
	 * @param provider
	 *            the account provider
	 * @param username
	 *            the account username
	 * @throws ApplicationException
	 */
	public static void removeAccount(User user, String provider)
			throws ApplicationException {
		String endpoint = getEndpoint() + "users/%s/accounts";
		WSRequest request = WS.url(endpoint, user.id)
				.setParameter("provider", provider)
				.setHeader("Authorization", "Bearer " + getToken());

		HttpResponse response = null;

		try {
			response = request.delete();
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() != 204) {
			throw new ApplicationException("Can not remove account");
		}
	}

	/**
	 * Check if the user already exists
	 * 
	 * @param user
	 * @return
	 */
	public static boolean exists(String username) throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "users/login/" + username)
				.setHeader("Authorization", "Bearer " + getToken());

		HttpResponse response = null;

		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		return response.getStatus() == 200;
	}

	/**
	 * 
	 * @param user
	 */
	public static void save(SocialUser user) throws ApplicationException {
		Gson gson = new Gson();
		User bean = new User();
		bean.login = user.id.id;

		// don't care if we already have a social account support...
		// TODO : Will be nice to have it...
		bean.password = bean.login;

		Account account = new Account();
		account.email = user.email;
		account.accessToken = user.accessToken;
		account.avatarURL = user.avatarUrl;
		account.fullName = user.displayName;
		account.login = user.id.id;
		account.provider = user.id.provider.toString();
		account.secret = user.secret;
		account.token = user.token;
		bean.accounts.add(account);

		WSRequest request = WS.url(getEndpoint() + "users")
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(bean))
				.setHeader("Authorization", "Bearer " + getToken());

		HttpResponse response = null;
		try {	
			response = request.post();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 201) {
			Logger.debug("Saved!");
			// TODO
		} else {
			Logger.debug("Not Saved!");
			// TODO
		}
	}

	public static Group getGroup(String id) throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "groups/%s", id).setHeader(
				"Authorization", "Bearer " + getToken());

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, Group.class);
		} else {
			Logger.debug("Status %s", response.getStatusText());
			return null;
		}
	}

	public static void createGroup(Group group) throws ApplicationException {

		Gson gson = new Gson();
		WSRequest request = WS.url(getEndpoint() + "groups")
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(group))
				.setHeader("Authorization", "Bearer " + getToken());

		HttpResponse response = null;
		try {
			response = request.post();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		if (response.getStatus() == 201) {
			Logger.debug("Saved!");
			// TODO
		} else {
			Logger.debug("Not Saved!");
			// TODO
		}
	}

	/**
	 * TODO : Get the groups object for the user
	 * 
	 * @param user
	 * @return
	 */
	public static List<Group> getGroups(User user) throws ApplicationException {
		List<Group> result = new ArrayList<Group>();
		
		WSRequest request = WS.url(getEndpoint() + "groups").setHeader(
				"Authorization", "Bearer " + getToken());

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			Groups groups = gson.fromJson(json, Groups.class);
			return groups.groups;
		} else {
			Logger.debug("Status %s", response.getStatusText());
			return result;
		}
	}

	/**
	 * @param user
	 * @param name
	 * @return
	 * @throws ApplicationException 
	 */
	public static User addGroup(User user, String group) throws ApplicationException {
		
		User result = null;
		
		Gson gson = new Gson();
		Resource resource = new Resource();
		resource.uri = group;

		String endpoint = getEndpoint() + "users/%s/groups";
		WSRequest request = WS.url(endpoint, user.id)
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(resource))
				.setHeader("Authorization", "Bearer " + getToken());
		
		HttpResponse response = null;
		try {
			response = request.post();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
		
		if (response.getStatus() == 200) {
			// updated, got the user as response
			result = gson.fromJson(response.getJson(), User.class);
		} else {
			Logger.debug("Status %s", response.getStatusText());
		}

		return result;
	}
	
	/**
	 * Remove group from the user
	 * 
	 * @param user
	 * @param group
	 * @return
	 * @throws ApplicationException
	 */
	public static void removeGroup(User user, String group)
			throws ApplicationException {
		
		String endpoint = getEndpoint() + "users/%s/groups";
		WSRequest request = WS.url(endpoint, user.id)
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").setParameter("uri", group)
				.setHeader("Authorization", "Bearer " + getToken());
		
		try {
			request.delete();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}
	}

	private static final String getEndpoint() {
		return Play.configuration
				.getProperty(Constants.PLAY_GOVERNANCE_ENDPOINT);
	}

	private static final String getToken() {
		return Play.configuration.getProperty(Constants.PLAY_GOVERNANCE_TOKEN);
	}

}
