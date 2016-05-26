package com.habitrpg.android.habitica;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import com.amplitude.api.Amplitude;
import com.crashlytics.android.Crashlytics;
import com.habitrpg.android.habitica.database.CheckListItemExcludeStrategy;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.InAppPurchasesApiService;
import com.magicmicky.habitrpgwrapper.lib.api.MaintenanceApiService;
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
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
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
import com.magicmicky.habitrpgwrapper.lib.utils.SkillDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskTagDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TutorialStepListDeserializer;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    private final InAppPurchasesApiService inAppPurchasesService;
    public final MaintenanceApiService maintenanceService;
    private final GsonConverterFactory gsonConverter;
    private final Retrofit retrofitAdapter;
    private HostConfig cfg;

    final Observable.Transformer apiCallTransformer =
            observable -> ((Observable)observable).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            .doOnError(this);

    private AlertDialog displayedAlert;

    //private OnHabitsAPIResult mResultListener;
    //private HostConfig mConfig;
    public APIHelper(final HostConfig cfg) {
        this.cfg = cfg;
        Crashlytics.getInstance().core.setUserIdentifier(cfg.getUser());
        Crashlytics.getInstance().core.setUserName(cfg.getUser());
        Amplitude.getInstance().setUserId(cfg.getUser());

        Type taskTagClassListType = new TypeToken<List<TaskTag>>() {
        }.getType();


        Type skillListType = new TypeToken<List<Skill>>() {}.getType();
        Type customizationListType = new TypeToken<List<Customization>>() {}.getType();
        Type tutorialStepListType = new TypeToken<List<TutorialStep>>() {}.getType();
        Type faqArticleListType = new TypeToken<List<FAQArticle>>() {}.getType();
        Type itemDataListType = new TypeToken<List<ItemData>>() {}.getType();
        Type eggListType = new TypeToken<List<Egg>>() {}.getType();
        Type foodListType = new TypeToken<List<Food>>() {}.getType();
        Type hatchingPotionListType = new TypeToken<List<HatchingPotion>>() {}.getType();
        Type questContentListType = new TypeToken<List<QuestContent>>() {}.getType();
        Type petListType = new TypeToken<HashMap<String, Pet>>() {}.getType();
        Type mountListType = new TypeToken<HashMap<String, Mount>>() {}.getType();

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
                .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(skillListType, new SkillDeserializer())
                .registerTypeAdapter(ChecklistItem.class, new ChecklistItemSerializer())
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
                    if (cfg.getUser() != null) {
                        Request request = original.newBuilder()
                                .header("x-api-key", cfg.getApi())
                                .header("x-api-user", cfg.getUser())
                                .header("x-client", "habitica-android")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    } else {
                        return chain.proceed(original);
                    }
                })
                .build();


        gsonConverter = GsonConverterFactory.create(gson);

        Server server = new Server(cfg.getAddress());
        retrofitAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(server.toString())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build();
        this.apiService = retrofitAdapter.create(ApiService.class);

        server = new Server(cfg.getAddress(), false);
        Retrofit adapter = new Retrofit.Builder()
                .baseUrl(server.toString())
                .addConverterFactory(gsonConverter)
                .build();
        this.inAppPurchasesService = adapter.create(InAppPurchasesApiService.class);

        adapter = new Retrofit.Builder()
                .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build();
        this.maintenanceService = adapter.create(MaintenanceApiService.class);
    }

    private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING:
                    return Boolean.parseBoolean(in.nextString());
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };

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
       Call<PurchaseValidationResult> response = inAppPurchasesService.validatePurchase(cfg.getUser(), cfg.getApi(), request);
        return response.execute().body();
    }

    @SuppressWarnings("unchecked")
    public <T> Observable.Transformer<T, T> configureApiCallObserver() {
        return (Observable.Transformer<T, T>) apiCallTransformer;
    }
}
