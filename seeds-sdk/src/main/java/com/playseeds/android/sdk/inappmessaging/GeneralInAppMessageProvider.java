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
 *		Changes: 	removed video, custom event and MRAID-related code
 *					renamed from RequestGeneralAd
 */

package com.playseeds.android.sdk.inappmessaging;

import com.playseeds.android.sdk.Seeds;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;


public class GeneralInAppMessageProvider extends InAppMessageProvider<InAppMessageResponse> {

	public GeneralInAppMessageProvider() {
	}

	public InAppMessageResponse parseCountlyJSON(final InputStream inputStream,  Map<String, List<String>> headers) throws RequestException {

		Log.i("Starting parseCountlyJSON");

		final InAppMessageResponse response = new InAppMessageResponse();
		response.setType(Const.TEXT);

		ClickType clickType = ClickType.getValue("inapp");
		response.setClickType(clickType);
		response.setRefresh(60);
		response.setScale(false);
		response.setSkipPreflight(true);

		try {
			JsonReader jsonReader = Json.createReader(inputStream);
			JsonObject jsonObject = jsonReader.readObject();
			response.setText(jsonObject.getString("htmlString"));
			JsonValue jsonClickUrl = jsonObject.get("clickurl");

			if (jsonClickUrl != null && !jsonClickUrl.equals(JsonValue.NULL) &&
					(jsonClickUrl instanceof JsonString)) {
				response.setClickUrl(((JsonString) jsonClickUrl).getString());
			} else {
				response.setSkipOverlay(1);
			}

			JsonValue jsonProductId = jsonObject.get("productIdAndroid");
			if (jsonProductId != null && !jsonProductId.equals(JsonValue.NULL) &&
					(jsonProductId instanceof JsonString)) {
				response.setProductId(((JsonString) jsonProductId).getString());
			}

			JsonValue jsonMessageVariant = jsonObject.get("messageVariant");
			if (jsonMessageVariant != null && !jsonMessageVariant.equals(JsonValue.NULL) &&
					(jsonMessageVariant instanceof JsonString)) {
				response.setMessageVariant(((JsonString) jsonMessageVariant).getString());
			}

			// result of policies such as do not show to paying users
			if (jsonObject.containsKey("doNotShow")) {
				boolean doNotShow = jsonObject.getBoolean("doNotShow");
				InAppMessageManager.sharedInstance().doNotShow(doNotShow);
			} else {
				// show it!
				InAppMessageManager.sharedInstance().doNotShow(false);
			}

			jsonReader.close();

		} catch (final Throwable t) {
			Log.e(t.toString());
			throw new RequestException("Cannot read Response", t);
		}

		return response;
	}
}