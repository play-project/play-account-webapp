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

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

	public static long groups(User user) {
		if (user == null || user.groups == null) {
			return 0;
		}
		return user.groups.size();
	}

	public static long subscriptions(User user) {
		if (user == null || user.resources == null) {
			return 0;
		}
		return Collections2.filter(user.resources, new Predicate<Resource>() {
			public boolean apply(Resource resource) {
				return resource != null && resource.name != null
						&& resource.name.equals("subscription");
			}
		}).size();
	}

	public static long patterns(User user) {
		if (user == null || user.resources == null) {
			return 0;
		}
		return Collections2.filter(user.resources, new Predicate<Resource>() {
			public boolean apply(Resource resource) {
				return resource != null && resource.name != null
						&& resource.name.equals("pattern");
			}
		}).size();
	}

	public static List<Resource> getOrderedResources(User user) {
		if (user == null) {
			return Lists.newArrayList();
		}

		List<Resource> system = Lists.newArrayList();

		// add date if any...
		// hack the resource.... use URI to push a message...
		Resource joined = new Resource();
		if (user.date != null) {
			joined.date = user.date;
		} else {
			joined.date = "0";
		}
		joined.name = "system";
		joined.uri = "You joined Play!";
		system.add(joined);

		ImmutableSortedSet<Resource> r = ImmutableSortedSet
				.orderedBy(new Comparator<Resource>() {
					public int compare(Resource r1, Resource r2) {
						if (Long.parseLong(r1.date) < Long.parseLong(r2.date)) {
							return 1;
						}
						if (Long.parseLong(r1.date) == Long.parseLong(r2.date)) {
							return 0;
						}
						return -1;
					}
				}).addAll(user.groups).addAll(user.resources).addAll(system)
				.build();

		return Lists.newArrayList(r);
	}

}
