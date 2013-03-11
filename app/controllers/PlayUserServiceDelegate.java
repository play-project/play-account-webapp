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
import java.util.List;

import models.Account;
import models.ApplicationException;
import models.User;
import play.cache.Cache;
import play.mvc.Controller;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserServiceDelegate;
import client.UserClient;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

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
		System.out.println("Looking at a user in the registry..." + user);
		User u = null;
		try {
			u = UserClient.getUserFromProvider(user.id,
					user.provider.toString());
		} catch (ApplicationException e) {
			flash.error("Internal error : %s", e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		if (u != null) {
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

				session.put(Application.PLAYUSER_ID, u.id);

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

	public void save(final SocialUser user) {
		System.out.println("Save social user : " + user);
		// called at each login. Do not save the user if it is already available
		// in the store...

		// TODO
		// if not logged in...

		if (this.find(user.id) != null) {
			System.out.println("Already available...");
			// FIXME : at least we can update the user information if needed...
			return;
		}

		// if we are already logged in and if the current account is not yet
		// linked,
		// let's add it to the current user account.
		if (session.get(Application.PLAYUSER_ID) != null) {

			// get the user
			User playuser = null;
			if (Cache.get(session.get(Application.PLAYUSER_ID)) != null) {
				playuser = (User) Cache.get(session
						.get(Application.PLAYUSER_ID));
			} else {
				// get the user from the store
				try {
					playuser = UserClient.getUserFromID(session
							.get(Application.PLAYUSER_ID));
					Cache.set(session.get(Application.PLAYUSER_ID), playuser);

				} catch (ApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}

			// check if the social account is already registered in the playuser
			List<Account> filtered = new ArrayList<Account>(
					Collections2.filter(playuser.accounts,
							new Predicate<Account>() {
								public boolean apply(Account account) {

									if (account.provider
											.equalsIgnoreCase(user.id.provider
													.toString())) {
										// / check OAUTH version

										if (user.authMethod.toString()
												.equalsIgnoreCase("oauth1")) {
											System.out.println("OAuth1");
											return account.secret != null
													&& account.token != null
													&& account.secret
															.equals(user.secret)
													&& account.token
															.equals(user.token);

										} else if (user.authMethod.toString()
												.equalsIgnoreCase("oauth2")) {
											System.out.println("OAuth2");
											return account.accessToken != null
													&& account.accessToken
															.equals(user.accessToken);
										} else {
											System.out.println("Unknow auth method..." + user.authMethod);
											return false;
										}

									} else {
										System.out
												.println("!provider : Account "
														+ account.provider
														+ " : user "
														+ user.id.provider);
										return false;
									}
								}
							}));

			if (filtered != null && filtered.size() == 1) {
				// already have the account linked
				System.out
						.println("Social Account has been found in the current user");
			} else {
				System.out
						.println("Social Account has NOT been found in the current user, let's add it");
				Account account = new Account();
				account.email = user.email;
				account.accessToken = user.accessToken;
				account.avatarURL = user.avatarUrl;
				account.fullName = user.displayName;
				account.login = user.id.id;
				account.provider = user.id.provider.toString().toLowerCase();
				account.secret = user.secret;
				account.token = user.token;
				try {
					User result = UserClient.addAccount(playuser, account);
				} catch (ApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// fech user from the store...
				try {
					playuser = UserClient.getUserFromID(session
							.get(Application.PLAYUSER_ID));
					Cache.set(session.get(Application.PLAYUSER_ID), playuser);
				} catch (ApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			// not logged in, TODO...
			System.out.println("Not logged in, do nothing...");
			flash.error("Something bad occured...");
		}
	}
}
