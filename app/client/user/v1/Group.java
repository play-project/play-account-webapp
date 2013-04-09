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
package client.user.v1;

import java.util.List;


/**
 * The group bean
 * 
 * @author chamerling
 *
 */
public class Group {

	/**
	 * ID in the backend, autogenerated at creation...
	 */
	public String id;
	
	public String title;

	/**
	 * Group name
	 */
	public String name;
	
	/**
	 * Group description
	 */
	public String description;
	
	/**
	 * 
	 */
	public String resourceURI;
	
	/**
	 * Additional information as key/value...
	 */
	public List<Meta> metadata;
	
	/**
	 * 
	 */
	public Group() {
	}

	public String getGroupURI() {
		if (resourceURI != null && resourceURI.endsWith("#group")) {
			return resourceURI.substring(0, resourceURI.indexOf("#group"));
		}
		return null;
	}

}