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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.User;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserServiceDelegate;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * 
 * @author chamerling
 * 
 */
public class PlayUserServiceDelegate extends Controller implements
		UserServiceDelegate {

	public SocialUser find(final UserId user) {
		// search in the user service if a user with login and provider is
		// already registered
		String endpoint = Play.configuration
				.getProperty("play.userservice.url");
		WSRequest request = WS.url(endpoint
				+ "users/query?login=%s&provider=%s", user.id,
				user.provider.toString());
		HttpResponse response = request.get();
		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			User u = gson.fromJson(json, User.class);
			List<Account> filtered = new ArrayList<Account>(
					Collections2.filter(u.accounts, new Predicate<Account>() {
						public boolean apply(Account input) {
							return input.provider.equals(user.provider
									.toString().toLowerCase());
						}
					}));
			
			if (filtered != null && filtered.size() == 1) {
				Account account = filtered.get(0);
				SocialUser result = new SocialUser();
				result.accessToken = account.accessToken;
				result.avatarUrl = account.avatarURL;
				result.displayName = account.fullName;
				result.email = account.email;
				
				UserId id = new UserId();
				id.id = account.login;
				id.provider = ProviderType.valueOf(account.provider);
				result.id = id;
				result.secret = account.secret;
				result.token = account.token;
				return result;
			}

			return null;
		}
		return null;
	}

	public String createActivation(SocialUser user) {
		return null;
	}

	public boolean activate(String uuid) {
		return false;
	}

	public void deletePendingActivations() {

	}

	public SocialUser find(String email) {
		return null;
	}

	public String createPasswordReset(SocialUser user) {
		return null;
	}

	public SocialUser fetchForPasswordReset(String username, String uuid) {
		return null;
	}

	public void disableResetCode(String username, String uuid) {
	}

	public void save(SocialUser user) {
		// called at each login. Do not save the user if it is already available in the store...
		if (this.find(user.id) != null) {
			System.out.println("Already available...");
			// FIXME : at least we can update the user information if needed...
			return;
		}

		Gson gson = new Gson();
		User bean = new User();
		bean.login = user.id.id;
		bean.password = "noop";
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
				.setHeader("ContentType", "application/json").mimeType("application/json")
				.body(gson.toJson(bean));
		HttpResponse response = request.post();
		if (response.getStatus() == 201) {
			System.out.println("Saved!");
			// TODO
		} else {
			System.out.println("Not Saved!");
			// TODO
		}

	}
}
