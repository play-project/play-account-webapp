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

import models.ApplicationException;
import client.user.v1.User;
import client.user.v1.UserClient;
import play.data.validation.Required;
import play.mvc.Controller;

/**
 * @author chamerling
 *
 */
public class Registration extends Controller {
	
	public static void register(String fullName, String username, String email, String password) {
		render(fullName, username, email, password);
	}

	/**
	 * Register user from the register() form
	 * 
	 * @param fullName
	 * @param username
	 * @param email
	 * @param password
	 */
	public static void doRegister(
			@Required(message = "Full name is required") String fullName,
			@Required(message = "Username is required") String username,
			@Required(message = "Valid email address is required") String email,
			@Required(message = "Password is required") String password) {

		validation.required(fullName);
		validation.required(username);
		validation.email(email);
		validation.required(password);

		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			register(fullName, username, email, password);
		}

		try {
			if (UserClient.exists(username)) {
				flash.error(
						"'%s' is already registered in the platform, please choose another one...",
						username);
				register(fullName, username, email, password);
			}
		} catch (ApplicationException e) {
			flash.error(e.getMessage());
			register(fullName, username, email, password);
		}

		// register the user...
		User user = new User();
		user.email = email;
		user.fullName = fullName;
		user.login = username;
		user.password = password;
		try {
			if (UserClient.save(user)) {
				// saved, let's push user into the session...
				flash.success("You are now registered as '%s',  please login", username);
				Application.signin(username);

			} else {
				// ...
				flash.error("Problem while registering your account, Play has been notified...");
				Application.index();
			}
		} catch (ApplicationException e) {
			flash.error(e.getMessage());
			Application.index();
		}
		
		Application.index();
	}

}
