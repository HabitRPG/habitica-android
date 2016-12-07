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
 *		Changes: 	removed video and custom ad-specific code
 *				 	renamed from AdResponse
 */

package com.playseeds.android.sdk.inappmessaging;

public class  InAppMessageResponse implements InAppMessage {
	public static final String OTHER = "other";
	private static final long serialVersionUID = 3271938798582141269L;
	private int type;
	private int bannerWidth;
	private int bannerHeight;
	private String text;
	private int skipOverlay = 0;
	private String imageUrl;
	private String productId;
	private String formattedPrice;
	private String manualLocalizedPrice;

	public String getFormattedPrice() {
		boolean overrideWithManuallyEnteredPrice = manualLocalizedPrice != null;

		if (overrideWithManuallyEnteredPrice) {
			return manualLocalizedPrice;
		} else {
			return formattedPrice;
		}
	}

	public InAppMessageResponse setFormattedPrice(String formattedPrice) {
		this.formattedPrice = formattedPrice;
		return this;
	}

	private ClickType clickType;
	private String clickUrl;
	private String urlType;
	private String seedsLinkUrl;
	private int refresh;
	private boolean scale;
	private boolean skipPreflight;
	private long timestamp;
	private boolean horizontalOrientationRequested;
	private String messageId;
	private String messageVariant;
	private String messageContext;

	public String getProductId() {
		return this.productId;
	}

	public boolean isScale() {
		return this.scale;
	}

	public boolean isSkipPreflight() {
		return this.skipPreflight;
	}

	public boolean isHorizontalOrientationRequested() {
		return horizontalOrientationRequested;
	}

	public void setBannerHeight(final int bannerHeight) {
		this.bannerHeight = bannerHeight;
	}

	public void setBannerWidth(final int bannerWidth) {
		this.bannerWidth = bannerWidth;
	}

	public void setClickType(final ClickType clickType) {
		this.clickType = clickType;
	}

	public void setClickUrl(final String clickUrl) {
		this.clickUrl = clickUrl;
	}

	public void setSeedsLinkUrl(final String seedsLinkUrl) {
		this.seedsLinkUrl = seedsLinkUrl;
	}

	public void setProductId(final String productId) {
		this.productId = productId;
	}

	public void setImageUrl(final String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setRefresh(final int refresh) {
		this.refresh = refresh;
	}

	public void setScale(final boolean scale) {
		this.scale = scale;
	}

	public void setSkipPreflight(final boolean skipPreflight) {
		this.skipPreflight = skipPreflight;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public void setUrlType(final String urlType) {
		this.urlType = urlType;
	}

	@Override
	public void setType(final int adType) {
		this.type = adType;
	}

	public void setSkipOverlay(int skipOverlay) {
		this.skipOverlay = skipOverlay;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setHorizontalOrientationRequested(boolean horizontalOrientationRequested) {
		this.horizontalOrientationRequested = horizontalOrientationRequested;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getBannerHeight() {
		return this.bannerHeight;
	}

	public int getBannerWidth() {
		return this.bannerWidth;
	}

	public ClickType getClickType() {
		return this.clickType;
	}

	public String getClickUrl() {
		return this.clickUrl;
	}

	public String getSeedsLinkUrl() {
		return this.seedsLinkUrl;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public int getRefresh() {
		return this.refresh;
	}

	public String getText() {
		return this.text;
	}

	public String getUrlType() {
		return this.urlType;
	}

	@Override
	public int getType() {
		return this.type;
	}

	public String getString() {
		return "Response [refresh=" + this.refresh + ", type=" + this.type
				+ ", bannerWidth=" + this.bannerWidth + ", bannerHeight="
				+ this.bannerHeight + ", text=" + this.text + ", imageUrl="
				+ this.imageUrl + ", clickType=" + this.clickType
				+ ", clickUrl=" + this.clickUrl + ", urlType=" + this.urlType
				+ ", scale=" + this.scale + ", skipPreflight="
				+ this.skipPreflight + "]";
	}

	public int getSkipOverlay() {
		return skipOverlay;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageIdRequested) {
		this.messageId = messageIdRequested;
	}

	public String getMessageVariant() {
		return messageVariant != null ? messageVariant : "";
	}

	public void setMessageVariant(String messageVariant) {
		this.messageVariant = messageVariant;
	}

	public String getMessageContext() {
		return messageContext != null ? messageContext : "";
	}

	public void setMessageContext(String messageContext) {
		this.messageContext = messageContext;
	}


	public void setManualLocalizedPrice(String manualLocalizedPrice) {
		this.manualLocalizedPrice = manualLocalizedPrice;
	}
}