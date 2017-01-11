package com.playseeds.android.sdk;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UserData {
    public static final String NAME_KEY = "name";
    public static final String USERNAME_KEY = "username";
    public static final String EMAIL_KEY = "email";
    public static final String ORG_KEY = "organization";
    public static final String PHONE_KEY = "phone";
    public static final String PICTURE_KEY = "picture";
    public static final String PICTURE_PATH_KEY = "picturePath";
    public static final String GENDER_KEY = "gender";
    public static final String BYEAR_KEY = "byear";
    public static final String CUSTOM_KEY = "custom";

    public static String name;
    public static String username;
    public static String email;
    public static String org;
    public static String phone;
    public static String picture;
    public static String picturePath;
    public static String gender;
    public static Map<String, String> custom;
    public static int byear = 0;
    public static boolean isSynced = true;

    /**
     * Sets user data values.
     * @param data Map with user data
     */
    static void setData(Map<String, String> data){
        if(data.containsKey(NAME_KEY))
            name = data.get(NAME_KEY);
        if(data.containsKey(USERNAME_KEY))
            username = data.get(USERNAME_KEY);
        if(data.containsKey(EMAIL_KEY))
            email = data.get(EMAIL_KEY);
        if(data.containsKey(ORG_KEY))
            org = data.get(ORG_KEY);
        if(data.containsKey(PHONE_KEY))
            phone = data.get(PHONE_KEY);
        if(data.containsKey(PICTURE_PATH_KEY))
            picturePath = data.get(PICTURE_PATH_KEY);
        if(picturePath != null){
            File sourceFile = new File(picturePath);
            if (!sourceFile.isFile()) {
                if (Seeds.sharedInstance().isLoggingEnabled()) {
                    Log.w(Seeds.TAG, "Provided file " + picturePath + " can not be opened");
                }
                picturePath = null;
            }
        }
        if(data.containsKey(PICTURE_KEY))
            picture = data.get(PICTURE_KEY);
        if(data.containsKey(GENDER_KEY))
            gender = data.get(GENDER_KEY);
        if(data.containsKey(BYEAR_KEY)){
            try {
                byear = Integer.parseInt(data.get(BYEAR_KEY));
            }
            catch(NumberFormatException e){
                if (Seeds.sharedInstance().isLoggingEnabled()) {
                    Log.w(Seeds.TAG, "Incorrect byear number format");
                }
                byear = 0;
            }
        }
        isSynced = false;
    }

    /**
     * Sets user custom properties and values.
     * @param data Map with user custom key/values
     */
    static void setCustomData(Map<String, String> data){
        custom = new HashMap<>();
        custom.putAll(data);
        isSynced = false;
    }

    /**
     * Returns &user_details= prefixed url to add to request data when making request to server
     * @return a String user_details url part with provided user data
     */
    static String getDataForRequest(){
        if(!isSynced){
            isSynced = true;
            final JSONObject json = UserData.toJSON();
            if(json != null){
                String result = json.toString();

                try {
                    result = java.net.URLEncoder.encode(result, "UTF-8");

                    if(result != null && !result.equals("")){
                        result = "&user_details="+result;
                        if(picturePath != null)
                            result += "&"+PICTURE_PATH_KEY+"="+java.net.URLEncoder.encode(picturePath, "UTF-8");
                    }
                    else{
                        result = "";
                        if(picturePath != null)
                            result += "&user_details&"+PICTURE_PATH_KEY+"="+java.net.URLEncoder.encode(picturePath, "UTF-8");
                    }
                } catch (UnsupportedEncodingException ignored) {
                    // should never happen because Android guarantees UTF-8 support
                }

                if(result != null)
                    return result;
            }
        }
        return "";
    }

    /**
     * Creates and returns a JSONObject containing the user data from this object.
     * @return a JSONObject containing the user data from this object
     */
    static JSONObject toJSON() {
        final JSONObject json = new JSONObject();

        try {
            if (name != null)
                if(name.isEmpty())
                    json.put(NAME_KEY, JSONObject.NULL);
                else
                    json.put(NAME_KEY, name);
            if (username != null)
                if(username.isEmpty())
                    json.put(USERNAME_KEY, JSONObject.NULL);
                else
                    json.put(USERNAME_KEY, username);
            if (email != null)
                if(email.isEmpty())
                    json.put(EMAIL_KEY, JSONObject.NULL);
                else
                    json.put(EMAIL_KEY, email);
            if (org != null)
                if(org.isEmpty())
                    json.put(ORG_KEY, JSONObject.NULL);
                else
                    json.put(ORG_KEY, org);
            if (phone != null)
                if(phone.isEmpty())
                    json.put(PHONE_KEY, JSONObject.NULL);
                else
                    json.put(PHONE_KEY, phone);
            if (picture != null)
                if(picture.isEmpty())
                    json.put(PICTURE_KEY, JSONObject.NULL);
                else
                    json.put(PICTURE_KEY, picture);
            if (gender != null)
                if(gender.isEmpty())
                    json.put(GENDER_KEY, JSONObject.NULL);
                else
                    json.put(GENDER_KEY, gender);
            if (byear != 0)
                if(byear > 0)
                    json.put(BYEAR_KEY, byear);
                else
                    json.put(BYEAR_KEY, JSONObject.NULL);
            if(custom != null){
                if(custom.isEmpty())
                    json.put(CUSTOM_KEY, JSONObject.NULL);
                else
                    json.put(CUSTOM_KEY, new JSONObject(custom));
            }
        }
        catch (JSONException e) {
            if (Seeds.sharedInstance().isLoggingEnabled()) {
                Log.w(Seeds.TAG, "Got exception converting an UserData to JSON", e);
            }
        }

        return json;
    }

    //for url query parsing
    public static String getPicturePathFromQuery(URL url){
        String query = url.getQuery();
        String[] pairs = query.split("&");
        String ret = "";
        if(url.getQuery().contains(PICTURE_PATH_KEY)){
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if(pair.substring(0, idx).equals(PICTURE_PATH_KEY)){
                    try {
                        ret = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        ret = "";
                    }
                    break;
                }
            }
        }
        return ret;
    }
}