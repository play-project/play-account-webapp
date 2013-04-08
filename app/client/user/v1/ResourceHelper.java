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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author chamerling
 * 
 */
public class ResourceHelper {

	public static boolean in(final String resource,
			final List<Resource> resources) {
		return Iterables.tryFind(resources, new Predicate<Resource>() {
			public boolean apply(Resource r) {
				return r.uri.equals(resource);
			};
		}).isPresent();
	}

}
