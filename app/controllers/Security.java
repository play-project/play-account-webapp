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

import play.Play;
import models.*;

/**
 * 
 * @author chamerling
 *
 */
public class Security extends Secure.Security {

	/**
	 * TODO : USe Secure Social...
	 * @param username
	 * @param password
	 * @return
	 */
	static boolean authenticate(String username, String password) {
		return Play.configuration.getProperty("secure.admin.username").equals(username)
				&& Play.configuration.getProperty("secure.admin.password").equals(password);
	}

}
