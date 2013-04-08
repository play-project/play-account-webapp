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
package client.platform.v1;

import java.util.List;

import models.ApplicationException;
import play.Logger;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import client.user.v1.User;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author chamerling
 * 
 */
public class PlatformClient {

	/**
	 * 
	 * @param user
	 * @return
	 * @throws ApplicationException
	 */
	public static List<Stream> streams(User user) throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "streams").setHeader(
				"Authorization", "Bearer " + user.apiToken);

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			Stream[] streams = gson.fromJson(json, Stream[].class);
			return Lists.newArrayList(streams);
		} else {
			Logger.debug("Status %s", response.getStatusText());
			throw new ApplicationException("Bad request : "
					+ response.getStatusText());
		}
	}

	public static List<Subscription> subscriptions(User user)
			throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "subscriptions").setHeader(
				"Authorization", "Bearer " + user.apiToken);

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			Subscription[] subscription = gson.fromJson(json,
					Subscription[].class);

			return Lists.newArrayList(subscription);
		} else {
			Logger.debug("Status %s", response.getStatusText());
			throw new ApplicationException("Bad request : "
					+ response.getStatusText());
		}
	}

	/**
	 * Subscribe and get back a subscription if all is OK. TODO : Do it async...
	 * 
	 * @param user
	 * @param resource
	 * @param subscriber
	 * @return
	 * @throws ApplicationException
	 */
	public static Subscription subscribe(User user, String resource,
			String subscriber) throws ApplicationException {

		Subscription subscription = new Subscription();
		subscription.resource = resource;
		subscription.subscriber = subscriber;

		WSRequest request = WS.url(getEndpoint() + "subscriptions")
				.setHeader("Authorization", "Bearer " + user.apiToken)
				.mimeType("application/json")
				.body(new Gson().toJson(subscription));

		HttpResponse response = null;
		try {
			response = request.post();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() == 201) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			Subscription s = gson.fromJson(json, Subscription.class);
			return s;
		} else {
			Logger.debug("Status %s", response.getStatusText());
			throw new ApplicationException("Bad request : "
					+ response.getStatusText());
		}
	}

	/**
	 * Subscribe and get back a subscription if all is OK. TODO : Do it async...
	 * 
	 * @param user
	 * @param resource
	 * @param subscriber
	 * @return
	 * @throws ApplicationException
	 */
	public static boolean unsubscribe(User user, String subscriptionID)
			throws ApplicationException {

		WSRequest request = WS.url(getEndpoint() + "subscriptions/" + subscriptionID)
				.setHeader("Authorization", "Bearer " + user.apiToken);
		
		HttpResponse response = null;
		try {
			response = request.delete();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() == 204) {
			return true;
		} else {
			Logger.debug("Status %s", response.getStatusText());
			throw new ApplicationException("Bad request : "
					+ response.getStatusText());
		}
	}

	public static List<Pattern> patterns(User user) throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "patterns").setHeader(
				"Authorization", "Bearer " + user.apiToken);

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			Pattern[] pattern = gson.fromJson(json, Pattern[].class);
			return Lists.newArrayList(pattern);
		} else {
			Logger.debug("Status %s", response.getStatusText());
			throw new ApplicationException("Bad request : "
					+ response.getStatusText());
		}
	}

	public static Pattern pattern(User user, String id)
			throws ApplicationException {
		WSRequest request = WS.url(getEndpoint() + "patterns/" + id).setHeader(
				"Authorization", "Bearer " + user.apiToken);

		HttpResponse response = null;
		try {
			response = request.get();
		} catch (RuntimeException e) {
			throw new ApplicationException("Can not connect to service");
		}

		if (response.getStatus() == 200) {
			JsonElement json = response.getJson();
			Gson gson = new Gson();
			return gson.fromJson(json, Pattern.class);
		} else {
			Logger.debug("Status %s", response.getStatusText());
			throw new ApplicationException("Bad request : "
					+ response.getStatusText());
		}
	}

	public static String getEndpoint() {
		return "http://localhost:8080/play/api/v1/platform/";
	}

}
