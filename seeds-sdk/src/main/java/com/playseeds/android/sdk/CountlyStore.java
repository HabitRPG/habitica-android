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

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class provides a persistence layer for the local event &amp; connection queues.
 *
 * The "read" methods in this class are not synchronized, because the underlying data store
 * provides thread-safe reads.  The "write" methods in this class are synchronized, because
 * 1) they often read a list of items, modify the list, and then commit it back to the underlying
 * data store, and 2) while the Seeds singleton is synchronized to ensure only a single writer
 * at a time from the public API side, the internal implementation has a background thread that
 * submits data to a Seeds server, and it writes to this store as well.
 *
 * NOTE: This class is only public to facilitate unit testing, because
 *       of this bug in dexmaker: https://code.google.com/p/dexmaker/issues/detail?id=34
 */
public class CountlyStore {
    private static final String PREFERENCES = "COUNTLY_STORE";
    private static final String DELIMITER = ":::";
    private static final String CONNECTIONS_PREFERENCE = "CONNECTIONS";
    private static final String EVENTS_PREFERENCE = "EVENTS";
    private static final String LOCATION_PREFERENCE = "LOCATION";

    private final SharedPreferences preferences_;

    /**
     * Constructs a CountlyStore object.
     * @param context used to retrieve storage meta data, must not be null.
     * @throws IllegalArgumentException if context is null
     */
    CountlyStore(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("must provide valid context");
        }
        preferences_ = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Returns an unsorted array of the current stored connections.
     */
    public String[] connections() {
        final String joinedConnStr = preferences_.getString(CONNECTIONS_PREFERENCE, "");
        return joinedConnStr.length() == 0 ? new String[0] : joinedConnStr.split(DELIMITER);
    }

    /**
     * Returns an unsorted array of the current stored event JSON strings.
     */
    public String[] events() {
        final String joinedEventsStr = preferences_.getString(EVENTS_PREFERENCE, "");
        return joinedEventsStr.length() == 0 ? new String[0] : joinedEventsStr.split(DELIMITER);
    }

    /**
     * Returns a list of the current stored events, sorted by timestamp from oldest to newest.
     */
    public List<Event> eventsList() {
        final String[] array = events();
        final List<Event> events = new ArrayList<>(array.length);
        for (String s : array) {
            try {
                final Event event = Event.fromJSON(new JSONObject(s));
                if (event != null) {
                    events.add(event);
                }
            } catch (JSONException ignored) {
                // should not happen since JSONObject is being constructed from previously stringified JSONObject
                // events -> json objects -> json strings -> storage -> json strings -> here
            }
        }
        // order the events from least to most recent
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(final Event e1, final Event e2) {
                return e1.timestamp - e2.timestamp;
            }
        });
        return events;
    }

    /**
     * Returns true if no connections are current stored, false otherwise.
     */
    public boolean isEmptyConnections() {
        return preferences_.getString(CONNECTIONS_PREFERENCE, "").length() == 0;
    }

    /**
     * Adds a connection to the local store.
     * @param str the connection to be added, ignored if null or empty
     */
    public synchronized void addConnection(final String str) {
        if (str != null && str.length() > 0) {
            final List<String> connections = new ArrayList<>(Arrays.asList(connections()));
            connections.add(str);
            preferences_.edit().putString(CONNECTIONS_PREFERENCE, join(connections, DELIMITER)).commit();
        }
    }

    /**
     * Removes a connection from the local store.
     * @param str the connection to be removed, ignored if null or empty,
     *            or if a matching connection cannot be found
     */
    public synchronized void removeConnection(final String str) {
        if (str != null && str.length() > 0) {
            final List<String> connections = new ArrayList<>(Arrays.asList(connections()));
            if (connections.remove(str)) {
                preferences_.edit().putString(CONNECTIONS_PREFERENCE, join(connections, DELIMITER)).commit();
            }
        }
    }

    /**
     * Adds a custom event to the local store.
     * @param event event to be added to the local store, must not be null
     */
    void addEvent(final Event event) {
        final List<Event> events = eventsList();
        events.add(event);
        preferences_.edit().putString(EVENTS_PREFERENCE, joinEvents(events, DELIMITER)).commit();
    }

    /**
     * Sets location of user and sends it with next request
     */
    void setLocation(final double lat, final double lon) {
        if (preferences_ != null) {
            preferences_.edit().putString(LOCATION_PREFERENCE, lat + "," + lon).commit();
        }
    }

    /**
     * Get location or empty string in case if no location is specified
     */
    String getAndRemoveLocation() {
        String location = null;
        if (preferences_ != null) {
            location = preferences_.getString(LOCATION_PREFERENCE, "");
            if (!location.equals("")) {
                preferences_.edit().remove(LOCATION_PREFERENCE).commit();
            }
        }
        return location;
    }

    /**
     * Adds a custom event to the local store.
     * @param key name of the custom event, required, must not be the empty string
     * @param segmentation segmentation values for the custom event, may be null
     * @param timestamp timestamp (seconds since 1970) in GMT when the event occurred
     * @param count count associated with the custom event, should be more than zero
     * @param sum sum associated with the custom event, if not used, pass zero.
     *            NaN and infinity values will be quietly ignored.
     */
    public synchronized void addEvent(final String key, final Map<String, String> segmentation, final int timestamp, final int count, final double sum) {
        final Event event = new Event();
        event.key = key;
        event.segmentation = segmentation;
        event.timestamp = timestamp;
        event.count = count;
        event.sum = sum;

        addEvent(event);
    }

    /**
     * Removes the specified events from the local store. Does nothing if the event collection
     * is null or empty.
     * @param eventsToRemove collection containing the events to remove from the local store
     */
    public synchronized void removeEvents(final Collection<Event> eventsToRemove) {
        if (eventsToRemove != null && eventsToRemove.size() > 0) {
            final List<Event> events = eventsList();
            if (events.removeAll(eventsToRemove)) {
                preferences_.edit().putString(EVENTS_PREFERENCE, joinEvents(events, DELIMITER)).commit();
            }
        }
    }

    /**
     * Converts a collection of Event objects to URL-encoded JSON to a string, with each
     * event JSON string delimited by the specified delimiter.
     * @param collection events to join into a delimited string
     * @param delimiter delimiter to use, should not be something that can be found in URL-encoded JSON string
     */
    static String joinEvents(final Collection<Event> collection, final String delimiter) {
        final List<String> strings = new ArrayList<>();
        for (Event e : collection) {
            strings.add(e.toJSON().toString());
        }
        return join(strings, delimiter);
    }

    /**
     * Joins all the strings in the specified collection into a single string with the specified delimiter.
     */
    static String join(final Collection<String> collection, final String delimiter) {
        final StringBuilder builder = new StringBuilder();

        int i = 0;
        for (String s : collection) {
            builder.append(s);
            if (++i < collection.size()) {
                builder.append(delimiter);
            }
        }

        return builder.toString();
    }

    /**
     * Retrieves a preference from local store.
     * @param key the preference key
     */
    public synchronized String getPreference(final String key) {
        return preferences_.getString(key, null);
    }

    /**
     * Adds a preference to local store.
     * @param key the preference key
     * @param value the preference value, supply null value to remove preference
     */
    public synchronized void setPreference(final String key, final String value) {
        if (value == null) {
            preferences_.edit().remove(key).commit();
        } else {
            preferences_.edit().putString(key, value).commit();
        }
    }

    // for unit testing
    synchronized void clear() {
        if (preferences_ != null) {
            SharedPreferences.Editor prefsEditor = preferences_.edit();
            prefsEditor.remove(EVENTS_PREFERENCE);
            prefsEditor.remove(CONNECTIONS_PREFERENCE);
            prefsEditor.commit();
        }
    }
}
