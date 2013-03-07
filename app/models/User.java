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
package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chamerling
 * 
 */
public class User implements Serializable {

	/**
	 * The user id
	 */
	public String id;

	/**
	 * The user id in the Play platform
	 */
	public String login;

	public String fullName;

	/**
	 * If any...
	 */
	public String password;

	public String email;

	public String avatarURL;
	
	public String apiToken;

	/**
	 * Providers accounts
	 */
	public List<Account> accounts = new ArrayList<Account>();

	public List<String> groups = new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [id=" + id + ", login=" + login + ", fullName=" + fullName
				+ ", password=" + password + ", email=" + email
				+ ", avatarURL=" + avatarURL + ", accounts=" + accounts
				+ ", groups=" + groups + "]";
	}
}
