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

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Controller;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

import com.google.gson.JsonObject;

import controllers.securesocial.SecureSocial;

/**
 * 
 * @author chamerling
 *
 */
public class Authentifier extends Controller {
	
	public static void githubAuth() {
		SecureSocial.authenticate(ProviderType.github);
	}

	public static void twitterAuth() {
		SecureSocial.authenticate(ProviderType.twitter);
	}
	
	public static void logout() {
		SecureSocial.logout();
	}
	
	public static void login() {
		SecureSocial.login();
	}
}
