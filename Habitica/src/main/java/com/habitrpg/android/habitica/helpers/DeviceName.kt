package com.habitrpg.android.habitica.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.annotation.WorkerThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/*
* Copyright (C) 2017 Jared Rummler
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ // @formatter:off
/**
 *
 * Get the consumer friendly name of an Android device.
 *
 *
 * On many popular devices the market name of the device is not available. For example, on the
 * Samsung Galaxy S6 the value of [Build.MODEL] could be "SM-G920F", "SM-G920I", "SM-G920W8",
 * etc.
 *
 *
 * See the usages below to get the consumer friends name of a device:
 *
 *
 * **Get the name of the current device:**
 *
 * <pre>
 * String deviceName = DeviceName.getDeviceName();
</pre> *
 *
 *
 * The above code will get the correct device name for the top 600 Android devices. If the
 * device is unrecognized, then Build.MODEL is returned.
 *
 *
 * **Get the name of a device using the device's codename:**
 *
 * <pre>
 * // Retruns "Moto X Style"
 * DeviceName.getDeviceName("clark", "Unknown device");
</pre> *
 *
 *
 * **Get information about the device:**
 *
 * <pre>
 * DeviceName.with(context).request(new DeviceName.Callback() {
 *
 * &#64;Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
 * String manufacturer = info.manufacturer;  // "Samsung"
 * String name = info.marketName;            // "Galaxy S6 Edge"
 * String model = info.model;                // "SM-G925I"
 * String codename = info.codename;          // "zerolte"
 * String deviceName = info.getName();       // "Galaxy S6 Edge"
 * // FYI: We are on the UI thread.
 * }
 * });
</pre> *
 *
 *
 * The above code loads JSON from a generated list of device names based on Google's maintained
 * list. It will be up-to-date with Google's supported device list so that you will get the correct
 * name for new or unknown devices. This supports over 10,000 devices.
 *
 *
 * This will only make a network call once. The value is saved to SharedPreferences for future
 * calls.
 */
object DeviceName {
    // @formatter:on
    // JSON which is derived from Google's PDF document which contains all devices on Google Play.
    // To get the URL to the JSON file which contains information about the device name:
    // String url = String.format(DEVICE_JSON_URL, Build.DEVICE);
    private const val DEVICE_JSON_URL = "https://raw.githubusercontent.com/jaredrummler/AndroidDeviceNames/master/json/devices/%s.json"

    // Preference filename for storing device info so we don't need to download it again.
    private const val SHARED_PREF_NAME = "device_names"

    /**
     * Create a new request to get information about a device.
     *
     * @param context the application context
     * @return a new Request instance.
     */
    fun with(context: Context): Request {
        return Request(context.applicationContext)
    }

    /**
     * Get the consumer friendly name of the device.
     *
     * @return the market name of the current device.
     * @see .getDeviceName
     */
    val deviceName: String?
        get() = getDeviceName(Build.DEVICE, Build.MODEL, Build.MODEL.capitalize(Locale.getDefault()))

    /**
     * Get the consumer friendly name of a device.
     *
     * @param codename the value of the system property "ro.product.device" ([Build.DEVICE])
     * *or*
     * the value of the system property "ro.product.model" ([Build.MODEL])
     * @param fallback the fallback name if the device is unknown. Usually the value of the system property
     * "ro.product.model" ([Build.MODEL])
     * @return the market name of a device or `fallback` if the device is unknown.
     */
    fun getDeviceName(codename: String?, fallback: String?): String? {
        return getDeviceName(codename, codename, fallback)
    }

    /**
     * Get the consumer friendly name of a device.
     *
     * @param codename the value of the system property "ro.product.device" ([Build.DEVICE]).
     * @param model the value of the system property "ro.product.model" ([Build.MODEL]).
     * @param fallback the fallback name if the device is unknown. Usually the value of the system property
     * "ro.product.model" ([Build.MODEL])
     * @return the market name of a device or `fallback` if the device is unknown.
     */
    fun getDeviceName(codename: String?, model: String?, fallback: String?): String? {
        // ----------------------------------------------------------------------------
        // Google
        if (codename != null && codename == "walleye") {
            return "Pixel 2"
        }
        if (codename != null && codename == "taimen") {
            return "Pixel 2 XL"
        }
        if (codename != null && codename == "blueline") {
            return "Pixel 3"
        }
        if (codename != null && codename == "crosshatch") {
            return "Pixel 3 XL"
        }
        if (codename != null && codename == "sargo") {
            return "Pixel 3a"
        }
        if (codename != null && codename == "bonito") {
            return "Pixel 3a XL"
        }
        // ----------------------------------------------------------------------------
        // Huawei
        if (codename != null && codename == "HWBND-H" || model != null && (model == "BND-L21" || model == "BND-L24" || model == "BND-L31")) {
            return "Honor 7X"
        }
        if (codename != null && codename == "HWBKL" || model != null && (
            model == "BKL-L04" || model ==
                "BKL-L09"
            )
        ) {
            return "Honor View 10"
        }
        if (codename != null && codename == "HWALP" || model != null && (
            model == "ALP-AL00" || model ==
                "ALP-L09" || model == "ALP-L29" || model == "ALP-TL00"
            )
        ) {
            return "Mate 10"
        }
        if (codename != null && codename == "HWMHA" || model != null && (
            model == "MHA-AL00" || model ==
                "MHA-L09" || model == "MHA-L29" || model == "MHA-TL00"
            )
        ) {
            return "Mate 9"
        }
        if (codename != null && codename == "angler") {
            return "Nexus 6P"
        }
        // ----------------------------------------------------------------------------
        // LGE
        if (codename != null && codename == "h1" || model != null && (
            model == "LG-F700K" || model ==
                "LG-F700L" || model == "LG-F700S" || model == "LG-H820" || model == "LG-H820PR" || model ==
                "LG-H830" || model == "LG-H831" || model == "LG-H850" || model == "LG-H858" || model ==
                "LG-H860" || model == "LG-H868" || model == "LGAS992" || model == "LGLS992" || model ==
                "LGUS992" || model == "RS988" || model == "VS987"
            )
        ) {
            return "LG G5"
        }
        if (codename != null && codename == "lucye" || model != null && (model == "LG-AS993" || model == "LG-H870" || model == "LG-H870AR" || model == "LG-H870DS" || model == "LG-H870I" || model == "LG-H870S" || model == "LG-H871" || model == "LG-H871S" || model == "LG-H872" || model == "LG-H872PR" || model == "LG-H873" || model == "LG-LS993" || model == "LGM-G600K" || model == "LGM-G600L" || model == "LGM-G600S" || model == "LGUS997" || model == "VS988")) {
            return "LG G6"
        }
        if (codename != null && codename == "flashlmdd" || model != null && (model == "LM-V500" || model == "LM-V500N")) {
            return "LG V50 ThinQ"
        }
        if (codename != null && codename == "mako") {
            return "Nexus 4"
        }
        if (codename != null && codename == "hammerhead") {
            return "Nexus 5"
        }
        if (codename != null && codename == "bullhead") {
            return "Nexus 5X"
        }
        // ----------------------------------------------------------------------------
        // Motorola
        if (codename != null && codename == "griffin" || model != null && (
            model == "XT1650" || model ==
                "XT1650-05"
            )
        ) {
            return "Moto Z"
        }
        if (codename != null && codename == "shamu") {
            return "Nexus 6"
        }
        // ----------------------------------------------------------------------------
        // Nokia
        if (codename != null && (codename == "RHD" || codename == "ROO" || codename == "ROON_sprout" || codename == "ROO_sprout")) {
            return "Nokia 3.1 Plus"
        }
        if (codename != null && codename == "CTL_sprout") {
            return "Nokia 7.1"
        }
        // ----------------------------------------------------------------------------
        // OnePlus
        if (codename != null && codename == "OnePlus6" || model != null && model == "ONEPLUS A6003") {
            return "OnePlus 6"
        }
        if (codename != null && (codename == "OnePlus6T" || codename == "OnePlus6TSingle") || (
            model != null &&
                model == "ONEPLUS A6013"
            )
        ) {
            return "OnePlus 6T"
        }
        if (codename != null && codename == "OnePlus7" || model != null && model == "GM1905") {
            return "OnePlus 7"
        }
        if (codename != null && (codename == "OnePlus7Pro" || codename == "OnePlus7ProTMO") || (
            model != null &&
                (model == "GM1915" || model == "GM1917")
            )
        ) {
            return "OnePlus 7 Pro"
        }
        // ----------------------------------------------------------------------------
        // Samsung
        if (codename != null && codename == "a50" || model != null && (model == "SM-A505F" || model == "SM-A505FM" || model == "SM-A505FN" || model == "SM-A505G" || model == "SM-A505GN" || model == "SM-A505GT" || model == "SM-A505N" || model == "SM-A505U" || model == "SM-A505W" || model == "SM-A505YN")) {
            return "Galaxy A50"
        }
        if (codename != null && (codename == "a6elteaio" || codename == "a6elteatt" || codename == "a6eltemtr" || codename == "a6eltespr" || codename == "a6eltetmo" || codename == "a6elteue" || codename == "a6lte" || codename == "a6lteks") || model != null && (model == "SM-A600A" || model == "SM-A600AZ" || model == "SM-A600F" || model == "SM-A600FN" || model == "SM-A600G" || model == "SM-A600GN" || model == "SM-A600N" || model == "SM-A600P" || model == "SM-A600T" || model == "SM-A600T1" || model == "SM-A600U")) {
            return "Galaxy A6"
        }
        if (codename != null && (codename == "SC-01J" || codename == "SCV34" || codename == "gracelte" || codename == "graceltektt" || codename == "graceltelgt" || codename == "gracelteskt" || codename == "graceqlteacg" || codename == "graceqlteatt" || codename == "graceqltebmc" || codename == "graceqltechn" || codename == "graceqltedcm" || codename == "graceqltelra" || codename == "graceqltespr" || codename == "graceqltetfnvzw" || codename == "graceqltetmo" || codename == "graceqlteue" || codename == "graceqlteusc" || codename == "graceqltevzw") || model != null && (model == "SAMSUNG-SM-N930A" || model == "SC-01J" || model == "SCV34" || model == "SGH-N037" || model == "SM-N9300" || model == "SM-N930F" || model == "SM-N930K" || model == "SM-N930L" || model == "SM-N930P" || model == "SM-N930R4" || model == "SM-N930R6" || model == "SM-N930R7" || model == "SM-N930S" || model == "SM-N930T" || model == "SM-N930U" || model == "SM-N930V" || model == "SM-N930VL" || model == "SM-N930W8" || model == "SM-N930X")) {
            return "Galaxy Note7"
        }
        if (codename != null && (codename == "SC-01K" || codename == "SCV37" || codename == "greatlte" || codename == "greatlteks" || codename == "greatqlte" || codename == "greatqltechn" || codename == "greatqltecmcc" || codename == "greatqltecs" || codename == "greatqlteue") || model != null && (model == "SC-01K" || model == "SCV37" || model == "SM-N9500" || model == "SM-N9508" || model == "SM-N950F" || model == "SM-N950N" || model == "SM-N950U" || model == "SM-N950U1" || model == "SM-N950W" || model == "SM-N950XN")) {
            return "Galaxy Note8"
        }
        if (codename != null && (codename == "SC-01L" || codename == "SCV40" || codename == "crownlte" || codename == "crownlteks" || codename == "crownqltechn" || codename == "crownqltecs" || codename == "crownqltesq" || codename == "crownqlteue") || model != null && (model == "SC-01L" || model == "SCV40" || model == "SM-N9600" || model == "SM-N960F" || model == "SM-N960N" || model == "SM-N960U" || model == "SM-N960U1" || model == "SM-N960W")) {
            return "Galaxy Note9"
        }
        if (codename != null && (codename == "SC-03L" || codename == "SCV41" || codename == "beyond1" || codename == "beyond1q") || model != null && (model == "SC-03L" || model == "SCV41" || model == "SM-G9730" || model == "SM-G9738" || model == "SM-G973F" || model == "SM-G973N" || model == "SM-G973U" || model == "SM-G973U1" || model == "SM-G973W")) {
            return "Galaxy S10"
        }
        if (codename != null && (codename == "SC-04L" || codename == "SCV42" || codename == "beyond2" || codename == "beyond2q") || model != null && (model == "SC-04L" || model == "SCV42" || model == "SM-G9750" || model == "SM-G9758" || model == "SM-G975F" || model == "SM-G975N" || model == "SM-G975U" || model == "SM-G975U1" || model == "SM-G975W")) {
            return "Galaxy S10+"
        }
        if (codename != null && (codename == "beyond0" || codename == "beyond0q") || (
            model != null &&
                (model == "SM-G9700" || model == "SM-G9708" || model == "SM-G970F" || model == "SM-G970N" || model == "SM-G970U" || model == "SM-G970U1" || model == "SM-G970W")
            )
        ) {
            return "Galaxy S10e"
        }
        if (codename != null && (codename == "SC-04F" || codename == "SCL23" || codename == "k3g" || codename == "klte" || codename == "klteMetroPCS" || codename == "klteacg" || codename == "klteaio" || codename == "klteatt" || codename == "kltecan" || codename == "klteduoszn" || codename == "kltektt" || codename == "kltelgt" || codename == "kltelra" || codename == "klteskt" || codename == "kltespr" || codename == "kltetfnvzw" || codename == "kltetmo" || codename == "klteusc" || codename == "kltevzw" || codename == "kwifi" || codename == "lentisltektt" || codename == "lentisltelgt" || codename == "lentislteskt") || model != null && (model == "SAMSUNG-SM-G900A" || model == "SAMSUNG-SM-G900AZ" || model == "SC-04F" || model == "SCL23" || model == "SM-G9006W" || model == "SM-G9008W" || model == "SM-G9009W" || model == "SM-G900F" || model == "SM-G900FQ" || model == "SM-G900H" || model == "SM-G900I" || model == "SM-G900K" || model == "SM-G900L" || model == "SM-G900M" || model == "SM-G900MD" || model == "SM-G900P" || model == "SM-G900R4" || model == "SM-G900R6" || model == "SM-G900R7" || model == "SM-G900S" || model == "SM-G900T" || model == "SM-G900T1" || model == "SM-G900T3" || model == "SM-G900T4" || model == "SM-G900V" || model == "SM-G900W8" || model == "SM-G900X" || model == "SM-G906K" || model == "SM-G906L" || model == "SM-G906S" || model == "SM-S903VL")) {
            return "Galaxy S5"
        }
        if (codename != null && (codename == "s5neolte" || codename == "s5neoltecan") || model != null && (model == "SM-G903F" || model == "SM-G903M" || model == "SM-G903W")) {
            return "Galaxy S5 Neo"
        }
        if (codename != null && (codename == "SC-05G" || codename == "zeroflte" || codename == "zeroflteacg" || codename == "zeroflteaio" || codename == "zeroflteatt" || codename == "zerofltebmc" || codename == "zerofltechn" || codename == "zerofltectc" || codename == "zerofltektt" || codename == "zerofltelgt" || codename == "zerofltelra" || codename == "zerofltemtr" || codename == "zeroflteskt" || codename == "zerofltespr" || codename == "zerofltetfnvzw" || codename == "zerofltetmo" || codename == "zeroflteusc" || codename == "zerofltevzw") || model != null && (model == "SAMSUNG-SM-G920A" || model == "SAMSUNG-SM-G920AZ" || model == "SC-05G" || model == "SM-G9200" || model == "SM-G9208" || model == "SM-G9209" || model == "SM-G920F" || model == "SM-G920I" || model == "SM-G920K" || model == "SM-G920L" || model == "SM-G920P" || model == "SM-G920R4" || model == "SM-G920R6" || model == "SM-G920R7" || model == "SM-G920S" || model == "SM-G920T" || model == "SM-G920T1" || model == "SM-G920V" || model == "SM-G920W8" || model == "SM-G920X" || model == "SM-S906L" || model == "SM-S907VL")) {
            return "Galaxy S6"
        }
        if (codename != null && (codename == "404SC" || codename == "SC-04G" || codename == "SCV31" || codename == "zerolte" || codename == "zerolteacg" || codename == "zerolteatt" || codename == "zeroltebmc" || codename == "zeroltechn" || codename == "zeroltektt" || codename == "zeroltelra" || codename == "zerolteskt" || codename == "zeroltespr" || codename == "zeroltetmo" || codename == "zerolteusc" || codename == "zeroltevzw") || model != null && (model == "404SC" || model == "SAMSUNG-SM-G925A" || model == "SC-04G" || model == "SCV31" || model == "SM-G9250" || model == "SM-G925I" || model == "SM-G925K" || model == "SM-G925P" || model == "SM-G925R4" || model == "SM-G925R6" || model == "SM-G925R7" || model == "SM-G925S" || model == "SM-G925T" || model == "SM-G925V" || model == "SM-G925W8" || model == "SM-G925X")) {
            return "Galaxy S6 Edge"
        }
        if (codename != null && (codename == "zenlte" || codename == "zenlteatt" || codename == "zenltebmc" || codename == "zenltechn" || codename == "zenltektt" || codename == "zenltekx" || codename == "zenltelgt" || codename == "zenlteskt" || codename == "zenltespr" || codename == "zenltetmo" || codename == "zenlteusc" || codename == "zenltevzw") || model != null && (model == "SAMSUNG-SM-G928A" || model == "SM-G9280" || model == "SM-G9287C" || model == "SM-G928C" || model == "SM-G928G" || model == "SM-G928I" || model == "SM-G928K" || model == "SM-G928L" || model == "SM-G928N0" || model == "SM-G928P" || model == "SM-G928R4" || model == "SM-G928S" || model == "SM-G928T" || model == "SM-G928V" || model == "SM-G928W8" || model == "SM-G928X")) {
            return "Galaxy S6 Edge+"
        }
        if (codename != null && (
            codename == "herolte" || codename == "heroltebmc" || codename ==
                "heroltektt" || codename == "heroltelgt" || codename == "herolteskt" || codename ==
                "heroqlteacg" || codename == "heroqlteaio" || codename == "heroqlteatt" || codename ==
                "heroqltecctvzw" || codename == "heroqltechn" || codename == "heroqltelra" || codename ==
                "heroqltemtr" || codename == "heroqltespr" || codename == "heroqltetfnvzw" || codename ==
                "heroqltetmo" || codename == "heroqlteue" || codename == "heroqlteusc" || codename ==
                "heroqltevzw"
            ) || model != null && (model == "SAMSUNG-SM-G930A" || model == "SAMSUNG-SM-G930AZ" || model == "SM-G9300" || model == "SM-G9308" || model == "SM-G930F" || model == "SM-G930K" || model == "SM-G930L" || model == "SM-G930P" || model == "SM-G930R4" || model == "SM-G930R6" || model == "SM-G930R7" || model == "SM-G930S" || model == "SM-G930T" || model == "SM-G930T1" || model == "SM-G930U" || model == "SM-G930V" || model == "SM-G930VC" || model == "SM-G930VL" || model == "SM-G930W8" || model == "SM-G930X")
        ) {
            return "Galaxy S7"
        }
        if (codename != null && (codename == "SC-02H" || codename == "SCV33" || codename == "hero2lte" || codename == "hero2ltebmc" || codename == "hero2ltektt" || codename == "hero2lteskt" || codename == "hero2qlteatt" || codename == "hero2qltecctvzw" || codename == "hero2qltespr" || codename == "hero2qltetmo" || codename == "hero2qlteusc" || codename == "hero2qltevzw") || model != null && (model == "SAMSUNG-SM-G935A" || model == "SC-02H" || model == "SCV33" || model == "SM-G935K" || model == "SM-G935P" || model == "SM-G935R4" || model == "SM-G935S" || model == "SM-G935T" || model == "SM-G935V" || model == "SM-G935VC" || model == "SM-G935W8" || model == "SM-G935X")) {
            return "Galaxy S7 Edge"
        }
        if (codename != null && (codename == "SC-02J" || codename == "SCV36" || codename == "dreamlte" || codename == "dreamlteks" || codename == "dreamqltecan" || codename == "dreamqltechn" || codename == "dreamqltecmcc" || codename == "dreamqltesq" || codename == "dreamqlteue") || model != null && (model == "SC-02J" || model == "SCV36" || model == "SM-G9500" || model == "SM-G9508" || model == "SM-G950F" || model == "SM-G950N" || model == "SM-G950U" || model == "SM-G950U1" || model == "SM-G950W")) {
            return "Galaxy S8"
        }
        if (codename != null && (codename == "SC-03J" || codename == "SCV35" || codename == "dream2lte" || codename == "dream2lteks" || codename == "dream2qltecan" || codename == "dream2qltechn" || codename == "dream2qltesq" || codename == "dream2qlteue") || model != null && (model == "SC-03J" || model == "SCV35" || model == "SM-G9550" || model == "SM-G955F" || model == "SM-G955N" || model == "SM-G955U" || model == "SM-G955U1" || model == "SM-G955W")) {
            return "Galaxy S8+"
        }
        if (codename != null && (codename == "SC-02K" || codename == "SCV38" || codename == "starlte" || codename == "starlteks" || codename == "starqltechn" || codename == "starqltecmcc" || codename == "starqltecs" || codename == "starqltesq" || codename == "starqlteue") || model != null && (model == "SC-02K" || model == "SCV38" || model == "SM-G9600" || model == "SM-G9608" || model == "SM-G960F" || model == "SM-G960N" || model == "SM-G960U" || model == "SM-G960U1" || model == "SM-G960W")) {
            return "Galaxy S9"
        }
        if (codename != null && (codename == "SC-03K" || codename == "SCV39" || codename == "star2lte" || codename == "star2lteks" || codename == "star2qltechn" || codename == "star2qltecs" || codename == "star2qltesq" || codename == "star2qlteue") || model != null && (model == "SC-03K" || model == "SCV39" || model == "SM-G9650" || model == "SM-G965F" || model == "SM-G965N" || model == "SM-G965U" || model == "SM-G965U1" || model == "SM-G965W")) {
            return "Galaxy S9+"
        }
        // ----------------------------------------------------------------------------
        // Sony
        if (codename != null && (codename == "802SO" || codename == "J8110" || codename == "J8170" || codename == "J9110" || codename == "SO-03L" || codename == "SOV40") || model != null && (model == "802SO" || model == "J8110" || model == "J8170" || model == "J9110" || model == "SO-03L" || model == "SOV40")) {
            return "Xperia 1"
        }
        if (codename != null && (codename == "I3113" || codename == "I3123" || codename == "I4113" || codename == "I4193") || model != null && (model == "I3113" || model == "I3123" || model == "I4113" || model == "I4193")) {
            return "Xperia 10"
        }
        if (codename != null && (codename == "I3213" || codename == "I3223" || codename == "I4213" || codename == "I4293") || model != null && (model == "I3213" || model == "I3223" || model == "I4213" || model == "I4293")) {
            return "Xperia 10 Plus"
        }
        if (codename != null && (codename == "702SO" || codename == "H8216" || codename == "H8266" || codename == "H8276" || codename == "H8296" || codename == "SO-03K" || codename == "SOV37") || model != null && (model == "702SO" || model == "H8216" || model == "H8266" || model == "H8276" || model == "H8296" || model == "SO-03K" || model == "SOV37")) {
            return "Xperia XZ2"
        }
        if (codename != null && (codename == "H8116" || codename == "H8166" || codename == "SO-04K" || codename == "SOV38") || model != null && (model == "H8116" || model == "H8166" || model == "SO-04K" || model == "SOV38")) {
            return "Xperia XZ2 Premium"
        }
        return if (codename != null && (codename == "801SO" || codename == "H8416" || codename == "H9436" || codename == "H9493" || codename == "SO-01L" || codename == "SOV39") || model != null && (model == "801SO" || model == "H8416" || model == "H9436" || model == "H9493" || model == "SO-01L" || model == "SOV39")) {
            "Xperia XZ3"
        } else fallback
    }

    /**
     * Get the [DeviceInfo] for the current device. Do not run on the UI thread, as this may
     * download JSON to retrieve the [DeviceInfo]. JSON is only downloaded once and then
     * stored to [SharedPreferences].
     *
     * @param context the application context.
     * @return [DeviceInfo] for the current device.
     */
    @WorkerThread
    fun getDeviceInfo(context: Context): DeviceInfo {
        return getDeviceInfo(context.applicationContext, Build.DEVICE, Build.MODEL)
    }

    /**
     * Get the [DeviceInfo] for the current device. Do not run on the UI thread, as this may
     * download JSON to retrieve the [DeviceInfo]. JSON is only downloaded once and then
     * stored to [SharedPreferences].
     *
     * @param context the application context.
     * @param codename the codename of the device
     * @return [DeviceInfo] for the current device.
     */
    @WorkerThread
    fun getDeviceInfo(context: Context, codename: String?): DeviceInfo {
        return getDeviceInfo(context, codename, null)
    }

    /**
     * Get the [DeviceInfo] for the current device. Do not run on the UI thread, as this may
     * download JSON to retrieve the [DeviceInfo]. JSON is only downloaded once and then
     * stored to [SharedPreferences].
     *
     * @param context the application context.
     * @param codename the codename of the device
     * @param model the model of the device
     * @return [DeviceInfo] for the current device.
     */
    @WorkerThread
    fun getDeviceInfo(context: Context, codename: String?, model: String?): DeviceInfo {
        val prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val key = String.format(Locale.getDefault(), "%s:%s", codename, model)
        val savedJson = prefs.getString(key, null)
        if (savedJson != null) {
            try {
                return DeviceInfo(JSONObject(savedJson))
            } catch (e: JSONException) {
                RxErrorHandler.reportError(e)
            }
        }

        // check if we have an internet connection
        val ret = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        var isConnectedToNetwork = false
        if (ret == PackageManager.PERMISSION_GRANTED) {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            @SuppressLint("MissingPermission") val networkInfo = connMgr.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                isConnectedToNetwork = true
            }
        } else {
            // assume we are connected.
            isConnectedToNetwork = true
        }
        if (isConnectedToNetwork) {
            try {
                // Get the device name from the generated JSON files created from Google's device list.
                val url = String.format(DEVICE_JSON_URL, codename!!.toLowerCase(Locale.ENGLISH))
                val jsonString = downloadJson(url)
                val jsonArray = JSONArray(jsonString)
                var i = 0
                val len = jsonArray.length()
                while (i < len) {
                    val json = jsonArray.getJSONObject(i)
                    val info = DeviceInfo(json)
                    if (codename.equals(info.codename, ignoreCase = true) && model == null ||
                        codename.equals(info.codename, ignoreCase = true) && model.equals(info.model, ignoreCase = true)
                    ) {
                        // Save to SharedPreferences so we don't need to make another request.
                        val editor = prefs.edit()
                        editor.putString(key, json.toString())
                        editor.apply()
                        return info
                    }
                    i++
                }
            } catch (e: Exception) {
                RxErrorHandler.reportError(e)
            }
        }
        return if (codename == Build.DEVICE && Build.MODEL == model) {
            DeviceInfo(Build.MANUFACTURER, deviceName, codename, model) // current device
        } else DeviceInfo(null, null, codename, model)
        // unknown device
    }

    /** Download URL to String  */
    @WorkerThread
    @Throws(IOException::class)
    private fun downloadJson(myurl: String): String {
        val sb = StringBuilder()
        var reader: BufferedReader? = null
        return try {
            val url = URL(myurl)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                reader = BufferedReader(InputStreamReader(conn.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append('\n')
                }
            }
            sb.toString()
        } finally {
            reader?.close()
        }
    }

    class Request internal constructor(val context: Context) {
        val handler: Handler
        var codename: String? = null
        var model: String? = null

        /**
         * Set the device codename to query. You should also set the model.
         *
         * @param codename the value of the system property "ro.product.device"
         * @return This Request object to allow for chaining of calls to set methods.
         * @see Build.DEVICE
         */
        fun setCodename(codename: String?): Request {
            this.codename = codename
            return this
        }

        /**
         * Set the device model to query. You should also set the codename.
         *
         * @param model the value of the system property "ro.product.model"
         * @return This Request object to allow for chaining of calls to set methods.
         * @see Build.MODEL
         */
        fun setModel(model: String?): Request {
            this.model = model
            return this
        }

        /**
         * Download information about the device. This saves the results in shared-preferences so
         * future requests will not need a network connection.
         *
         * @param callback the callback to retrieve the [DeviceName.DeviceInfo]
         */
        fun request(callback: Callback) {
            if (codename == null && model == null) {
                codename = Build.DEVICE
                model = Build.MODEL
            }
            val runnable = GetDeviceRunnable(callback)
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread(runnable).start()
            } else {
                runnable.run() // already running in background thread.
            }
        }

        private inner class GetDeviceRunnable(val callback: Callback) : Runnable {
            var deviceInfo: DeviceInfo? = null
            var error: Exception? = null
            override fun run() {
                try {
                    deviceInfo = getDeviceInfo(context, codename, model)
                } catch (e: Exception) {
                    error = e
                }
                handler.post { callback.onFinished(deviceInfo, error) }
            }
        }

        init {
            handler = Handler(context.mainLooper)
        }
    }

    /**
     * Callback which is invoked when the [DeviceName.DeviceInfo] is finished loading.
     */
    interface Callback {
        /**
         * Callback to get the device info. This is run on the UI thread.
         *
         * @param info the requested [DeviceName.DeviceInfo]
         * @param error `null` if nothing went wrong.
         */
        fun onFinished(info: DeviceInfo?, error: Exception?)
    }

    /**
     * Device information based on
     * [Google's maintained list](https://support.google.com/googleplay/answer/1727131).
     */
    class DeviceInfo {
        /** Retail branding  */
        val manufacturer: String?

        /** Marketing name  */
        val marketName: String?

        /** the value of the system property "ro.product.device"  */
        val codename: String?

        /** the value of the system property "ro.product.model"  */
        val model: String?

        constructor(manufacturer: String?, marketName: String?, codename: String?, model: String?) {
            this.manufacturer = manufacturer
            this.marketName = marketName
            this.codename = codename
            this.model = model
        }

        constructor(jsonObject: JSONObject) {
            manufacturer = jsonObject.getString("manufacturer")
            marketName = jsonObject.getString("market_name")
            codename = jsonObject.getString("codename")
            model = jsonObject.getString("model")
        }

        /**
         * @return the consumer friendly name of the device.
         */
        val name: String?
            get() = if (!TextUtils.isEmpty(marketName)) {
                marketName
            } else model?.capitalize(Locale.getDefault())
    }
}
