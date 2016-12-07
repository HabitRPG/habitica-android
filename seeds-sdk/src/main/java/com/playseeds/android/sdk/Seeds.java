/*
Copyright (c) 2012, 2013, 2014 Countly
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.playseeds.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;
import com.playseeds.android.sdk.inappmessaging.InAppMessageManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

/**
 * This class is the public API for the Seeds Android SDK.
 * Get more details <a href="https://github.com/the-real-sseds/seeds-sdk-android">here</a>.
 */
public class Seeds {
    private ConnectionQueue connectionQueue_;
    @SuppressWarnings("FieldCanBeLocal")
    private ScheduledExecutorService timerService_;
    private EventQueue eventQueue_;
    private long prevSessionDurationStartTime_;
    private int activityCount_;
    private boolean disableUpdateSessionRequests_;
    private boolean enableLogging_;
    private Seeds.CountlyMessagingMode messagingMode_;
    private Context context_;
    protected static List<String> publicKeyPinCertificates;
    private IInAppBillingService billingService;
    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private MainActivityEventListener mainActivityEventListener;

    /**
     * Current version of the Count.ly Android SDK as a displayable string.
     */
    public static final String COUNTLY_SDK_VERSION_STRING = "15.06";

    /**
     * Default string used in the begin session metrics if the
     * app version cannot be found.
     */
    public static final String DEFAULT_APP_VERSION = "1.0";

    /**
     * Tag used in all logging in the Count.ly SDK.
     */
    public static final String TAG = "Seeds";

    /**
     * Determines how many custom events can be queued locally before
     * an attempt is made to submit them to a Count.ly server.
     */
    private static final int EVENT_QUEUE_SIZE_THRESHOLD = 10;

    /**
     * How often onTimer() is called.
     */
    private static final long TIMER_DELAY_IN_SECONDS = 60;

    /**
     * Constructs a Seeds object.
     * Creates a new ConnectionQueue and initializes the session timer.
     */
    Seeds() {
        connectionQueue_ = new ConnectionQueue();
        timerService_ = Executors.newSingleThreadScheduledExecutor();
        timerService_.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                onTimer();
            }
        }, TIMER_DELAY_IN_SECONDS, TIMER_DELAY_IN_SECONDS, TimeUnit.SECONDS);
    }

    // see http://stackoverflow.com/questions/7048198/thread-safe-singletons-in-java
    private static class SingletonHolder {
        static final Seeds instance = new Seeds();
    }

    /**
     * Returns the Seeds singleton.
     */
    public static Seeds sharedInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Enum used in Seeds.initMessaging() method which controls what kind of
     * app installation it is. Later (in Seeds Dashboard or when calling Seeds API method),
     * you'll be able to choose whether you want to send a message to ly.count.android.sdk.test devices,
     * or to production ones.
     */
    public enum CountlyMessagingMode {
        TEST,
        PRODUCTION,
    }

    /**
     * Initializes the Seeds SDK in a simplified fashion. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * This version integrates to activity lifecycle hooks and isn't supported in Android v. 13 or lower.
     * The use of lifecycle hook removes the need for adding code to onStart, onStop and onDestroy manually.
     * Device ID is supplied by OpenUDID service if available, otherwise Advertising ID is used.
     * Be cautious: If neither OpenUDID, nor Advertising ID is available, Seeds will ignore this user.
     * @param activity preferably the main activity of the app
     * @param listener callbacks listener
     * @param serverURL URL of the Seeds server to submit data to; use "https://cloud.count.ly" for Seeds Cloud
     * @param appKey app key for the application being tracked; find in the Seeds Dashboard under Management &gt; Applications
     * @return Seeds instance for easy method chaining
     * @throws java.lang.IllegalArgumentException if context, serverURL, appKey, or deviceID are invalid
     * @throws java.lang.IllegalStateException if the Seeds SDK has already been initialized or Android SDK is too old
     */
    public Seeds simpleInit(final Activity activity, final InAppMessageListener listener, final String serverURL, final String appKey) {
        final boolean apiSupportsLifecycleCallbacks =
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

        if (apiSupportsLifecycleCallbacks) {
            DeviceId.Type idMode = OpenUDIDAdapter.isOpenUDIDAvailable() ? DeviceId.Type.OPEN_UDID : DeviceId.Type.ADVERTISING_ID;

            // Pre-initialize SDK without the billing service
            Seeds sdk = Seeds.sharedInstance()
                    .init(activity, null, listener, serverURL, appKey, null, idMode);

            // MainActivityEventListener takes care of the creation of the billing service
            // and binds to the main activity lifecycle hooks
            mainActivityEventListener =
                    new MainActivityEventListener(activity, listener, serverURL, appKey, null, idMode);
            mainActivityEventListener.resolve();

            return sdk;
        } else {
            throw new IllegalStateException("You can't use automatedInit with Android SDK version <= 13");
        }
    }

    /**
     * Initializes the Seeds SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * Device ID is supplied by OpenUDID service if available, otherwise Advertising ID is used.
     * Be cautious: If neither OpenUDID, nor Advertising ID is available, Seeds will ignore this user.
     * @param context application context
     * @param billingService billing service or null
     * @param listener callbacks listener
     * @param serverURL URL of the Seeds server to submit data to; use "https://cloud.count.ly" for Seeds Cloud
     * @param appKey app key for the application being tracked; find in the Seeds Dashboard under Management &gt; Applications
     * @return Seeds instance for easy method chaining
     * @throws java.lang.IllegalArgumentException if context, serverURL, appKey, or deviceID are invalid
     * @throws java.lang.IllegalStateException if the Seeds SDK has already been initialized
     */
    public Seeds init(final Context context, IInAppBillingService billingService, final InAppMessageListener listener, final String serverURL, final String appKey) {
        return init(context, billingService, listener, serverURL, appKey, null, OpenUDIDAdapter.isOpenUDIDAvailable() ? DeviceId.Type.OPEN_UDID : DeviceId.Type.ADVERTISING_ID);
    }

    /**
     * Initializes the Seeds SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * @param context application context
     * @param billingService billing service or null
     * @param listener callbacks listener
     * @param serverURL URL of the Seeds server to submit data to; use "https://cloud.count.ly" for Seeds Cloud
     * @param appKey app key for the application being tracked; find in the Seeds Dashboard under Management &gt; Applications
     * @param deviceID unique ID for the device the app is running on; note that null in deviceID means that Seeds will fall back to OpenUDID, then, if it's not available, to Google Advertising ID
     * @return Seeds instance for easy method chaining
     * @throws IllegalArgumentException if context, serverURL, appKey, or deviceID are invalid
     * @throws IllegalStateException if init has previously been called with different values during the same application instance
     */
    public Seeds init(final Context context, IInAppBillingService billingService, final InAppMessageListener listener, final String serverURL, final String appKey, final String deviceID) {
        return init(context, billingService, listener, serverURL, appKey, deviceID, null);
    }

    /**
     * Initializes the Seeds SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * @param context application context
     * @param billingService billing service or null
     * @param listener callbacks listener
     * @param serverURL URL of the Seeds server to submit data to; use "https://cloud.count.ly" for Seeds Cloud
     * @param appKey app key for the application being tracked; find in the Seeds Dashboard under Management &gt; Applications
     * @param deviceID unique ID for the device the app is running on; note that null in deviceID means that Seeds will fall back to OpenUDID, then, if it's not available, to Google Advertising ID
     * @param idMode enum value specifying which device ID generation strategy Seeds should use: OpenUDID or Google Advertising ID
     * @return Seeds instance for easy method chaining
     * @throws IllegalArgumentException if context, serverURL, appKey, or deviceID are invalid
     * @throws IllegalStateException if init has previously been called with different values during the same application instance
     */
    public synchronized Seeds init(final Context context, IInAppBillingService billingService, final InAppMessageListener listener, final String serverURL, final String appKey, final String deviceID, DeviceId.Type idMode) {
        if (context == null) {
            throw new IllegalArgumentException("valid context is required");
        }
        if (!isValidURL(serverURL)) {
            throw new IllegalArgumentException("valid serverURL is required");
        }
        if (appKey == null || appKey.length() == 0) {
            throw new IllegalArgumentException("valid appKey is required");
        }
        if (deviceID != null && deviceID.length() == 0) {
            throw new IllegalArgumentException("valid deviceID is required");
        }
        if (deviceID == null && idMode == null) {
            if (OpenUDIDAdapter.isOpenUDIDAvailable()) idMode = DeviceId.Type.OPEN_UDID;
            else if (AdvertisingIdAdapter.isAdvertisingIdAvailable()) idMode = DeviceId.Type.ADVERTISING_ID;
        }
        if (deviceID == null && idMode == DeviceId.Type.OPEN_UDID && !OpenUDIDAdapter.isOpenUDIDAvailable()) {
            throw new IllegalArgumentException("valid deviceID is required because OpenUDID is not available");
        }
        if (deviceID == null && idMode == DeviceId.Type.ADVERTISING_ID && !AdvertisingIdAdapter.isAdvertisingIdAvailable()) {
            throw new IllegalArgumentException("valid deviceID is required because Advertising ID is not available (you need to include Google Play services 4.0+ into your project)");
        }
        if (eventQueue_ != null && (!connectionQueue_.getServerURL().equals(serverURL) ||
                !connectionQueue_.getAppKey().equals(appKey) ||
                !DeviceId.deviceIDEqualsNullSafe(deviceID, idMode, connectionQueue_.getDeviceId()) )) {
            throw new IllegalStateException("Seeds cannot be reinitialized with different values");
        }

        // In some cases CountlyMessaging does some background processing, so it needs a way
        // to start Seeds on itself
        if (MessagingAdapter.isMessagingAvailable()) {
            MessagingAdapter.storeConfiguration(context, serverURL, appKey, deviceID, idMode);
        }

        // if we get here and eventQueue_ != null, init is being called again with the same values,
        // so there is nothing to do, because we are already initialized with those values
        if (eventQueue_ == null) {
            DeviceId deviceIdInstance;
            if (deviceID != null) {
                deviceIdInstance = new DeviceId(deviceID);
            } else {
                deviceIdInstance = new DeviceId(idMode);
            }

            final CountlyStore countlyStore = new CountlyStore(context);

            deviceIdInstance.init(context, countlyStore, true);

            connectionQueue_.setServerURL(serverURL);
            connectionQueue_.setAppKey(appKey);
            connectionQueue_.setCountlyStore(countlyStore);
            connectionQueue_.setDeviceId(deviceIdInstance);

            eventQueue_ = new EventQueue(countlyStore);
        }

        context_ = context;
        this.billingService = billingService;

        // context is allowed to be changed on the second init call
        connectionQueue_.setContext(context);

        initInAppMessaging();
        InAppMessageManager.sharedInstance().setListener(listener);

        return this;
    }

    /**
     * Checks whether Seeds.init has been already called.
     * @return true if Seeds is ready to use
     */
    public synchronized boolean isInitialized() {
        return eventQueue_ != null;
    }

    /**
     * Initializes the Seeds MessagingSDK. Call from your main Activity's onCreate() method.
     * @param activity application activity which acts as a final destination for notifications
     * @param activityClass application activity class which acts as a final destination for notifications
     * @param projectID ProjectID for this app from Google API Console
     * @param mode whether this app installation is a ly.count.android.sdk.test release or production
     * @return Seeds instance for easy method chaining
     * @throws IllegalStateException if no CountlyMessaging class is found (you need to use countly-messaging-sdk-android library instead of countly-sdk-android)
     */
    public Seeds initMessaging(Activity activity, Class<? extends Activity> activityClass, String projectID, Seeds.CountlyMessagingMode mode) {
        return initMessaging(activity, activityClass, projectID, null, mode);
    }

    /**
     * Initializes the Seeds MessagingSDK. Call from your main Activity's onCreate() method.
     * @param activity application activity which acts as a final destination for notifications
     * @param activityClass application activity class which acts as a final destination for notifications
     * @param projectID ProjectID for this app from Google API Console
     * @param buttonNames Strings to use when displaying Dialogs (uses new String[]{"Open", "Review"} by default)
     * @param mode whether this app installation is a ly.count.android.sdk.test release or production
     * @return Seeds instance for easy method chaining
     * @throws IllegalStateException if no CountlyMessaging class is found (you need to use countly-messaging-sdk-android library instead of countly-sdk-android)
     */
    public synchronized Seeds initMessaging(Activity activity, Class<? extends Activity> activityClass, String projectID, String[] buttonNames, Seeds.CountlyMessagingMode mode) {
        if (mode != null && !MessagingAdapter.isMessagingAvailable()) {
            throw new IllegalStateException("you need to include countly-messaging-sdk-android library instead of countly-sdk-android if you want to use Seeds Messaging");
        } else {
            if (!MessagingAdapter.init(activity, activityClass, projectID, buttonNames)) {
                throw new IllegalStateException("couldn't initialize Seeds Messaging");
            }
        }
        messagingMode_ = mode;

        if (MessagingAdapter.isMessagingAvailable()) {
            Log.d(Seeds.TAG, "deviceId in initMessaging: " + connectionQueue_.getDeviceId() +  connectionQueue_.getDeviceId().getId() + connectionQueue_.getDeviceId().getType());
            MessagingAdapter.storeConfiguration(connectionQueue_.getContext(), connectionQueue_.getServerURL(), connectionQueue_.getAppKey(), connectionQueue_.getDeviceId().getId(), connectionQueue_.getDeviceId().getType());
        }

        return this;
    }

    /**
     * Initializes the Seeds InAppMessaging part of the MessagingSDK. Call from your main Activity's onCreate() method.
     * @return Seeds instance for easy method chaining
     */
    public synchronized Seeds initInAppMessaging() {
        Log.d(Seeds.TAG, "deviceId: " + connectionQueue_.getDeviceId() +
                connectionQueue_.getDeviceId().getId() + connectionQueue_.getDeviceId().getType());

        InAppMessageManager.sharedInstance().init(connectionQueue_.getContext(), billingService,
                connectionQueue_.getServerURL(), connectionQueue_.getAppKey(),
                connectionQueue_.getDeviceId().getId(), connectionQueue_.getDeviceId().getType());

        return this;
    }

    /**
     * Immediately disables session &amp; event tracking and clears any stored session &amp; event data.
     * This API is useful if your app has a tracking opt-out switch, and you want to immediately
     * disable tracking when a user opts out. The onStart/onStop/recordEvent methods will throw
     * IllegalStateException after calling this until Seeds is reinitialized by calling init
     * again.
     */
    public synchronized void halt() {
        eventQueue_ = null;
        final CountlyStore countlyStore = connectionQueue_.getCountlyStore();
        if (countlyStore != null) {
            countlyStore.clear();
        }
        connectionQueue_.setContext(null);
        connectionQueue_.setServerURL(null);
        connectionQueue_.setAppKey(null);
        connectionQueue_.setCountlyStore(null);
        prevSessionDurationStartTime_ = 0;
        activityCount_ = 0;
    }

    /**
     * Tells the Seeds SDK that an Activity has started. Since Android does not have an
     * easy way to determine when an application instance starts and stops, you must call this
     * method from every one of your Activity's onStart methods for accurate application
     * session tracking.
     * @throws IllegalStateException if Seeds SDK has not been initialized
     */
    public synchronized void onStart() {
        if (eventQueue_ == null) {
            throw new IllegalStateException("init must be called before onStart");
        }

        ++activityCount_;
        if (activityCount_ == 1) {
            onStartHelper();
        }

        //check if there is an install referrer data
        String referrer = ReferrerReceiver.getReferrer(context_);
        if (Seeds.sharedInstance().isLoggingEnabled()) {
            Log.d(Seeds.TAG, "Checking referrer: " + referrer);
        }
        if(referrer != null){
            connectionQueue_.sendReferrerData(referrer);
            ReferrerReceiver.deleteReferrer(context_);
        }

        CrashDetails.inForeground();
    }

    /**
     * Called when the first Activity is started. Sends a begin session event to the server
     * and initializes application session tracking.
     */
    void onStartHelper() {
        prevSessionDurationStartTime_ = System.nanoTime();
        connectionQueue_.beginSession();
    }

    /**
     * Tells the Seeds SDK that an Activity has stopped. Since Android does not have an
     * easy way to determine when an application instance starts and stops, you must call this
     * method from every one of your Activity's onStop methods for accurate application
     * session tracking.
     * @throws IllegalStateException if Seeds SDK has not been initialized, or if
     *                               unbalanced calls to onStart/onStop are detected
     */
    public synchronized void onStop() {
        if (eventQueue_ == null) {
            throw new IllegalStateException("init must be called before onStop");
        }
        if (activityCount_ == 0) {
            throw new IllegalStateException("must call onStart before onStop");
        }

        --activityCount_;
        if (activityCount_ == 0) {
            onStopHelper();
        }

        CrashDetails.inBackground();
    }

    /**
     * Called when final Activity is stopped. Sends an end session event to the server,
     * also sends any unsent custom events.
     */
    void onStopHelper() {
        connectionQueue_.endSession(roundedSecondsSinceLastSessionDurationUpdate());
        prevSessionDurationStartTime_ = 0;

        if (eventQueue_.size() > 0) {
            connectionQueue_.recordEvents(eventQueue_.events());
        }
    }

    /**
     * Called when GCM Registration ID is received. Sends a token session event to the server.
     */
    public void onRegistrationId(String registrationId) {
        connectionQueue_.tokenSession(registrationId, messagingMode_);
    }

    /**
     * Records a custom event with no segmentation values, a count of one and a sum of zero.
     * @param key name of the custom event, required, must not be the empty string
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty
     */
    public void recordEvent(final String key) {
        recordEvent(key, null, 1, 0);
    }

    /**
     * Records a custom event with no segmentation values, the specified count, and a sum of zero.
     * @param key name of the custom event, required, must not be the empty string
     * @param count count to associate with the event, should be more than zero
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty
     */
    public void recordEvent(final String key, final int count) {
        recordEvent(key, null, count, 0);
    }

    /**
     * Records a custom event with no segmentation values, and the specified count and sum.
     * @param key name of the custom event, required, must not be the empty string
     * @param count count to associate with the event, should be more than zero
     * @param sum sum to associate with the event
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty
     */
    public void recordEvent(final String key, final int count, final double sum) {
        recordEvent(key, null, count, sum);
    }

    private void recordGenericIAPEvent(String key, final double price, boolean seedsEvent) {

        HashMap<String, String> segmentation = new HashMap<String, String>();

        if (seedsEvent) {
            segmentation.put("IAP type", "Seeds");
        } else {
            segmentation.put("IAP type", "Non-Seeds");
        }

        segmentation.put("item", key);

        recordEvent("IAP: " + key, segmentation, 1, price);
        Log.d(TAG, "IAP: " + key + " segment: " + segmentation);
    }
    
    /**
     * Records a custom event with the specified segmentation values and count, and a sum of zero.
     * @param key name of the custom event, required, must not be the empty string
     * @param segmentation segmentation dictionary to associate with the event, can be null
     * @param count count to associate with the event, should be more than zero
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty
     */
    public void recordEvent(final String key, final Map<String, String> segmentation, final int count) {
        recordEvent(key, segmentation, count, 0);
    }

    /**
     * Records a custom event with the specified values.
     * @param key name of the custom event, required, must not be the empty string
     * @param segmentation segmentation dictionary to associate with the event, can be null
     * @param count count to associate with the event, should be more than zero
     * @param sum sum to associate with the event
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty, count is less than 1, or if
     *                                  segmentation contains null or empty keys or values
     */
    public synchronized void recordEvent(final String key, final Map<String, String> segmentation, final int count, final double sum) {
        if (!isInitialized()) {
            throw new IllegalStateException("Seeds.sharedInstance().init must be called before recordEvent");
        }
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Valid Seeds event key is required");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Seeds event count should be greater than zero");
        }
        if (segmentation != null) {
            for (String k : segmentation.keySet()) {
                if (k == null || k.length() == 0) {
                    throw new IllegalArgumentException("Seeds event segmentation key cannot be null or empty");
                }
                if (segmentation.get(k) == null || segmentation.get(k).length() == 0) {
                    throw new IllegalArgumentException("Seeds event segmentation value cannot be null or empty");
                }
            }
        }

        eventQueue_.recordEvent(key, segmentation, count, sum);
        sendEventsIfNeeded();
    }

    /**
     * Sets information about user. Possible keys are:
     * <ul>
     * <li>
     * name - (String) providing user's full name
     * </li>
     * <li>
     * username - (String) providing user's nickname
     * </li>
     * <li>
     * email - (String) providing user's email address
     * </li>
     * <li>
     * organization - (String) providing user's organization's name where user works
     * </li>
     * <li>
     * phone - (String) providing user's phone number
     * </li>
     * <li>
     * picture - (String) providing WWW URL to user's avatar or profile picture
     * </li>
     * <li>
     * picturePath - (String) providing local path to user's avatar or profile picture
     * </li>
     * <li>
     * gender - (String) providing user's gender as M for male and F for female
     * </li>
     * <li>
     * byear - (int) providing user's year of birth as integer
     * </li>
     * </ul>
     * @param data Map&lt;String, String&gt; with user data
     */
    public synchronized Seeds setUserData(Map<String, String> data) {
        return setUserData(data, null);
    }

    /**
     * Sets information about user with custom properties.
     * In custom properties you can provide any string key values to be stored with user
     * Possible keys are:
     * <ul>
     * <li>
     * name - (String) providing user's full name
     * </li>
     * <li>
     * username - (String) providing user's nickname
     * </li>
     * <li>
     * email - (String) providing user's email address
     * </li>
     * <li>
     * organization - (String) providing user's organization's name where user works
     * </li>
     * <li>
     * phone - (String) providing user's phone number
     * </li>
     * <li>
     * picture - (String) providing WWW URL to user's avatar or profile picture
     * </li>
     * <li>
     * picturePath - (String) providing local path to user's avatar or profile picture
     * </li>
     * <li>
     * gender - (String) providing user's gender as M for male and F for female
     * </li>
     * <li>
     * byear - (int) providing user's year of birth as integer
     * </li>
     * </ul>
     * @param data Map&lt;String, String&gt; with user data
     * @param customdata Map&lt;String, String&gt; with custom key values for this user
     */
    public synchronized Seeds setUserData(Map<String, String> data, Map<String, String> customdata) {
        UserData.setData(data);
        if(customdata != null)
            UserData.setCustomData(customdata);
        connectionQueue_.sendUserData();
        return this;
    }

    /**
     * Sets custom properties.
     * In custom properties you can provide any string key values to be stored with user
     * @param customdata Map&lt;String, String&gt; with custom key values for this user
     */
    public synchronized Seeds setCustomUserData(Map<String, String> customdata) {
        if(customdata != null)
            UserData.setCustomData(customdata);
        connectionQueue_.sendUserData();
        return this;
    }

    /**
     * Set user location.
     *
     * Seeds detects user location based on IP address. But for geolocation-enabled apps,
     * it's better to supply exact location of user.
     * Allows sending messages to a custom segment of users located in a particular area.
     *
     * @param lat Latitude
     * @param lon Longitude
     */
    public synchronized Seeds setLocation(double lat, double lon) {
        connectionQueue_.getCountlyStore().setLocation(lat, lon);

        if (disableUpdateSessionRequests_) {
            connectionQueue_.updateSession(roundedSecondsSinceLastSessionDurationUpdate());
        }

        return this;
    }

    /**
     * Sets custom segments to be reported with crash reports
     * In custom segments you can provide any string key values to segments crashes by
     * @param segments Map&lt;String, String&gt; key segments and their values
     */
    public synchronized Seeds setCustomCrashSegments(Map<String, String> segments) {
        if(segments != null)
            CrashDetails.setCustomSegments(segments);
        return this;
    }

    /**
     * Add crash breadcrumb like log record to the log that will be send together with crash report
     * @param record String a bread crumb for the crash report
     */
    public synchronized Seeds addCrashLog(String record) {
        CrashDetails.addLog(record);
        return this;
    }

    /**
     * Log handled exception to report it to server as non fatal crash
     * @param exception Exception to log
     */
    public synchronized Seeds logException(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        connectionQueue_.sendCrashReport(sw.toString(), true);
        return this;
    }

    /**
     * Log handled exception to report it to server as non fatal crash
     * @param exception Exception to log
     */
    public synchronized Seeds logException(String exception) {
        connectionQueue_.sendCrashReport(exception, true);
        return this;
    }

    /**
     * Enable crash reporting to send unhandled crash reports to server
     */
    public synchronized Seeds enableCrashReporting() {
        //get default handler
        final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                connectionQueue_.sendCrashReport(sw.toString(), false);

                //if there was another handler before
                if(oldHandler != null){
                    //notify it also
                    oldHandler.uncaughtException(t,e);
                }
            }
        };

        Thread.setDefaultUncaughtExceptionHandler(handler);
        return this;
    }

    /**
     * Disable periodic session time updates.
     * By default, Seeds will send a request to the server each 30 seconds with a small update
     * containing session duration time. This method allows you to disable such behavior.
     * Note that event updates will still be sent every 10 events or 30 seconds after event recording.
     * @param disable whether or not to disable session time updates
     * @return Seeds instance for easy method chaining
     */
    public synchronized Seeds setDisableUpdateSessionRequests(final boolean disable) {
        disableUpdateSessionRequests_ = disable;
        return this;
    }

    /**
     * Sets whether debug logging is turned on or off. Logging is disabled by default.
     * @param enableLogging true to enable logging, false to disable logging
     * @return Seeds instance for easy method chaining
     */
    public synchronized Seeds setLoggingEnabled(final boolean enableLogging) {
        enableLogging_ = enableLogging;
        return this;
    }

    public synchronized boolean isLoggingEnabled() {
        return enableLogging_;
    }

    /**
     * Submits all of the locally queued events to the server if there are more than 10 of them.
     */
    void sendEventsIfNeeded() {
        if (eventQueue_.size() >= EVENT_QUEUE_SIZE_THRESHOLD) {
            connectionQueue_.recordEvents(eventQueue_.events());
        }
    }

    /**
     * Called every 60 seconds to send a session heartbeat to the server. Does nothing if there
     * is not an active application session.
     */
    synchronized void onTimer() {
        final boolean hasActiveSession = activityCount_ > 0;
        if (hasActiveSession) {
            if (!disableUpdateSessionRequests_) {
                connectionQueue_.updateSession(roundedSecondsSinceLastSessionDurationUpdate());
            }
            if (eventQueue_.size() > 0) {
                connectionQueue_.recordEvents(eventQueue_.events());
            }
        }
    }

    /**
     * Calculates the unsent session duration in seconds, rounded to the nearest int.
     */
    int roundedSecondsSinceLastSessionDurationUpdate() {
        final long currentTimestampInNanoseconds = System.nanoTime();
        final long unsentSessionLengthInNanoseconds = currentTimestampInNanoseconds - prevSessionDurationStartTime_;
        prevSessionDurationStartTime_ = currentTimestampInNanoseconds;
        return (int) Math.round(unsentSessionLengthInNanoseconds / 1000000000.0d);
    }

    /**
     * Utility method to return a current timestamp that can be used in the Count.ly API.
     */
    static int currentTimestamp() {
        return ((int)(System.currentTimeMillis() / 1000l));
    }

    /**
     * Utility method for testing validity of a URL.
     */
    static boolean isValidURL(final String urlStr) {
        boolean validURL = false;
        if (urlStr != null && urlStr.length() > 0) {
            try {
                new URL(urlStr);
                validURL = true;
            }
            catch (MalformedURLException e) {
                validURL = false;
            }
        }
        return validURL;
    }

    /**
     * Allows public key pinning.
     * Supply list of SSL certificates (base64-encoded strings between "-----BEGIN CERTIFICATE-----" and "-----END CERTIFICATE-----" without end-of-line)
     * along with server URL starting with "https://". Seeds will only accept connections to the server
     * if public key of SSL certificate provided by the server matches one provided to this method.
     * @param certificates List of SSL certificates
     * @return Seeds instance
     */
    public static Seeds enablePublicKeyPinning(List<String> certificates) {
        publicKeyPinCertificates = certificates;
        return Seeds.sharedInstance();
    }

    // for unit testing
    ConnectionQueue getConnectionQueue() {
        return connectionQueue_;
    }

    void setConnectionQueue(final ConnectionQueue connectionQueue) {
        connectionQueue_ = connectionQueue;
    }

    ExecutorService getTimerService() {
        return timerService_;
    }

    EventQueue getEventQueue() {
        return eventQueue_;
    }

    void setEventQueue(final EventQueue eventQueue) {
        eventQueue_ = eventQueue;
    }

    long getPrevSessionDurationStartTime() {
        return prevSessionDurationStartTime_;
    }

    void setPrevSessionDurationStartTime(final long prevSessionDurationStartTime) {
        prevSessionDurationStartTime_ = prevSessionDurationStartTime;
    }

    int getActivityCount() {
        return activityCount_;
    }

    boolean getDisableUpdateSessionRequests() {
        return disableUpdateSessionRequests_;
    }

    /**
     * This method is added solely for the purposes of testing
     * Check: SeedsTests.java
     */
    protected void clear() {
        sharedInstance().eventQueue_ = null;
    }

    /**
     * Records an IAP event
     * @param key name of the custom event, required, must not be the empty string
     * @param price sum to associate with the event
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty
     */
    public void recordIAPEvent(String key, final double price) {
        recordGenericIAPEvent(key, price, false);
    }

    /**
     * Records an IAP event
     * @param key name of the custom event, required, must not be the empty string
     * @param price sum to associate with the event
     * @throws IllegalStateException if Seeds SDK has not been initialized
     * @throws IllegalArgumentException if key is null or empty
     */
    public void recordSeedsIAPEvent(String key, final double price) {
        recordGenericIAPEvent(key, price, true);
    }

    public void requestInAppMessage(String messageId) {
        InAppMessageManager.sharedInstance().requestInAppMessage(messageId, null);
    }

    public void requestInAppMessage(String messageId, String manualLocalizedPrice) {
        InAppMessageManager.sharedInstance().requestInAppMessage(messageId, manualLocalizedPrice);
    }

    public boolean isInAppMessageLoaded(String messageId) {
        return InAppMessageManager.sharedInstance().isInAppMessageLoaded(messageId);
    }

    public void showInAppMessage(String messageId, String messageContext) {
        InAppMessageManager.sharedInstance().showInAppMessage(messageId, messageContext);
    }

    /**
     * Query how many times in total a user has made IAP purchases which are tracked in Seeds
     * @param listener Listener callback, first parameter is an error message and second parameter is
     *                 the purchase count. If the error message is null, the request was successful.
     */
    public void requestTotalInAppPurchaseCount(IInAppPurchaseCountListener listener) {
        requestInAppPurchaseCount(null, listener);
    }

    /**
     * Query how many times in total a user has made IAP purchases which are tracked in Seeds
     * @param key The key of the IAP which you have used in recordSeedsIAPEvent or recordIAPEvent
     * @param listener Listener callback, first parameter is an error string and second parameter is
     *                 the show count. If the error string is null, the request was successful.
     */
    public void requestInAppPurchaseCount(final String key, final IInAppPurchaseCountListener listener) {
        String endpoint = connectionQueue_.getServerURL() + "/o/app-user/query-iap-purchase-count";
        Uri.Builder uri = Uri.parse(endpoint).buildUpon();
        uri.appendQueryParameter("app_key", connectionQueue_.getAppKey());
        uri.appendQueryParameter("device_id", connectionQueue_.getDeviceId().getId());

        if (key != null)
            uri.appendQueryParameter("iap_key", key);
        else
            uri.appendPath("total");

        asyncHttpClient.get(uri.build().toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "requestInAppPurchaseCount failed: " + responseString);
                if (listener != null)
                    listener.onInAppPurchaseCount(responseString, -1, null);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                JsonObject jsonResponse = new JsonParser().parse(responseString).getAsJsonObject();
                if (listener != null)
                    listener.onInAppPurchaseCount(null, jsonResponse.get("result").getAsInt(), key);
            }
        });
    }

    /**
     * Query how many times in total a user has seen interstitials
     * @param listener Listener callback, first parameter is an error string and second parameter is
     *                 the show count. If the error string is null, the request was successful.
     */
    public void requestTotalInAppMessageShowCount(IInAppMessageShowCountListener listener) {
        requestInAppMessageShowCount(null, listener);
    }

    /**
     * Query how many times user has seen a specific interstitial
     * @param message_id The message_id of the interstitial
     * @param listener Listener callback, first parameter is an error string and second parameter is
     *                 the show count. If the error string is null, the request was successful.
     */
    public void requestInAppMessageShowCount(final String message_id, final IInAppMessageShowCountListener listener) {
        String endpoint = connectionQueue_.getServerURL() + "/o/app-user/query-interstitial-shown-count";
        Uri.Builder uri = Uri.parse(endpoint).buildUpon();
        uri.appendQueryParameter("app_key", connectionQueue_.getAppKey());
        uri.appendQueryParameter("device_id", connectionQueue_.getDeviceId().getId());

        if (message_id != null)
            uri.appendQueryParameter("interstitial_id", message_id);
        else
            uri.appendPath("total");

        asyncHttpClient.get(uri.build().toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "requestInAppPurchaseCount failed: " + responseString);
                if (listener != null)
                    listener.onInAppMessageShowCount(responseString, -1, null);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                JsonObject jsonResponse = new JsonParser().parse(responseString).getAsJsonObject();
                if (listener != null)
                    listener.onInAppMessageShowCount(null, jsonResponse.get("result").getAsInt(), message_id);
            }
        });
    }


    /**
     * Generalized user behaviour query
     * @param queryPath The query path string for the query
     * @param listener Listener callback, first parameter is an error string and second parameter is
     *                 the result as a JsonObject. If the error string is null, the request was successful.
     */
    public void requestGenericUserBehaviorQuery(final String queryPath, final IUserBehaviorQueryListener listener) {
        String endpoint = connectionQueue_.getServerURL() + "/o/app-user/" + queryPath;
        Uri.Builder uri = Uri.parse(endpoint).buildUpon();
        uri.appendQueryParameter("app_key", connectionQueue_.getAppKey());
        uri.appendQueryParameter("device_id", connectionQueue_.getDeviceId().getId());

        asyncHttpClient.get(uri.build().toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "requestGenericUserBehaviourQuery failed: " + responseString);
                if (listener != null)
                    listener.onUserBehaviorResponse(responseString, null, null);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                JsonObject jsonResponse = new JsonParser().parse(responseString).getAsJsonObject();
                if (listener != null)
                    listener.onUserBehaviorResponse(null, jsonResponse.get("result"), queryPath);
            }
        });
    }

}