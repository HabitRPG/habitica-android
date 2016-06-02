package com.habitrpg.android.habitica;

import com.amplitude.api.Amplitude;
import com.crashlytics.android.Crashlytics;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocial;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocialTokens;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class APIHelper implements Action1<Throwable> {

    // I think we don't need the APIHelper anymore we could just use ApiService
    public final ApiService apiService;
    private final GsonConverterFactory gsonConverter;
    private final HostConfig hostConfig;
    private final Retrofit retrofitAdapter;

    final Observable.Transformer apiCallTransformer =
            observable -> ((Observable)observable).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            .doOnError(this);

    private AlertDialog displayedAlert;

    //private OnHabitsAPIResult mResultListener;
    //private HostConfig mConfig;
    public APIHelper(GsonConverterFactory gsonConverter, HostConfig hostConfig) {
        this.gsonConverter = gsonConverter;
        this.hostConfig = hostConfig;
        Crashlytics.getInstance().core.setUserIdentifier(this.hostConfig.getUser());
        Crashlytics.getInstance().core.setUserName(this.hostConfig.getUser());
        Amplitude.getInstance().setUserId(this.hostConfig.getUser());

        Interceptor remove_data_interceptor = chain -> {
            Response response = chain.proceed(chain.request());
            String stringJson = response.body().string();
            JSONObject jsonObject = null;
            String dataString = null;
            try {
                jsonObject = new JSONObject(stringJson);
                if (jsonObject.has("data")) {
                    dataString = jsonObject.getString("data");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MediaType contentType = response.body().contentType();
            ResponseBody body = null;

            if (dataString != null) {
                body = ResponseBody.create(contentType, dataString);
            } else {
                body = ResponseBody.create(contentType, stringJson);
            }
            return response.newBuilder().body(body).build();
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(remove_data_interceptor)
                .addInterceptor(logging)
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    if (this.hostConfig.getUser() != null) {
                        Request request = original.newBuilder()
                                .header("x-api-key", this.hostConfig.getApi())
                                .header("x-api-user", this.hostConfig.getUser())
                                .header("x-client", "habitica-android")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    } else {
                        return chain.proceed(original);
                    }
                })
                .build();

        Server server = new Server(this.hostConfig.getAddress());
        retrofitAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(server.toString())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build();
        this.apiService = retrofitAdapter.create(ApiService.class);
    }

    public Observable<UserAuthResponse> registerUser(String username, String email, String password, String confirmPassword) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        auth.setConfirmPassword(confirmPassword);
        auth.setEmail(email);
        return this.apiService.registerUser(auth)
                .compose(this.configureApiCallObserver());
    }

    public Observable<UserAuthResponse> connectUser(String username, String password) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        return this.apiService.connectLocal(auth)
                .compose(this.configureApiCallObserver());
    }

	public Observable<UserAuthResponse> connectSocial(String userId, String accessToken) {
		UserAuthSocial auth = new UserAuthSocial();
		auth.setNetwork("facebook");
        UserAuthSocialTokens authResponse = new UserAuthSocialTokens();
        authResponse.setClient_id(userId);
        authResponse.setAccess_token(accessToken);
        auth.setAuthResponse(authResponse);
		return this.apiService.connectSocial(auth)
                .compose(this.configureApiCallObserver());
	}

    @Override
    public void call(Throwable throwable) {
        if (throwable.getClass().equals(ConnectException.class)) {
            this.showConnectionProblemDialog(R.string.internal_error_api);
        } else if (throwable.getClass().equals(HttpException.class)) {
            HttpException error = (HttpException)throwable;
            retrofit2.Response<?> response = error.response();
            ErrorResponse res = null;

            Converter<ResponseBody, ?> errorConverter =
                    gsonConverter
                            .responseBodyConverter(ErrorResponse.class, new Annotation[0], retrofitAdapter);
            try {
                res = (ErrorResponse) errorConverter.convert(response.errorBody());
            } catch (IOException e) {
                e.printStackTrace();
            }

            int status = error.code();
            if (status == 401) {
                if(res != null && res.message != null && !res.message.isEmpty()) {
                    showConnectionProblemDialog("", res.message);
                } else {
                    showConnectionProblemDialog(R.string.authentication_error_title, R.string.authentication_error_body);
                }

            } else if (status >= 500 && status < 600) {
                this.showConnectionProblemDialog(R.string.internal_error_api);
            } else if (status == 400) {
                if(res != null && res.message != null && !res.message.isEmpty()) {
                    showConnectionProblemDialog("", res.message);
                }
            } else {
                showConnectionProblemDialog(R.string.internal_error_api);
            }
        } else {
            Crashlytics.logException(throwable);
        }
    }

    public Observable<HabitRPGUser> retrieveUser(boolean withTasks) {
        Observable<HabitRPGUser> userObservable = apiService.getUser();
        if (withTasks) {
            Observable<TaskList> tasksObservable = apiService.getTasks();

            userObservable = Observable.zip(userObservable, tasksObservable, (habitRPGUser, tasks) -> {
                habitRPGUser.setHabits(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getHabits()));
                habitRPGUser.setDailys(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getDailys()));
                habitRPGUser.setTodos(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getTodos()));
                habitRPGUser.setRewards(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getRewards()));
                return habitRPGUser;
            });
        }
        return userObservable.compose(configureApiCallObserver());
    }

    private List<Task> sortTasks(Map<String, Task> taskMap, List<String> taskOrder){
        List<Task> taskList = new ArrayList<>();
        int position = 0;
        for (String taskId : taskOrder) {
            Task task = taskMap.get(taskId);
            if (task != null) {
                task.position = position;
                taskList.add(task);
                position++;
            }
        }
        return taskList;
    }

    public class ErrorResponse{
        public String message;
    }

    private void showConnectionProblemDialog(final int resourceMessageString) {
        showConnectionProblemDialog(R.string.network_error_title, resourceMessageString);
    }

    private void showConnectionProblemDialog(final int resourceTitleString, final int resourceMessageString) {
        Activity currentActivity = HabiticaApplication.currentActivity;
        if (currentActivity != null) {
            showConnectionProblemDialog(currentActivity.getString(resourceTitleString), currentActivity.getString(resourceMessageString));
        }
    }

    private void showConnectionProblemDialog(final String resourceTitleString, final String resourceMessageString){
        HabiticaApplication.currentActivity.runOnUiThread(() -> {
            if (!(HabiticaApplication.currentActivity).isFinishing() && displayedAlert == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HabiticaApplication.currentActivity)
                        .setTitle(resourceTitleString)
                        .setMessage(resourceMessageString)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> {
                            displayedAlert = null;
                        });

                if (!resourceTitleString.isEmpty()) {
                    builder.setIcon(R.drawable.ic_warning_black);
                }

                displayedAlert = builder.show();
            }
        });
    }

    public PurchaseValidationResult validatePurchase(PurchaseValidationRequest request) throws IOException {
       Call<PurchaseValidationResult> response = apiService.validatePurchase(request);
        return response.execute().body();
    }

    @SuppressWarnings("unchecked")
    public <T> Observable.Transformer<T, T> configureApiCallObserver() {
        return (Observable.Transformer<T, T>) apiCallTransformer;
    }

    public void updateAuthenticationCredentials(String userID, String apiToken) {
        this.hostConfig.setUser(userID);
        this.hostConfig.setApi(apiToken);
        Crashlytics.getInstance().core.setUserIdentifier(this.hostConfig.getUser());
        Crashlytics.getInstance().core.setUserName(this.hostConfig.getUser());
        Amplitude.getInstance().setUserId(this.hostConfig.getUser());
    }
}
