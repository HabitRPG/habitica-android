package com.habitrpg.android.habitica;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.amplitude.api.Amplitude;
import com.crashlytics.android.Crashlytics;
import com.habitrpg.android.habitica.database.CheckListItemExcludeStrategy;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Purchases;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocial;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocialTokens;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.responses.FeedResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.utils.BooleanAsIntAdapter;
import com.magicmicky.habitrpgwrapper.lib.utils.ChatMessageDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ChecklistItemSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ContentDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.CustomizationDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.DateDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.EggListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FAQArticleListDeserilializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FeedResponseDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FoodListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.GroupSerialization;
import com.magicmicky.habitrpgwrapper.lib.utils.HatchingPotionListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ItemDataListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.MountListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.PetListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.PurchasedDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.QuestListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.RemindersItemSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.SkillDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskTagDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TutorialStepListDeserializer;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
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
    final Observable.Transformer apiCallTransformer =
            observable -> ((Observable) observable).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this);
    private final GsonConverterFactory gsonConverter;
    private final HostConfig hostConfig;
    private final Retrofit retrofitAdapter;
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
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        String userAgent = System.getProperty("http.agent");

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(remove_data_interceptor)
                .addInterceptor(logging)
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    if (this.hostConfig.getUser() != null) {
                        Request.Builder builder = original.newBuilder()
                                .header("x-api-key", this.hostConfig.getApi())
                                .header("x-api-user", this.hostConfig.getUser())
                                .header("x-client", "habitica-android");
                        if (userAgent != null) {
                            builder = builder.header("user-agent", userAgent);
                        }
                        Request request = builder.method(original.method(), original.body())
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

    public static GsonConverterFactory createGsonFactory() {
        Type taskTagClassListType = new TypeToken<List<TaskTag>>() {
        }.getType();
        Type skillListType = new TypeToken<List<Skill>>() {
        }.getType();
        Type customizationListType = new TypeToken<List<Customization>>() {
        }.getType();
        Type tutorialStepListType = new TypeToken<List<TutorialStep>>() {
        }.getType();
        Type faqArticleListType = new TypeToken<List<FAQArticle>>() {
        }.getType();
        Type itemDataListType = new TypeToken<List<ItemData>>() {
        }.getType();
        Type eggListType = new TypeToken<List<Egg>>() {
        }.getType();
        Type foodListType = new TypeToken<List<Food>>() {
        }.getType();
        Type hatchingPotionListType = new TypeToken<List<HatchingPotion>>() {
        }.getType();
        Type questContentListType = new TypeToken<List<QuestContent>>() {
        }.getType();
        Type petListType = new TypeToken<HashMap<String, Pet>>() {
        }.getType();
        Type mountListType = new TypeToken<HashMap<String, Mount>>() {
        }.getType();

        //Exclusion strategy needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new CheckListItemExcludeStrategy())
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaredClass().equals(ModelAdapter.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(taskTagClassListType, new TaskTagDeserializer())
                .registerTypeAdapter(Boolean.class, new BooleanAsIntAdapter())
                .registerTypeAdapter(boolean.class, new BooleanAsIntAdapter())
                .registerTypeAdapter(skillListType, new SkillDeserializer())
                .registerTypeAdapter(ChecklistItem.class, new ChecklistItemSerializer())
                .registerTypeAdapter(RemindersItem.class, new RemindersItemSerializer())
                .registerTypeAdapter(TaskList.class, new TaskListDeserializer())
                .registerTypeAdapter(Purchases.class, new PurchasedDeserializer())
                .registerTypeAdapter(customizationListType, new CustomizationDeserializer())
                .registerTypeAdapter(tutorialStepListType, new TutorialStepListDeserializer())
                .registerTypeAdapter(faqArticleListType, new FAQArticleListDeserilializer())
                .registerTypeAdapter(Group.class, new GroupSerialization())
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(itemDataListType, new ItemDataListDeserializer())
                .registerTypeAdapter(eggListType, new EggListDeserializer())
                .registerTypeAdapter(foodListType, new FoodListDeserializer())
                .registerTypeAdapter(hatchingPotionListType, new HatchingPotionListDeserializer())
                .registerTypeAdapter(questContentListType, new QuestListDeserializer())
                .registerTypeAdapter(petListType, new PetListDeserializer())
                .registerTypeAdapter(mountListType, new MountListDeserializer())
                .registerTypeAdapter(ChatMessage.class, new ChatMessageDeserializer())
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(ContentResult.class, new ContentDeserializer())
                .registerTypeAdapter(FeedResponse.class, new FeedResponseDeserializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        return GsonConverterFactory.create(gson);
    }

    public Observable<UserAuthResponse> registerUser(String username, String email, String password, String confirmPassword) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        auth.setConfirmPassword(confirmPassword);
        auth.setEmail(email);
        return this.apiService.registerUser(auth);
    }

    public Observable<UserAuthResponse> connectUser(String username, String password) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        return this.apiService.connectLocal(auth);
    }

    public Observable<UserAuthResponse> connectSocial(String userId, String accessToken) {
        UserAuthSocial auth = new UserAuthSocial();
        auth.setNetwork("facebook");
        UserAuthSocialTokens authResponse = new UserAuthSocialTokens();
        authResponse.setClient_id(userId);
        authResponse.setAccess_token(accessToken);
        auth.setAuthResponse(authResponse);
        return this.apiService.connectSocial(auth);
    }

    @Override
    public void call(Throwable throwable) {
        final Class<?> throwableClass = throwable.getClass();
        if (SocketException.class.isAssignableFrom(throwableClass)  ||  SSLException.class.isAssignableFrom(throwableClass)) {
            this.showConnectionProblemDialog(R.string.internal_error_api);
        } else if (throwableClass.equals(SocketTimeoutException.class) || UnknownHostException.class.equals(throwableClass)) {
            this.showConnectionProblemDialog(R.string.network_error_no_network_body);
        } else if (throwableClass.equals(HttpException.class)) {
            HttpException error = (HttpException) throwable;
            ErrorResponse res = getErrorResponse(error);

            int status = error.code();
            if (status >= 400 && status < 500) {
                if (res != null && res.message != null && !res.message.isEmpty()) {
                    showConnectionProblemDialog("", res.message);
                } else if (status == 401) {
                    showConnectionProblemDialog(R.string.authentication_error_title, R.string.authentication_error_body);
                }

            } else if (status >= 500 && status < 600) {
                this.showConnectionProblemDialog(R.string.internal_error_api);
            } else {
                showConnectionProblemDialog(R.string.internal_error_api);
            }
        } else {
            Crashlytics.logException(throwable);
        }
    }

    public ErrorResponse getErrorResponse(HttpException error) {
        retrofit2.Response<?> response = error.response();
        Converter<ResponseBody, ?> errorConverter =
                gsonConverter
                        .responseBodyConverter(ErrorResponse.class, new Annotation[0], retrofitAdapter);
        try {
            return (ErrorResponse) errorConverter.convert(response.errorBody());
        } catch (IOException e) {
            return new ErrorResponse();
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
        return userObservable;
    }

    private List<Task> sortTasks(Map<String, Task> taskMap, List<String> taskOrder) {
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

    public boolean hasAuthenticationKeys() {
        return this.hostConfig.getUser() != null;
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

    private void showConnectionProblemDialog(final String resourceTitleString, final String resourceMessageString) {
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

    public static class ErrorResponse {
        public String message;
    }
}
