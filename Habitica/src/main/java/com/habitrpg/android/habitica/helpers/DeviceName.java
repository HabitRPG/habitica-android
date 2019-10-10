package com.habitrpg.android.habitica.helpers;

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
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

// @formatter:off
/**
 * <p>Get the consumer friendly name of an Android device.</p>
 *
 * <p>On many popular devices the market name of the device is not available. For example, on the
 * Samsung Galaxy S6 the value of {@link Build#MODEL} could be "SM-G920F", "SM-G920I", "SM-G920W8",
 * etc.</p>
 *
 * <p>See the usages below to get the consumer friends name of a device:</p>
 *
 * <p><b>Get the name of the current device:</b></p>
 *
 * <pre>
 * String deviceName = DeviceName.getDeviceName();
 * </pre>
 *
 * <p>The above code will get the correct device name for the top 600 Android devices. If the
 * device is unrecognized, then Build.MODEL is returned.</p>
 *
 * <p><b>Get the name of a device using the device's codename:</b></p>
 *
 * <pre>
 * // Retruns "Moto X Style"
 * DeviceName.getDeviceName("clark", "Unknown device");
 * </pre>
 *
 * <p><b>Get information about the device:</b></p>
 *
 * <pre>
 * DeviceName.with(context).request(new DeviceName.Callback() {
 *
 *   &#64;Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
 *     String manufacturer = info.manufacturer;  // "Samsung"
 *     String name = info.marketName;            // "Galaxy S6 Edge"
 *     String model = info.model;                // "SM-G925I"
 *     String codename = info.codename;          // "zerolte"
 *     String deviceName = info.getName();       // "Galaxy S6 Edge"
 *     // FYI: We are on the UI thread.
 *   }
 * });
 * </pre>
 *
 * <p>The above code loads JSON from a generated list of device names based on Google's maintained
 * list. It will be up-to-date with Google's supported device list so that you will get the correct
 * name for new or unknown devices. This supports over 10,000 devices.</p>
 *
 * <p>This will only make a network call once. The value is saved to SharedPreferences for future
 * calls.</p>
 */
public class DeviceName {
    // @formatter:on

    // JSON which is derived from Google's PDF document which contains all devices on Google Play.
    // To get the URL to the JSON file which contains information about the device name:
    // String url = String.format(DEVICE_JSON_URL, Build.DEVICE);
    private static final String DEVICE_JSON_URL =
            "https://raw.githubusercontent.com/jaredrummler/AndroidDeviceNames/master/json/devices/%s.json";

    // Preference filename for storing device info so we don't need to download it again.
    private static final String SHARED_PREF_NAME = "device_names";

    /**
     * Create a new request to get information about a device.
     *
     * @param context the application context
     * @return a new Request instance.
     */
    public static Request with(Context context) {
        return new Request(context.getApplicationContext());
    }

    /**
     * Get the consumer friendly name of the device.
     *
     * @return the market name of the current device.
     * @see #getDeviceName(String, String)
     */
    public static String getDeviceName() {
        return getDeviceName(Build.DEVICE, Build.MODEL, capitalize(Build.MODEL));
    }

    /**
     * Get the consumer friendly name of a device.
     *
     * @param codename the value of the system property "ro.product.device" ({@link Build#DEVICE})
     * <i>or</i>
     * the value of the system property "ro.product.model" ({@link Build#MODEL})
     * @param fallback the fallback name if the device is unknown. Usually the value of the system property
     * "ro.product.model" ({@link Build#MODEL})
     * @return the market name of a device or {@code fallback} if the device is unknown.
     */
    public static String getDeviceName(String codename, String fallback) {
        return getDeviceName(codename, codename, fallback);
    }

    /**
     * Get the consumer friendly name of a device.
     *
     * @param codename the value of the system property "ro.product.device" ({@link Build#DEVICE}).
     * @param model the value of the system property "ro.product.model" ({@link Build#MODEL}).
     * @param fallback the fallback name if the device is unknown. Usually the value of the system property
     * "ro.product.model" ({@link Build#MODEL})
     * @return the market name of a device or {@code fallback} if the device is unknown.
     */
    public static String getDeviceName(String codename, String model, String fallback) {
        // ----------------------------------------------------------------------------
        // Google
        if ((codename != null && codename.equals("walleye"))) {
            return "Pixel 2";
        }
        if ((codename != null && codename.equals("taimen"))) {
            return "Pixel 2 XL";
        }
        if ((codename != null && codename.equals("blueline"))) {
            return "Pixel 3";
        }
        if ((codename != null && codename.equals("crosshatch"))) {
            return "Pixel 3 XL";
        }
        if ((codename != null && codename.equals("sargo"))) {
            return "Pixel 3a";
        }
        if ((codename != null && codename.equals("bonito"))) {
            return "Pixel 3a XL";
        }
        // ----------------------------------------------------------------------------
        // Huawei
        if ((codename != null && (codename.equals("HWBND-H"))) || (model != null && (model.equals("BND-L21")
                || model.equals("BND-L24")
                || model.equals("BND-L31")))) {
            return "Honor 7X";
        }
        if ((codename != null && (codename.equals("HWBKL"))) || (model != null && (model.equals("BKL-L04") || model.equals(
                "BKL-L09")))) {
            return "Honor View 10";
        }
        if ((codename != null && (codename.equals("HWALP"))) || (model != null && (model.equals("ALP-AL00") || model.equals(
                "ALP-L09") || model.equals("ALP-L29") || model.equals("ALP-TL00")))) {
            return "Mate 10";
        }
        if ((codename != null && (codename.equals("HWMHA"))) || (model != null && (model.equals("MHA-AL00") || model.equals(
                "MHA-L09") || model.equals("MHA-L29") || model.equals("MHA-TL00")))) {
            return "Mate 9";
        }
        if ((codename != null && codename.equals("angler"))) {
            return "Nexus 6P";
        }
        // ----------------------------------------------------------------------------
        // LGE
        if ((codename != null && (codename.equals("h1"))) || (model != null && (model.equals("LG-F700K") || model.equals(
                "LG-F700L") || model.equals("LG-F700S") || model.equals("LG-H820") || model.equals("LG-H820PR") || model.equals(
                "LG-H830") || model.equals("LG-H831") || model.equals("LG-H850") || model.equals("LG-H858") || model.equals(
                "LG-H860") || model.equals("LG-H868") || model.equals("LGAS992") || model.equals("LGLS992") || model.equals(
                "LGUS992") || model.equals("RS988") || model.equals("VS987")))) {
            return "LG G5";
        }
        if ((codename != null && (codename.equals("lucye"))) || (model != null && (model.equals("LG-AS993")
                || model.equals("LG-H870")
                || model.equals("LG-H870AR")
                || model.equals("LG-H870DS")
                || model.equals("LG-H870I")
                || model.equals("LG-H870S")
                || model.equals("LG-H871")
                || model.equals("LG-H871S")
                || model.equals("LG-H872")
                || model.equals("LG-H872PR")
                || model.equals("LG-H873")
                || model.equals("LG-LS993")
                || model.equals("LGM-G600K")
                || model.equals("LGM-G600L")
                || model.equals("LGM-G600S")
                || model.equals("LGUS997")
                || model.equals("VS988")))) {
            return "LG G6";
        }
        if ((codename != null && (codename.equals("flashlmdd"))) || (model != null && (model.equals("LM-V500")
                || model.equals("LM-V500N")))) {
            return "LG V50 ThinQ";
        }
        if ((codename != null && codename.equals("mako"))) {
            return "Nexus 4";
        }
        if ((codename != null && codename.equals("hammerhead"))) {
            return "Nexus 5";
        }
        if ((codename != null && codename.equals("bullhead"))) {
            return "Nexus 5X";
        }
        // ----------------------------------------------------------------------------
        // Motorola
        if ((codename != null && (codename.equals("griffin"))) || (model != null && (model.equals("XT1650") || model.equals(
                "XT1650-05")))) {
            return "Moto Z";
        }
        if ((codename != null && codename.equals("shamu"))) {
            return "Nexus 6";
        }
        // ----------------------------------------------------------------------------
        // Nokia
        if ((codename != null && (codename.equals("RHD")
                || codename.equals("ROO")
                || codename.equals("ROON_sprout")
                || codename.equals("ROO_sprout")))) {
            return "Nokia 3.1 Plus";
        }
        if ((codename != null && codename.equals("CTL_sprout"))) {
            return "Nokia 7.1";
        }
        // ----------------------------------------------------------------------------
        // OnePlus
        if ((codename != null && codename.equals("OnePlus6")) || (model != null && model.equals("ONEPLUS A6003"))) {
            return "OnePlus 6";
        }
        if ((codename != null && (codename.equals("OnePlus6T") || codename.equals("OnePlus6TSingle"))) || (model != null
                && (model.equals("ONEPLUS A6013")))) {
            return "OnePlus 6T";
        }
        if ((codename != null && codename.equals("OnePlus7")) || (model != null && model.equals("GM1905"))) {
            return "OnePlus 7";
        }
        if ((codename != null && (codename.equals("OnePlus7Pro") || codename.equals("OnePlus7ProTMO"))) || (model != null
                && (model.equals("GM1915") || model.equals("GM1917")))) {
            return "OnePlus 7 Pro";
        }
        // ----------------------------------------------------------------------------
        // Samsung
        if ((codename != null && (codename.equals("a50"))) || (model != null && (model.equals("SM-A505F")
                || model.equals("SM-A505FM")
                || model.equals("SM-A505FN")
                || model.equals("SM-A505G")
                || model.equals("SM-A505GN")
                || model.equals("SM-A505GT")
                || model.equals("SM-A505N")
                || model.equals("SM-A505U")
                || model.equals("SM-A505W")
                || model.equals("SM-A505YN")))) {
            return "Galaxy A50";
        }
        if ((codename != null && (codename.equals("a6elteaio")
                || codename.equals("a6elteatt")
                || codename.equals("a6eltemtr")
                || codename.equals("a6eltespr")
                || codename.equals("a6eltetmo")
                || codename.equals("a6elteue")
                || codename.equals("a6lte")
                || codename.equals("a6lteks"))) || (model != null && (model.equals("SM-A600A")
                || model.equals("SM-A600AZ")
                || model.equals("SM-A600F")
                || model.equals("SM-A600FN")
                || model.equals("SM-A600G")
                || model.equals("SM-A600GN")
                || model.equals("SM-A600N")
                || model.equals("SM-A600P")
                || model.equals("SM-A600T")
                || model.equals("SM-A600T1")
                || model.equals("SM-A600U")))) {
            return "Galaxy A6";
        }
        if ((codename != null && (codename.equals("SC-01J")
                || codename.equals("SCV34")
                || codename.equals("gracelte")
                || codename.equals("graceltektt")
                || codename.equals("graceltelgt")
                || codename.equals("gracelteskt")
                || codename.equals("graceqlteacg")
                || codename.equals("graceqlteatt")
                || codename.equals("graceqltebmc")
                || codename.equals("graceqltechn")
                || codename.equals("graceqltedcm")
                || codename.equals("graceqltelra")
                || codename.equals("graceqltespr")
                || codename.equals("graceqltetfnvzw")
                || codename.equals("graceqltetmo")
                || codename.equals("graceqlteue")
                || codename.equals("graceqlteusc")
                || codename.equals("graceqltevzw"))) || (model != null && (model.equals("SAMSUNG-SM-N930A")
                || model.equals("SC-01J")
                || model.equals("SCV34")
                || model.equals("SGH-N037")
                || model.equals("SM-N9300")
                || model.equals("SM-N930F")
                || model.equals("SM-N930K")
                || model.equals("SM-N930L")
                || model.equals("SM-N930P")
                || model.equals("SM-N930R4")
                || model.equals("SM-N930R6")
                || model.equals("SM-N930R7")
                || model.equals("SM-N930S")
                || model.equals("SM-N930T")
                || model.equals("SM-N930U")
                || model.equals("SM-N930V")
                || model.equals("SM-N930VL")
                || model.equals("SM-N930W8")
                || model.equals("SM-N930X")))) {
            return "Galaxy Note7";
        }
        if ((codename != null && (codename.equals("SC-01K")
                || codename.equals("SCV37")
                || codename.equals("greatlte")
                || codename.equals("greatlteks")
                || codename.equals("greatqlte")
                || codename.equals("greatqltechn")
                || codename.equals("greatqltecmcc")
                || codename.equals("greatqltecs")
                || codename.equals("greatqlteue"))) || (model != null && (model.equals("SC-01K")
                || model.equals("SCV37")
                || model.equals("SM-N9500")
                || model.equals("SM-N9508")
                || model.equals("SM-N950F")
                || model.equals("SM-N950N")
                || model.equals("SM-N950U")
                || model.equals("SM-N950U1")
                || model.equals("SM-N950W")
                || model.equals("SM-N950XN")))) {
            return "Galaxy Note8";
        }
        if ((codename != null && (codename.equals("SC-01L")
                || codename.equals("SCV40")
                || codename.equals("crownlte")
                || codename.equals("crownlteks")
                || codename.equals("crownqltechn")
                || codename.equals("crownqltecs")
                || codename.equals("crownqltesq")
                || codename.equals("crownqlteue"))) || (model != null && (model.equals("SC-01L")
                || model.equals("SCV40")
                || model.equals("SM-N9600")
                || model.equals("SM-N960F")
                || model.equals("SM-N960N")
                || model.equals("SM-N960U")
                || model.equals("SM-N960U1")
                || model.equals("SM-N960W")))) {
            return "Galaxy Note9";
        }
        if ((codename != null && (codename.equals("SC-03L")
                || codename.equals("SCV41")
                || codename.equals("beyond1")
                || codename.equals("beyond1q"))) || (model != null && (model.equals("SC-03L")
                || model.equals("SCV41")
                || model.equals("SM-G9730")
                || model.equals("SM-G9738")
                || model.equals("SM-G973F")
                || model.equals("SM-G973N")
                || model.equals("SM-G973U")
                || model.equals("SM-G973U1")
                || model.equals("SM-G973W")))) {
            return "Galaxy S10";
        }
        if ((codename != null && (codename.equals("SC-04L")
                || codename.equals("SCV42")
                || codename.equals("beyond2")
                || codename.equals("beyond2q"))) || (model != null && (model.equals("SC-04L")
                || model.equals("SCV42")
                || model.equals("SM-G9750")
                || model.equals("SM-G9758")
                || model.equals("SM-G975F")
                || model.equals("SM-G975N")
                || model.equals("SM-G975U")
                || model.equals("SM-G975U1")
                || model.equals("SM-G975W")))) {
            return "Galaxy S10+";
        }
        if ((codename != null && (codename.equals("beyond0") || codename.equals("beyond0q"))) || (model != null
                && (model.equals("SM-G9700")
                || model.equals("SM-G9708")
                || model.equals("SM-G970F")
                || model.equals("SM-G970N")
                || model.equals("SM-G970U")
                || model.equals("SM-G970U1")
                || model.equals("SM-G970W")))) {
            return "Galaxy S10e";
        }
        if ((codename != null && (codename.equals("SC-04F")
                || codename.equals("SCL23")
                || codename.equals("k3g")
                || codename.equals("klte")
                || codename.equals("klteMetroPCS")
                || codename.equals("klteacg")
                || codename.equals("klteaio")
                || codename.equals("klteatt")
                || codename.equals("kltecan")
                || codename.equals("klteduoszn")
                || codename.equals("kltektt")
                || codename.equals("kltelgt")
                || codename.equals("kltelra")
                || codename.equals("klteskt")
                || codename.equals("kltespr")
                || codename.equals("kltetfnvzw")
                || codename.equals("kltetmo")
                || codename.equals("klteusc")
                || codename.equals("kltevzw")
                || codename.equals("kwifi")
                || codename.equals("lentisltektt")
                || codename.equals("lentisltelgt")
                || codename.equals("lentislteskt"))) || (model != null && (model.equals("SAMSUNG-SM-G900A")
                || model.equals("SAMSUNG-SM-G900AZ")
                || model.equals("SC-04F")
                || model.equals("SCL23")
                || model.equals("SM-G9006W")
                || model.equals("SM-G9008W")
                || model.equals("SM-G9009W")
                || model.equals("SM-G900F")
                || model.equals("SM-G900FQ")
                || model.equals("SM-G900H")
                || model.equals("SM-G900I")
                || model.equals("SM-G900K")
                || model.equals("SM-G900L")
                || model.equals("SM-G900M")
                || model.equals("SM-G900MD")
                || model.equals("SM-G900P")
                || model.equals("SM-G900R4")
                || model.equals("SM-G900R6")
                || model.equals("SM-G900R7")
                || model.equals("SM-G900S")
                || model.equals("SM-G900T")
                || model.equals("SM-G900T1")
                || model.equals("SM-G900T3")
                || model.equals("SM-G900T4")
                || model.equals("SM-G900V")
                || model.equals("SM-G900W8")
                || model.equals("SM-G900X")
                || model.equals("SM-G906K")
                || model.equals("SM-G906L")
                || model.equals("SM-G906S")
                || model.equals("SM-S903VL")))) {
            return "Galaxy S5";
        }
        if ((codename != null && (codename.equals("s5neolte") || codename.equals("s5neoltecan"))) || (model != null && (
                model.equals("SM-G903F")
                        || model.equals("SM-G903M")
                        || model.equals("SM-G903W")))) {
            return "Galaxy S5 Neo";
        }
        if ((codename != null && (codename.equals("SC-05G")
                || codename.equals("zeroflte")
                || codename.equals("zeroflteacg")
                || codename.equals("zeroflteaio")
                || codename.equals("zeroflteatt")
                || codename.equals("zerofltebmc")
                || codename.equals("zerofltechn")
                || codename.equals("zerofltectc")
                || codename.equals("zerofltektt")
                || codename.equals("zerofltelgt")
                || codename.equals("zerofltelra")
                || codename.equals("zerofltemtr")
                || codename.equals("zeroflteskt")
                || codename.equals("zerofltespr")
                || codename.equals("zerofltetfnvzw")
                || codename.equals("zerofltetmo")
                || codename.equals("zeroflteusc")
                || codename.equals("zerofltevzw"))) || (model != null && (model.equals("SAMSUNG-SM-G920A")
                || model.equals("SAMSUNG-SM-G920AZ")
                || model.equals("SC-05G")
                || model.equals("SM-G9200")
                || model.equals("SM-G9208")
                || model.equals("SM-G9209")
                || model.equals("SM-G920F")
                || model.equals("SM-G920I")
                || model.equals("SM-G920K")
                || model.equals("SM-G920L")
                || model.equals("SM-G920P")
                || model.equals("SM-G920R4")
                || model.equals("SM-G920R6")
                || model.equals("SM-G920R7")
                || model.equals("SM-G920S")
                || model.equals("SM-G920T")
                || model.equals("SM-G920T1")
                || model.equals("SM-G920V")
                || model.equals("SM-G920W8")
                || model.equals("SM-G920X")
                || model.equals("SM-S906L")
                || model.equals("SM-S907VL")))) {
            return "Galaxy S6";
        }
        if ((codename != null && (codename.equals("404SC")
                || codename.equals("SC-04G")
                || codename.equals("SCV31")
                || codename.equals("zerolte")
                || codename.equals("zerolteacg")
                || codename.equals("zerolteatt")
                || codename.equals("zeroltebmc")
                || codename.equals("zeroltechn")
                || codename.equals("zeroltektt")
                || codename.equals("zeroltelra")
                || codename.equals("zerolteskt")
                || codename.equals("zeroltespr")
                || codename.equals("zeroltetmo")
                || codename.equals("zerolteusc")
                || codename.equals("zeroltevzw"))) || (model != null && (model.equals("404SC")
                || model.equals("SAMSUNG-SM-G925A")
                || model.equals("SC-04G")
                || model.equals("SCV31")
                || model.equals("SM-G9250")
                || model.equals("SM-G925I")
                || model.equals("SM-G925K")
                || model.equals("SM-G925P")
                || model.equals("SM-G925R4")
                || model.equals("SM-G925R6")
                || model.equals("SM-G925R7")
                || model.equals("SM-G925S")
                || model.equals("SM-G925T")
                || model.equals("SM-G925V")
                || model.equals("SM-G925W8")
                || model.equals("SM-G925X")))) {
            return "Galaxy S6 Edge";
        }
        if ((codename != null && (codename.equals("zenlte")
                || codename.equals("zenlteatt")
                || codename.equals("zenltebmc")
                || codename.equals("zenltechn")
                || codename.equals("zenltektt")
                || codename.equals("zenltekx")
                || codename.equals("zenltelgt")
                || codename.equals("zenlteskt")
                || codename.equals("zenltespr")
                || codename.equals("zenltetmo")
                || codename.equals("zenlteusc")
                || codename.equals("zenltevzw"))) || (model != null && (model.equals("SAMSUNG-SM-G928A")
                || model.equals("SM-G9280")
                || model.equals("SM-G9287C")
                || model.equals("SM-G928C")
                || model.equals("SM-G928G")
                || model.equals("SM-G928I")
                || model.equals("SM-G928K")
                || model.equals("SM-G928L")
                || model.equals("SM-G928N0")
                || model.equals("SM-G928P")
                || model.equals("SM-G928R4")
                || model.equals("SM-G928S")
                || model.equals("SM-G928T")
                || model.equals("SM-G928V")
                || model.equals("SM-G928W8")
                || model.equals("SM-G928X")))) {
            return "Galaxy S6 Edge+";
        }
        if ((codename != null && (codename.equals("herolte") || codename.equals("heroltebmc") || codename.equals(
                "heroltektt") || codename.equals("heroltelgt") || codename.equals("herolteskt") || codename.equals(
                "heroqlteacg") || codename.equals("heroqlteaio") || codename.equals("heroqlteatt") || codename.equals(
                "heroqltecctvzw") || codename.equals("heroqltechn") || codename.equals("heroqltelra") || codename.equals(
                "heroqltemtr") || codename.equals("heroqltespr") || codename.equals("heroqltetfnvzw") || codename.equals(
                "heroqltetmo") || codename.equals("heroqlteue") || codename.equals("heroqlteusc") || codename.equals(
                "heroqltevzw"))) || (model != null && (model.equals("SAMSUNG-SM-G930A")
                || model.equals("SAMSUNG-SM-G930AZ")
                || model.equals("SM-G9300")
                || model.equals("SM-G9308")
                || model.equals("SM-G930F")
                || model.equals("SM-G930K")
                || model.equals("SM-G930L")
                || model.equals("SM-G930P")
                || model.equals("SM-G930R4")
                || model.equals("SM-G930R6")
                || model.equals("SM-G930R7")
                || model.equals("SM-G930S")
                || model.equals("SM-G930T")
                || model.equals("SM-G930T1")
                || model.equals("SM-G930U")
                || model.equals("SM-G930V")
                || model.equals("SM-G930VC")
                || model.equals("SM-G930VL")
                || model.equals("SM-G930W8")
                || model.equals("SM-G930X")))) {
            return "Galaxy S7";
        }
        if ((codename != null && (codename.equals("SC-02H")
                || codename.equals("SCV33")
                || codename.equals("hero2lte")
                || codename.equals("hero2ltebmc")
                || codename.equals("hero2ltektt")
                || codename.equals("hero2lteskt")
                || codename.equals("hero2qlteatt")
                || codename.equals("hero2qltecctvzw")
                || codename.equals("hero2qltespr")
                || codename.equals("hero2qltetmo")
                || codename.equals("hero2qlteusc")
                || codename.equals("hero2qltevzw"))) || (model != null && (model.equals("SAMSUNG-SM-G935A")
                || model.equals("SC-02H")
                || model.equals("SCV33")
                || model.equals("SM-G935K")
                || model.equals("SM-G935P")
                || model.equals("SM-G935R4")
                || model.equals("SM-G935S")
                || model.equals("SM-G935T")
                || model.equals("SM-G935V")
                || model.equals("SM-G935VC")
                || model.equals("SM-G935W8")
                || model.equals("SM-G935X")))) {
            return "Galaxy S7 Edge";
        }
        if ((codename != null && (codename.equals("SC-02J")
                || codename.equals("SCV36")
                || codename.equals("dreamlte")
                || codename.equals("dreamlteks")
                || codename.equals("dreamqltecan")
                || codename.equals("dreamqltechn")
                || codename.equals("dreamqltecmcc")
                || codename.equals("dreamqltesq")
                || codename.equals("dreamqlteue"))) || (model != null && (model.equals("SC-02J")
                || model.equals("SCV36")
                || model.equals("SM-G9500")
                || model.equals("SM-G9508")
                || model.equals("SM-G950F")
                || model.equals("SM-G950N")
                || model.equals("SM-G950U")
                || model.equals("SM-G950U1")
                || model.equals("SM-G950W")))) {
            return "Galaxy S8";
        }
        if ((codename != null && (codename.equals("SC-03J")
                || codename.equals("SCV35")
                || codename.equals("dream2lte")
                || codename.equals("dream2lteks")
                || codename.equals("dream2qltecan")
                || codename.equals("dream2qltechn")
                || codename.equals("dream2qltesq")
                || codename.equals("dream2qlteue"))) || (model != null && (model.equals("SC-03J")
                || model.equals("SCV35")
                || model.equals("SM-G9550")
                || model.equals("SM-G955F")
                || model.equals("SM-G955N")
                || model.equals("SM-G955U")
                || model.equals("SM-G955U1")
                || model.equals("SM-G955W")))) {
            return "Galaxy S8+";
        }
        if ((codename != null && (codename.equals("SC-02K")
                || codename.equals("SCV38")
                || codename.equals("starlte")
                || codename.equals("starlteks")
                || codename.equals("starqltechn")
                || codename.equals("starqltecmcc")
                || codename.equals("starqltecs")
                || codename.equals("starqltesq")
                || codename.equals("starqlteue"))) || (model != null && (model.equals("SC-02K")
                || model.equals("SCV38")
                || model.equals("SM-G9600")
                || model.equals("SM-G9608")
                || model.equals("SM-G960F")
                || model.equals("SM-G960N")
                || model.equals("SM-G960U")
                || model.equals("SM-G960U1")
                || model.equals("SM-G960W")))) {
            return "Galaxy S9";
        }
        if ((codename != null && (codename.equals("SC-03K")
                || codename.equals("SCV39")
                || codename.equals("star2lte")
                || codename.equals("star2lteks")
                || codename.equals("star2qltechn")
                || codename.equals("star2qltecs")
                || codename.equals("star2qltesq")
                || codename.equals("star2qlteue"))) || (model != null && (model.equals("SC-03K")
                || model.equals("SCV39")
                || model.equals("SM-G9650")
                || model.equals("SM-G965F")
                || model.equals("SM-G965N")
                || model.equals("SM-G965U")
                || model.equals("SM-G965U1")
                || model.equals("SM-G965W")))) {
            return "Galaxy S9+";
        }
        // ----------------------------------------------------------------------------
        // Sony
        if ((codename != null && (codename.equals("802SO")
                || codename.equals("J8110")
                || codename.equals("J8170")
                || codename.equals("J9110")
                || codename.equals("SO-03L")
                || codename.equals("SOV40"))) || (model != null && (model.equals("802SO")
                || model.equals("J8110")
                || model.equals("J8170")
                || model.equals("J9110")
                || model.equals("SO-03L")
                || model.equals("SOV40")))) {
            return "Xperia 1";
        }
        if ((codename != null && (codename.equals("I3113")
                || codename.equals("I3123")
                || codename.equals("I4113")
                || codename.equals("I4193"))) || (model != null && (model.equals("I3113")
                || model.equals("I3123")
                || model.equals("I4113")
                || model.equals("I4193")))) {
            return "Xperia 10";
        }
        if ((codename != null && (codename.equals("I3213")
                || codename.equals("I3223")
                || codename.equals("I4213")
                || codename.equals("I4293"))) || (model != null && (model.equals("I3213")
                || model.equals("I3223")
                || model.equals("I4213")
                || model.equals("I4293")))) {
            return "Xperia 10 Plus";
        }
        if ((codename != null && (codename.equals("702SO")
                || codename.equals("H8216")
                || codename.equals("H8266")
                || codename.equals("H8276")
                || codename.equals("H8296")
                || codename.equals("SO-03K")
                || codename.equals("SOV37"))) || (model != null && (model.equals("702SO")
                || model.equals("H8216")
                || model.equals("H8266")
                || model.equals("H8276")
                || model.equals("H8296")
                || model.equals("SO-03K")
                || model.equals("SOV37")))) {
            return "Xperia XZ2";
        }
        if ((codename != null && (codename.equals("H8116")
                || codename.equals("H8166")
                || codename.equals("SO-04K")
                || codename.equals("SOV38"))) || (model != null && (model.equals("H8116")
                || model.equals("H8166")
                || model.equals("SO-04K")
                || model.equals("SOV38")))) {
            return "Xperia XZ2 Premium";
        }
        if ((codename != null && (codename.equals("801SO")
                || codename.equals("H8416")
                || codename.equals("H9436")
                || codename.equals("H9493")
                || codename.equals("SO-01L")
                || codename.equals("SOV39"))) || (model != null && (model.equals("801SO")
                || model.equals("H8416")
                || model.equals("H9436")
                || model.equals("H9493")
                || model.equals("SO-01L")
                || model.equals("SOV39")))) {
            return "Xperia XZ3";
        }
        return fallback;
    }

    /**
     * Get the {@link DeviceInfo} for the current device. Do not run on the UI thread, as this may
     * download JSON to retrieve the {@link DeviceInfo}. JSON is only downloaded once and then
     * stored to {@link SharedPreferences}.
     *
     * @param context the application context.
     * @return {@link DeviceInfo} for the current device.
     */
    @WorkerThread public static DeviceInfo getDeviceInfo(Context context) {
        return getDeviceInfo(context.getApplicationContext(), Build.DEVICE, Build.MODEL);
    }

    /**
     * Get the {@link DeviceInfo} for the current device. Do not run on the UI thread, as this may
     * download JSON to retrieve the {@link DeviceInfo}. JSON is only downloaded once and then
     * stored to {@link SharedPreferences}.
     *
     * @param context the application context.
     * @param codename the codename of the device
     * @return {@link DeviceInfo} for the current device.
     */
    @WorkerThread public static DeviceInfo getDeviceInfo(Context context, String codename) {
        return getDeviceInfo(context, codename, null);
    }

    /**
     * Get the {@link DeviceInfo} for the current device. Do not run on the UI thread, as this may
     * download JSON to retrieve the {@link DeviceInfo}. JSON is only downloaded once and then
     * stored to {@link SharedPreferences}.
     *
     * @param context the application context.
     * @param codename the codename of the device
     * @param model the model of the device
     * @return {@link DeviceInfo} for the current device.
     */
    @WorkerThread public static DeviceInfo getDeviceInfo(Context context, String codename, String model) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String key = String.format("%s:%s", codename, model);
        String savedJson = prefs.getString(key, null);
        if (savedJson != null) {
            try {
                return new DeviceInfo(new JSONObject(savedJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // check if we have an internet connection
        int ret = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
        boolean isConnectedToNetwork = false;
        if (ret == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                isConnectedToNetwork = true;
            }
        } else {
            // assume we are connected.
            isConnectedToNetwork = true;
        }

        if (isConnectedToNetwork) {
            try {
                // Get the device name from the generated JSON files created from Google's device list.
                String url = String.format(DEVICE_JSON_URL, codename.toLowerCase(Locale.ENGLISH));
                String jsonString = downloadJson(url);
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0, len = jsonArray.length(); i < len; i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    DeviceInfo info = new DeviceInfo(json);
                    if ((codename.equalsIgnoreCase(info.codename) && model == null)
                            || codename.equalsIgnoreCase(info.codename) && model.equalsIgnoreCase(info.model)) {
                        // Save to SharedPreferences so we don't need to make another request.
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(key, json.toString());
                        editor.apply();
                        return info;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (codename.equals(Build.DEVICE) && Build.MODEL.equals(model)) {
            return new DeviceInfo(Build.MANUFACTURER, getDeviceName(), codename, model); // current device
        }

        return new DeviceInfo(null, null, codename, model); // unknown device
    }

    /**
     * <p>Capitalizes getAllProcesses the whitespace separated words in a String. Only the first
     * letter of each word is changed.</p>
     *
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     *
     * @param str the String to capitalize
     * @return capitalized The capitalized String
     */
    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    /** Download URL to String */
    @WorkerThread private static String downloadJson(String myurl) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static final class Request {

        final Context context;
        final Handler handler;
        String codename;
        String model;

        private Request(Context ctx) {
            context = ctx;
            handler = new Handler(ctx.getMainLooper());
        }

        /**
         * Set the device codename to query. You should also set the model.
         *
         * @param codename the value of the system property "ro.product.device"
         * @return This Request object to allow for chaining of calls to set methods.
         * @see Build#DEVICE
         */
        public Request setCodename(String codename) {
            this.codename = codename;
            return this;
        }

        /**
         * Set the device model to query. You should also set the codename.
         *
         * @param model the value of the system property "ro.product.model"
         * @return This Request object to allow for chaining of calls to set methods.
         * @see Build#MODEL
         */
        public Request setModel(String model) {
            this.model = model;
            return this;
        }

        /**
         * Download information about the device. This saves the results in shared-preferences so
         * future requests will not need a network connection.
         *
         * @param callback the callback to retrieve the {@link DeviceName.DeviceInfo}
         */
        public void request(Callback callback) {
            if (codename == null && model == null) {
                codename = Build.DEVICE;
                model = Build.MODEL;
            }
            GetDeviceRunnable runnable = new GetDeviceRunnable(callback);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(runnable).start();
            } else {
                runnable.run(); // already running in background thread.
            }
        }

        private final class GetDeviceRunnable implements Runnable {

            final Callback callback;
            DeviceInfo deviceInfo;
            Exception error;

            public GetDeviceRunnable(Callback callback) {
                this.callback = callback;
            }

            @Override public void run() {
                try {
                    deviceInfo = getDeviceInfo(context, codename, model);
                } catch (Exception e) {
                    error = e;
                }
                handler.post(new Runnable() {

                    @Override public void run() {
                        callback.onFinished(deviceInfo, error);
                    }
                });
            }
        }
    }

    /**
     * Callback which is invoked when the {@link DeviceName.DeviceInfo} is finished loading.
     */
    public interface Callback {

        /**
         * Callback to get the device info. This is run on the UI thread.
         *
         * @param info the requested {@link DeviceName.DeviceInfo}
         * @param error {@code null} if nothing went wrong.
         */
        void onFinished(DeviceInfo info, Exception error);
    }

    /**
     * Device information based on
     * <a href="https://support.google.com/googleplay/answer/1727131">Google's maintained list</a>.
     */
    public static final class DeviceInfo {

        /** Retail branding */
        public final String manufacturer;

        /** Marketing name */
        public final String marketName;

        /** the value of the system property "ro.product.device" */
        public final String codename;

        /** the value of the system property "ro.product.model" */
        public final String model;

        public DeviceInfo(String manufacturer, String marketName, String codename, String model) {
            this.manufacturer = manufacturer;
            this.marketName = marketName;
            this.codename = codename;
            this.model = model;
        }

        private DeviceInfo(JSONObject jsonObject) throws JSONException {
            manufacturer = jsonObject.getString("manufacturer");
            marketName = jsonObject.getString("market_name");
            codename = jsonObject.getString("codename");
            model = jsonObject.getString("model");
        }

        /**
         * @return the consumer friendly name of the device.
         */
        public String getName() {
            if (!TextUtils.isEmpty(marketName)) {
                return marketName;
            }
            return capitalize(model);
        }
    }
}
