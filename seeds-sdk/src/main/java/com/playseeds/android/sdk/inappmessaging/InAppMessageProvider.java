/*
 *		Copyright 2015 MobFox
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *		Unless required by applicable law or agreed to in writing, software
 *		distributed under the License is distributed on an "AS IS" BASIS,
 *		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *		See the License for the specific language governing permissions and
 *		limitations under the License.
 *
 *		Changes:	renamed from RequestAd
 *
 */

package com.playseeds.android.sdk.inappmessaging;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract class InAppMessageProvider<T> {

	public T obtainInAppMessage(InAppMessageRequest request) throws RequestException {
		Log.i("sendCountlyRequest");
		Log.d("Parse Real");

		String url = request.countlyUriToString();
		HttpURLConnection urlConnection = null;

		Log.i("InAppMessage RequestPerform HTTP Get Url: " + url);

		try {
			urlConnection = (HttpURLConnection) new URL(url).openConnection();
			//urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
			InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
			int responseCode = urlConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return parseCountlyJSON(inputStream, urlConnection.getHeaderFields());
			} else {
				throw new RequestException("Server Error. Response code:"
						+ responseCode);
			}
		} catch (Throwable t) {
			throw new RequestException("Error in HTTP request", t);
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}

	protected abstract T parseCountlyJSON(InputStream inputStream,  Map<String, List<String>> headers) throws RequestException;
}
