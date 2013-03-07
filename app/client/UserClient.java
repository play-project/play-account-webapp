/**
 * 
 */
package client;

import java.util.ArrayList;
import java.util.List;

import models.Account;
import models.Group;
import models.User;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import securesocial.provider.SocialUser;

import com.google.common.collect.Collections2;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author chamerling
 * 
 */
public class UserClient {

	private UserClient() {
	}

	public static User getUserFromID(String id) {
		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint + "users/%s", id);

		HttpResponse response = request.get();
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, User.class);
		} else {
			return null;
		}
	}

	public static User userpass(String username, String password) {
		System.out.println("userpass");
		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint
				+ "users/basicauth?login=%s&password=%s", username, password);

		HttpResponse response = request.get();
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, User.class);
		} else {
			System.out.println(response.toString());
			return null;
		}
	}

	public static User getUserFromProvider(String login, String provider) {
		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint
				+ "users/query?login=%s&provider=%s", login, provider);
		HttpResponse response = request.get();
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, User.class);
		}
		return null;
	}

	/**
	 * Save a user
	 * 
	 * @param user
	 */
	public static boolean save(User user) {
		boolean result = true;
		Gson gson = new Gson();

		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint + "users")
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(user));

		HttpResponse response = request.post();
		if (response.getStatus() == 201) {
			System.out.println("Saved!");
		} else {
			System.out.println("Not Saved!");
			result = false;
		}
		return result;
	}

	/**
	 * TODO
	 * 
	 * @param userId
	 * @param account
	 * @return
	 */
	public static User addAccount(User user, Account account) {
		System.out.println("Add acount to user");
		User result = null;
		
		Gson gson = new Gson();
		user.accounts.add(account);

		String endpoint = getEndpoint() + "users";
		WSRequest request = WS.url(endpoint)
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(user));
		
		HttpResponse response = request.put();
		if (response.getStatus() == 200) {
			// updated, got the user as response
			result = gson.fromJson(response.getJson(), User.class);
		} else {
			System.out.println(response.getStatus());
		}

		return result;
	}

	/**
	 * Check if the user already exists
	 * 
	 * @param user
	 * @return
	 */
	public static boolean exists(String username) {
		String endpoint = getEndpoint();
		WSRequest request = WS.url(endpoint + "users/login/" + username);
		HttpResponse response = request.get();
		System.out.println(response.getString());
		System.out.println(response.getStatus());
		return response.getStatus() == 200;
	}

	/**
	 * 
	 * @param user
	 */
	public static void save(SocialUser user) {
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

		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint + "users")
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(bean));

		HttpResponse response = request.post();
		if (response.getStatus() == 201) {
			System.out.println("Saved!");
			// TODO
		} else {
			System.out.println("Not Saved!");
			// TODO
		}
	}

	public static Group getGroup(String id) {
		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint + "groups/%s", id);

		HttpResponse response = request.get();
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, Group.class);
		} else {
			return null;
		}
	}

	public static void createGroup(Group group) {
		String endpoint = Play.configuration
				.getProperty("play.userservice.url");

		Gson gson = new Gson();
		WSRequest request = WS.url(endpoint + "groups")
				.setHeader("ContentType", "application/json")
				.mimeType("application/json").body(gson.toJson(group));

		HttpResponse response = request.post();
		if (response.getStatus() == 201) {
			System.out.println("Saved!");
			// TODO
		} else {
			System.out.println("Not Saved!");
			// TODO
		}
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	public static List<Group> getGroups(User user) {
		List<Group> result = new ArrayList<Group>();
		if (user.groups != null && user.groups.size() > 0) {
			// TODO
		}

		return result;
	}

	private static final String getEndpoint() {
		return Play.configuration.getProperty("play.userservice.url");
	}

}
