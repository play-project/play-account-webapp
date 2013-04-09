/**
 *
 * Copyright (c) 2013, Linagora
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 *
 */
package controllers;

import java.util.List;

import models.ApplicationException;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.URL;
import play.mvc.Before;
import play.mvc.Controller;
import securesocial.provider.ProviderType;
import utils.Gravatar;
import client.platform.v1.Pattern;
import client.platform.v1.PlatformClient;
import client.platform.v1.Stream;
import client.platform.v1.Subscription;
import client.user.v1.Account;
import client.user.v1.Group;
import client.user.v1.User;
import client.user.v1.UserClient;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import controllers.securesocial.SecureSocial;

/**
 * @author chamerling
 */
// @With(SecureSocial.class)
public class Application extends Controller {

	public static final String PLAYUSER = "playuser";

	public static final String PLAYUSER_ID = "playuserid";

	/**
	 * Check authentification on all request TODO : Cache user so we do not call
	 * the backend each time
	 */
	@Before(unless = { "signin", "doLogin" })
	private static void checkAuthentification() {
		// get ID from secure social plugin
		String uid = session.get(PLAYUSER_ID);
		if (uid == null) {
			signin(null);
		}

		// get the user from the store. TODO Can also get it from the cache...
		User user = null;
		if (Cache.get(uid) == null) {
			try {
				user = UserClient.getUserFromID(uid);
				Cache.set(uid, user);
			} catch (ApplicationException e) {
				e.printStackTrace();
				flash.error("Problem while getting user from store...");
			}
		} else {
			user = (User) Cache.get(uid);
		}

		if (user == null) {
			flash.error("Problem while getting user from store...");
			Authentifier.logout();
		}

		if (user.avatarURL == null) {
			user.avatarURL = Gravatar.get(user.email, 100);
		}

		// push the user object in the request arguments for later display...
		renderArgs.put(PLAYUSER, user);
	}

	public static void index() {
		render();
	}

	public static void signin(String username) {
		render(username);
	}

	public static void doLogin(
			@Required(message = "Username is required") String username,
			@Required(message = "Password is required") String password) {

		validation.required(username);
		validation.required(password);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			signin(username);
		}

		User user = null;
		try {
			user = UserClient.userpass(username, password);
		} catch (ApplicationException e) {
			flash.error("Internal error : " + e.getMessage());
			signin(username);
		}
		if (user == null) {
			flash.error("Bad username/password");
			signin(username);
		}

		// user has been found, let's push some stuff in the session and in the
		// cache...
		session.put(PLAYUSER_ID, user.id);
		Cache.add(user.id, user);

		flash.success("Successfully connected");
		index();
	}

	/**
	 * Add social account to the current account
	 * 
	 * @param provider
	 */
	public static final void linkAccount(final String provider) {
		// TODO : Check that the provider is supported...

		User current = getUser();
		if (Collections2.filter(current.accounts, new Predicate<Account>() {
			public boolean apply(Account account) {
				return account.provider != null
						&& account.provider.equalsIgnoreCase(provider);
			};
		}).size() > 0) {
			flash.error("'%s' is already linked with your account", provider);
			user();
		}
		SecureSocial.authenticate(ProviderType.valueOf(provider));
	}

	/**
	 * Remove account from the current account
	 * 
	 * @param provider
	 */
	public static final void unlinkAccount(String provider) {
		if (provider == null || provider.trim().length() == 0) {
			flash.error("Wrong account provider");
			user();
		}

		User user = getUser();
		if (user == null) {
			flash.error("Can not retrieve user");
			index();
		}
		try {
			UserClient.removeAccount(getUser(), provider);
			flash.success("%s account unlinked", provider);
			reloadUser();
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while removing account : %s", e.getMessage());
		}
		user();
	}

	public static void user() {
		render();
	}

	public static void group(String name) {
		try {
			Group group = UserClient.getGroup(name);
			render(group);
		} catch (ApplicationException e) {
			flash.error(e.getMessage());
			groups();
		}
	}

	/**
	 * Get the current user groups
	 * 
	 * @param name
	 */
	public static void groups() {
		try {
			User user = getUser();
			List<Group> groups = UserClient.getGroups(user);
			render(groups);
		} catch (ApplicationException e) {
			flash.error(e.getMessage());
			index();
		}
	}

	public static void joinGroup(String name) {
		User user = getUser();
		if (user == null) {
			flash.error("Can not retrieve user");
			index();
		}
		try {
			UserClient.addGroup(user, name);
			flash.success("You joined group %s", name);
			reloadUser();

		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while joining group : %s", e.getMessage());
		}
		groups();
	}

	public static void leaveGroup(String name) {
		User user = getUser();
		if (user == null) {
			flash.error("Can not retrieve user");
			index();
		}
		try {
			UserClient.removeGroup(user, name);
			flash.success("You left group %s", name);
			reloadUser();

		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while leaving group : %s", e.getMessage());
		}
		groups();
	}

	/**
	 * Get all the stuff the user created
	 */
	public static void created() {
		// TODO
	}

	/**
	 * Get the streams the user can access
	 */
	public static void streams() {
		try {
			List<Stream> streams = PlatformClient.streams(getUser());
			render(streams);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Can not get streams : %s", e.getMessage());
			render();
		}
	}

	/**
	 * Get the patterns the user created. Also provides way to publish more
	 */
	public static void patterns() {
		try {
			List<Pattern> patterns = PlatformClient.patterns(getUser());
			render(patterns);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Can not get patterns : %s", e.getMessage());
			render();
		}
	}

	public static void pattern(String id) {
		try {
			Pattern pattern = PlatformClient.pattern(getUser(), id);
			render(pattern);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Can not get pattern %s : %s", id, e.getMessage());
			render();
		}
	}

	public static void deployPattern() {
		render();
	}

	public static void doDeployPattern(
			@Required(message = "Pattern is required") String pattern) {
		validation.required(pattern);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			deployPattern();
		}

		try {
			Pattern p = PlatformClient.deploy(getUser(), pattern);
			flash.success("Pattern deployed into the platform with id %s",
					p.pattern_id);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.success("Error while deploying pattern to the platform");
		}
		patterns();
	}

	public static void doUndeployPattern(
			@Required(message = "Pattern ID required") String id) {
		validation.required(id);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			patterns();
		}

		try {
			PlatformClient.undeploy(getUser(), id);
			flash.success("Pattern %s undeployed from the platform", id);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.success("Error while deploying pattern to the platform");
		}
		patterns();
	}

	/**
	 * Get all the user subscriptions
	 */
	public static void subscriptions() {
		try {
			List<Subscription> subscriptions = PlatformClient
					.subscriptions(getUser());
			render(subscriptions);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Can not get subscriptions : %s", e.getMessage());
			render();
		}
	}

	public static void createSubscription() {
		List<Stream> streams = null;
		try {
			streams = PlatformClient.streams(getUser());
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while getting available streams %s",
					e.getMessage());
			index();
		}
		render(streams);
	}

	public static void doCreateSubscription(
			@Required(message = "Resource URL is required") @URL(message = "Valid resource URL is required") String resource,
			@Required(message = "Subscriber URL is required") String subscriber) {

		// resource and subscription must be non null and URLs
		validation.required(resource);
		validation.required(subscriber);
		validation.url(resource);

		// play validation does not like localhost...
		validation.isTrue(subscriber != null
				&& (subscriber.startsWith("http://") || subscriber
						.startsWith("https://")));
		// validation.url(subscriber);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			createSubscription();
		}

		try {
			Subscription subscription = PlatformClient.subscribe(getUser(),
					resource, subscriber);

			flash.success("Subscription created %s",
					subscription.subscription_id);
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while creating subscription %s", e.getMessage());
		}
		subscriptions();
	}

	public static void doDeleteSubscription(
			@Required(message = "Subscritpion ID is required") String id) {
		validation.required(id);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			subscriptions();
		}

		try {
			boolean result = PlatformClient.unsubscribe(getUser(), id);

			if (result) {
				flash.success("Subscription %s removed", id);
			} else {
				flash.error("Problem while removing subscription");
			}
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while removing subscription %s", e.getMessage());
		}
		subscriptions();
	}

	/**
	 * Publish message from a web page...
	 */
	public static void publish() {
		List<Stream> streams = null;
		try {
			streams = PlatformClient.streams(getUser());
		} catch (ApplicationException e) {
			e.printStackTrace();
			flash.error("Error while getting available streams %s",
					e.getMessage());
			index();
		}
		render(streams);
	}

	public static void doPublish(
			@Required(message = "Stream is required") String stream,
			@Required(message = "Message content is required") String message) {

		validation.required(stream);
		validation.required(message);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			publish();
		}

		try {
			PlatformClient.publish(getUser(), stream, message);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		flash.success("Message published into the platform");
		publish();
	}

	// Private stuff

	/**
	 * 
	 * @return
	 */
	private static User getUser() {
		String uid = session.get(PLAYUSER_ID);
		if (uid == null) {
			Logger.debug("No play user found...");
			signin(null);
		}

		User user = null;
		if (Cache.get(uid) == null) {
			try {
				user = UserClient.getUserFromID(uid);
				Cache.set(uid, user);
			} catch (ApplicationException e) {
				e.printStackTrace();
				flash.error("Something wrong occured");
			}
		} else {
			user = (User) Cache.get(uid);
		}
		return user;
	}

	private static void reloadUser() {
		String uid = session.get(PLAYUSER_ID);
		if (uid == null) {
			signin(null);
		}

		try {
			User user = UserClient.getUserFromID(uid);
			Cache.set(uid, user);
		} catch (ApplicationException e) {
			flash.error("Something wrong occured");
			signin(null);
		}
	}
}