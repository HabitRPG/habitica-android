package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.HabiticaApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

// based on http://stackoverflow.com/questions/29838565/downloading-files-using-okhttp-okio-and-rxjava
public class SoundFileLoader {
    OkHttpClient client;

    public SoundFileLoader(){
        client = new OkHttpClient();
    }

    public Observable<List<SoundFile>> download(List<SoundFile> files) {
        return Observable.from(files)
                .flatMap(audioFile -> {
                    File file = new File(getFullAudioFilePath(audioFile));
                    if (file.exists()) {
                        // Important, or else the MediaPlayer can't access this file
                        file.setReadable(true, false);
                        audioFile.setFile(file);
                        return Observable.just(audioFile);
                    }

                    final Observable<SoundFile> fileObservable = Observable.create(sub -> {
                        if (sub.isUnsubscribed()) {
                            return;
                        }

                        Request request = new Request.Builder().url(audioFile.getWebUrl()).build();

                        Response response;
                        try {
                            response = client.newCall(request).execute();
                            if (!response.isSuccessful()) { throw new IOException(); }
                        } catch (IOException io) {
                            throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, audioFile));
                        }

                        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            if (!sub.isUnsubscribed()) {
                                try {
                                    BufferedSink sink = Okio.buffer(Okio.sink(file));
                                    sink.writeAll(response.body().source());
                                    sink.flush();
                                    sink.close();
                                } catch (IOException io) {
                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, audioFile));
                                }

                                file.setReadable(true, false);
                                audioFile.setFile(file);
                                sub.onNext(audioFile);
                                sub.onCompleted();
                            }
                        }
                    });
                    return fileObservable.subscribeOn(Schedulers.io());
                }, 5)
                .toList()
                .map(ArrayList::new);
    }

    private String getExternalCacheDir() {
        return HabiticaApplication.getInstance(HabiticaApplication.currentActivity).getExternalCacheDir().getPath();
    }

    public String getFullAudioFilePath(SoundFile soundFile) {
        return getExternalCacheDir() + File.separator + soundFile.getFilePath();
    }
}