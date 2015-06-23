package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Simple callback implementation when the result is a list of tags.
 * Created by MagicMicky on 12/06/2014.
 */
public class MultiTagsCallback implements Callback<List<Tag>> {
    private static final String TAG = "MultiTag";

    @Override
    public void success(List<Tag> tags, Response response) {
        StringBuilder s=new StringBuilder();
        s.append("Success with tags : ");
        for(Tag t : tags) {
            s.append(t.getName() + ", ");
        }
        Log.d(TAG, s.toString());
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.w(TAG, "Failure ! ");
        Log.e(TAG, retrofitError.getUrl() + ":" + retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
        Log.e(TAG, "HTTP Response:" + retrofitError.getResponse().getStatus() + " - " + retrofitError.getResponse().getReason());

    }
}
