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

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * ConnectionProcessor is a Runnable that is executed on a background
 * thread to submit session &amp; event data to a Count.ly server.
 *
 * NOTE: This class is only public to facilitate unit testing, because
 *       of this bug in dexmaker: https://code.google.com/p/dexmaker/issues/detail?id=34
 */
public class ConnectionProcessor implements Runnable {
    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 30000;
    private static final int READ_TIMEOUT_IN_MILLISECONDS = 30000;

    private final CountlyStore store_;
    private final DeviceId deviceId_;
    private final String serverURL_;
    private final SSLContext sslContext_;

    ConnectionProcessor(final String serverURL, final CountlyStore store, final DeviceId deviceId, final SSLContext sslContext) {
        serverURL_ = serverURL;
        store_ = store;
        deviceId_ = deviceId;
        sslContext_ = sslContext;

        // HTTP connection reuse which was buggy pre-froyo
        System.setProperty("http.keepAlive", "false");
    }

    public URLConnection urlConnectionForEventData(final String eventData) throws IOException {
        String urlStr = serverURL_ + "/i?";
        if(!eventData.contains("&crash="))
            urlStr += eventData;
        final URL url = new URL(urlStr);
        final HttpURLConnection conn;
        if (Seeds.publicKeyPinCertificates == null) {
            conn = (HttpURLConnection)url.openConnection();
        } else {
            HttpsURLConnection c = (HttpsURLConnection)url.openConnection();
            c.setSSLSocketFactory(sslContext_.getSocketFactory());
            conn = c;
        }
        conn.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        conn.setReadTimeout(READ_TIMEOUT_IN_MILLISECONDS);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        String picturePath = UserData.getPicturePathFromQuery(url);
        if (Seeds.sharedInstance().isLoggingEnabled()) {
            Log.d(Seeds.TAG, "Got picturePath: " + picturePath);
        }
        if(!picturePath.equals("")){
        	//Uploading files:
        	//http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
        	
        	File binaryFile = new File(picturePath);
        	conn.setDoOutput(true);
        	// Just generate some unique random value.
        	String boundary = Long.toHexString(System.currentTimeMillis());
        	// Line separator required by multipart/form-data.
        	String CRLF = "\r\n";
        	String charset = "UTF-8";
        	conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        	OutputStream output = conn.getOutputStream();
        	PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
        	// Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            FileInputStream fileInputStream = new FileInputStream(binaryFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
            fileInputStream.close();

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
        }
        else if(eventData.contains("&crash=")){
            if (Seeds.sharedInstance().isLoggingEnabled()) {
                Log.d(Seeds.TAG, "Using post because of crash");
            }
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(eventData);
            writer.flush();
            writer.close();
            os.close();
        }
        else{
        	conn.setDoOutput(false);
        }
        return conn;
    }

    @Override
    public void run() {
        while (true) {
            final String[] storedEvents = store_.connections();
            if (storedEvents == null || storedEvents.length == 0) {
                // currently no data to send, we are done for now
                break;
            }

            // get first event from collection
            if (deviceId_.getId() == null) {
                // When device ID is supplied by OpenUDID or by Google Advertising ID.
                // In some cases it might take time for them to initialize. So, just wait for it.
                if (Seeds.sharedInstance().isLoggingEnabled()) {
                    Log.i(Seeds.TAG, "No Device ID available yet, skipping request " + storedEvents[0]);
                }
                break;
            }
            final String eventData = storedEvents[0] + "&device_id=" + deviceId_.getId();

            URLConnection conn = null;
            BufferedInputStream responseStream = null;
            try {
                // initialize and open connection
                conn = urlConnectionForEventData(eventData);
                conn.connect();

                // consume response stream
                responseStream = new BufferedInputStream(conn.getInputStream());
                final ByteArrayOutputStream responseData = new ByteArrayOutputStream(256); // big enough to handle success response without reallocating
                int c;
                while ((c = responseStream.read()) != -1) {
                    responseData.write(c);
                }

                // response code has to be 2xx to be considered a success
                boolean success = true;
                if (conn instanceof HttpURLConnection) {
                    final HttpURLConnection httpConn = (HttpURLConnection) conn;
                    final int responseCode = httpConn.getResponseCode();
                    success = responseCode >= 200 && responseCode < 300;
                    if (!success && Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.w(Seeds.TAG, "HTTP error response code was " + responseCode + " from submitting event data: " + eventData);
                    }
                }

                // HTTP response code was good, check response JSON contains {"result":"Success"}
                if (success) {
                    final JSONObject responseDict = new JSONObject(responseData.toString("UTF-8"));
                    success = responseDict.optString("result").equalsIgnoreCase("success");
                    if (!success && Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.w(Seeds.TAG, "Response from Seeds server did not report success, it was: " + responseData.toString("UTF-8"));
                    }
                }

                if (success) {
                    if (Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.d(Seeds.TAG, "ok ->" + eventData);
                    }

                    // successfully submitted event data to Count.ly server, so remove
                    // this one from the stored events collection
                    store_.removeConnection(storedEvents[0]);
                }
                else {
                    // warning was logged above, stop processing, let next tick take care of retrying
                    break;
                }
            }
            catch (Exception e) {
                if (Seeds.sharedInstance().isLoggingEnabled()) {
                    Log.w(Seeds.TAG, "Got exception while trying to submit event data: " + eventData, e);
                }
                // if exception occurred, stop processing, let next tick take care of retrying
                break;
            }
            finally {
                // free connection resources
                if (responseStream != null) {
                    try { responseStream.close(); } catch (IOException ignored) {}
                }
                if (conn != null && conn instanceof HttpURLConnection) {
                    ((HttpURLConnection)conn).disconnect();
                }
            }
        }
    }

    // for unit testing
    String getServerURL() { return serverURL_; }
    CountlyStore getCountlyStore() { return store_; }
    DeviceId getDeviceId() { return deviceId_; }
}
