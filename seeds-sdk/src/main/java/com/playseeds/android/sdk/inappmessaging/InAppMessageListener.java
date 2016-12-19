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
 *     Changes: 	renamed from AdListener
 */


package com.playseeds.android.sdk.inappmessaging;

public interface InAppMessageListener {
	void inAppMessageClicked(String messageId);

    void inAppMessageDismissed(String messageId);

	void inAppMessageLoadSucceeded(String messageId);

	void inAppMessageShown(String messageId, boolean succeeded);

	void noInAppMessageFound(String messageId);

    void inAppMessageClickedWithDynamicPrice(String messageId, Double price);
}
