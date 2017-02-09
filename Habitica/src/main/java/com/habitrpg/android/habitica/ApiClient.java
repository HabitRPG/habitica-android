package com.habitrpg.android.habitica;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.amplitude.api.Amplitude;
import com.habitrpg.android.habitica.database.CheckListItemExcludeStrategy;
import com.habitrpg.android.habitica.helpers.PopupNotificationsManager;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.IApiClient;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.models.AchievementResult;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Quest;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;
import com.magicmicky.habitrpgwrapper.lib.models.Status;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.responses.BuyResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
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
import com.magicmicky.habitrpgwrapper.lib.models.responses.SkillResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.UnlockResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.utils.BooleanAsIntAdapter;
import com.magicmicky.habitrpgwrapper.lib.utils.ChallengeDeserializer;
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

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class ApiClient implements Action1<Throwable>, IApiClient {
    private final GsonConverterFactory gsonConverter;
    private final HostConfig hostConfig;
    private final Retrofit retrofitAdapter;

    CrashlyticsProxy crashlyticsProxy;
    Context context;

    // I think we don't need the ApiClient anymore we could just use ApiService
    private final ApiService apiService;

    final Observable.Transformer apiCallTransformer =
            observable -> ((Observable) observable)
                    .map(new Func1<HabitResponse, Object>() {
                        @Override
                        public Object call(HabitResponse habitResponse) {
                            if (habitResponse.notifications != null) {
                                PopupNotificationsManager popupNotificationsManager = PopupNotificationsManager.getInstance(ApiClient.this, context);
                                popupNotificationsManager.showNotificationDialog(habitResponse.notifications);
                            }
                            return habitResponse.getData();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this);
    private AlertDialog displayedAlert;
    private String languageCode;

    //private OnHabitsAPIResult mResultListener;
    //private HostConfig mConfig;
    public ApiClient(GsonConverterFactory gsonConverter, HostConfig hostConfig, CrashlyticsProxy crashlyticsProxy,  Context context) {
        this.gsonConverter = gsonConverter;
        this.hostConfig = hostConfig;
        this.context = context;

        HabiticaBaseApplication.getComponent().inject(this);
        crashlyticsProxy.setUserIdentifier(this.hostConfig.getUser());
        crashlyticsProxy.setUserName(this.hostConfig.getUser());
        Amplitude.getInstance().setUserId(this.hostConfig.getUser());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        String userAgent = System.getProperty("http.agent");

        OkHttpClient client = new OkHttpClient.Builder()
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
                .registerTypeAdapter(Challenge.class, new ChallengeDeserializer())
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
        return this.apiService.registerUser(auth).compose(configureApiCallObserver());
    }

    public Observable<UserAuthResponse> connectUser(String username, String password) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        return this.apiService.connectLocal(auth).compose(configureApiCallObserver());
    }

    public Observable<UserAuthResponse> connectSocial(String network, String userId, String accessToken) {
        UserAuthSocial auth = new UserAuthSocial();
        auth.setNetwork(network);
        UserAuthSocialTokens authResponse = new UserAuthSocialTokens();
        authResponse.setClient_id(userId);
        authResponse.setAccess_token(accessToken);
        auth.setAuthResponse(authResponse);

        return this.apiService.connectSocial(auth).compose(configureApiCallObserver());
    }

    @Override
    public void call(Throwable throwable) {
        final Class<?> throwableClass = throwable.getClass();
        if (SocketException.class.isAssignableFrom(throwableClass) || SSLException.class.isAssignableFrom(throwableClass)) {
            this.showConnectionProblemDialog(R.string.internal_error_api);
        } else if (throwableClass.equals(SocketTimeoutException.class) || UnknownHostException.class.equals(throwableClass)) {
            this.showConnectionProblemDialog(R.string.network_error_no_network_body);
        } else if (throwableClass.equals(HttpException.class)) {
            HttpException error = (HttpException) throwable;
            ErrorResponse res = getErrorResponse(error);
            int status = error.code();

            if (error.response().raw().request().url().toString().endsWith("/user/push-devices")) {
                //workaround for an error that sometimes displays that the user already has this push device
                return;
            }

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
            crashlyticsProxy.logException(throwable);
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

        Observable<HabitRPGUser> userObservable = apiService.getUser()
                .compose(configureApiCallObserver());

        if (withTasks) {
            Observable<HabitResponse<TaskList>> tasksObservable = apiService.getTasks();

            userObservable = Observable.zip(userObservable, tasksObservable,
                    (habitRPGUser, taskListHabitResponse) -> {
                        TaskList tasks = taskListHabitResponse.getData();

                        habitRPGUser.setHabits(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getHabits()));
                        habitRPGUser.setDailys(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getDailys()));
                        habitRPGUser.setTodos(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getTodos()));
                        habitRPGUser.setRewards(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getRewards()));
                        for (Task task : tasks.tasks.values()) {
                            switch (task.getType()) {
                                case "habit":
                                    habitRPGUser.getHabits().add(task);
                                    break;
                                case "daily":
                                    habitRPGUser.getDailys().add(task);
                                    break;
                                case "todo":
                                    habitRPGUser.getTodos().add(task);
                                    break;
                                case "reward":
                                    habitRPGUser.getRewards().add(task);
                                    break;
                            }
                        }

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
                taskMap.remove(taskId);
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

    /*
     This function is used with Observer.compose to reuse transformers across the application.
     See here for more info: http://blog.danlew.net/2015/03/02/dont-break-the-chain/
     */

    @SuppressWarnings("unchecked")
    public <T> Observable.Transformer<HabitResponse<T>, T> configureApiCallObserver() {
        return (Observable.Transformer<HabitResponse<T>, T>) apiCallTransformer;
    }

    public void updateAuthenticationCredentials(String userID, String apiToken) {
        this.hostConfig.setUser(userID);
        this.hostConfig.setApi(apiToken);
        crashlyticsProxy.setUserIdentifier(this.hostConfig.getUser());
        crashlyticsProxy.setUserName(this.hostConfig.getUser());
        Amplitude.getInstance().setUserId(this.hostConfig.getUser());
    }

    @Override
    public void setLanguageCode(String languageCode) {
        this.languageCode =  languageCode;
    }

    @Override
    public Observable<Status> getStatus() {
        return apiService.getStatus().compose(configureApiCallObserver());
    }

    @Override
    public Observable<ContentResult> getContent(String language) {
        return apiService.getContent(language).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> getUser() {
        return apiService.getUser().compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> updateUser(Map<String, Object> updateDictionary) {
        return apiService.updateUser(updateDictionary).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> registrationLanguage(String registrationLanguage) {
        return apiService.registrationLanguage(registrationLanguage).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<ItemData>> getInventoryBuyableGear() {
        return apiService.getInventoryBuyableGear().compose(configureApiCallObserver());
    }

    @Override
    public Observable<Items> equipItem(String type, String itemKey) {
        return apiService.equipItem(type, itemKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<BuyResponse> buyItem(String itemKey) {

        return apiService.buyItem(itemKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> purchaseItem(String type, String itemKey) {

        return apiService.purchaseItem(type, itemKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> validateSubscription(SubscriptionValidationRequest request){
        return apiService.validateSubscription(request).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> purchaseHourglassItem(String type, String itemKey) {

        return apiService.purchaseHourglassItem(type, itemKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> purchaseMysterySet(String itemKey) {
        return apiService.purchaseMysterySet(itemKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> purchaseQuest(String key) {
        return apiService.purchaseQuest(key).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> sellItem(String itemType, String itemKey) {
        return apiService.sellItem(itemType, itemKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<FeedResponse> feedPet(String petKey, String foodKey) {
        return apiService.feedPet(petKey, foodKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Items> hatchPet(String eggKey, String hatchingPotionKey) {
        return apiService.hatchPet(eggKey, hatchingPotionKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<TaskList> getTasks() {
        return apiService.getTasks().compose(configureApiCallObserver());
    }

    @Override
    public Observable<UnlockResponse> unlockPath(String path) {
        return apiService.unlockPath(path).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Task> getTask(String id) {
        return apiService.getTask(id).compose(configureApiCallObserver());
    }

    @Override
    public Observable<TaskDirectionData> postTaskDirection(String id, String direction) {
        return apiService.postTaskDirection(id, direction).compose(configureApiCallObserver());
    }

    @Override
    public Observable<ArrayList<String>> postTaskNewPosition(String id, String position) {
        return apiService.postTaskNewPosition(id, position).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Task> scoreChecklistItem(String taskId, String itemId) {
        return apiService.scoreChecklistItem(taskId, itemId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Task> createItem(Task item) {
        return apiService.createItem(item).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<Task>> createTasks(List<Task> tasks) {
        return apiService.createTasks(tasks).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Task> updateTask(String id, Task item) {
        return apiService.updateTask(id, item).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> deleteTask(String id) {
        return apiService.deleteTask(id).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Tag> createTag(Tag tag) {
        return apiService.createTag(tag).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Tag> updateTag(String id, Tag tag) {
        return apiService.updateTag(id, tag).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> deleteTag(String id) {
        return apiService.deleteTag(id).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Boolean> sleep() {
        return apiService.sleep().compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> revive() {
        return apiService.revive().compose(configureApiCallObserver());
    }

    @Override
    public Observable<SkillResponse> useSkill(String skillName, String targetType, String targetId) {
        return apiService.useSkill(skillName, targetType, targetId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<SkillResponse> useSkill(String skillName, String targetType) {
        return apiService.useSkill(skillName, targetType).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> changeClass() {
        return apiService.changeClass().compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> changeClass(String className) {
        return apiService.changeClass(className).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> disableClasses() {
        return apiService.disableClasses().compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> markPrivateMessagesRead() {
        return apiService.markPrivateMessagesRead().compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<Group>> listGroups(String type) {
        return apiService.listGroups(type).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Group> getGroup(String groupId) {
        return apiService.getGroup(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> updateGroup(String id, Group item) {
        return apiService.updateGroup(id, item).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<ChatMessage>> listGroupChat(String groupId) {
        return apiService.listGroupChat(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Group> joinGroup(String groupId) {
        return apiService.joinGroup(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> leaveGroup(String groupId) {
        return apiService.leaveGroup(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<PostChatMessageResult> postGroupChat(String groupId, HashMap<String, String> message) {
        return apiService.postGroupChat(groupId, message).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> deleteMessage(String groupId, String messageId) {
        return apiService.deleteMessage(groupId, messageId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<HabitRPGUser>> getGroupMembers(String groupId, Boolean includeAllPublicFields) {
        return apiService.getGroupMembers(groupId, includeAllPublicFields).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<HabitRPGUser>> getGroupMembers(String groupId, Boolean includeAllPublicFields, String lastId) {
        return apiService.getGroupMembers(groupId, includeAllPublicFields, lastId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<ChatMessage> likeMessage(String groupId, String mid) {
        return apiService.likeMessage(groupId, mid).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> flagMessage(String groupId, String mid) {
        return apiService.flagMessage(groupId, mid).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> seenMessages(String groupId) {
        return apiService.seenMessages(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> inviteToGroup(String groupId, Map<String, Object> inviteData) {
        return apiService.inviteToGroup(groupId, inviteData).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> rejectGroupInvite(String groupId) {
        return apiService.rejectGroupInvite(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> acceptQuest(String groupId) {
        return apiService.acceptQuest(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> rejectQuest(String groupId) {
        return apiService.rejectQuest(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> cancelQuest(String groupId) {
        return apiService.cancelQuest(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Quest> forceStartQuest(String groupId, Group group) {
        return apiService.forceStartQuest(groupId,group).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Quest> inviteToQuest(String groupId, String questKey) {
        return apiService.inviteToQuest(groupId, questKey).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Quest> abortQuest(String groupId) {
        return apiService.abortQuest(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> leaveQuest(String groupId) {
        return apiService.leaveQuest(groupId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<PurchaseValidationResult> validatePurchase(PurchaseValidationRequest request) {
        return apiService.validatePurchase(request).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> changeCustomDayStart(Map<String, Object> updateObject) {
        return apiService.changeCustomDayStart(updateObject).compose(configureApiCallObserver());
    }

    @Override
    public Observable<HabitRPGUser> GetMember(String memberId) {
        return apiService.GetMember(memberId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<AchievementResult> GetMemberAchievements(String memberId) {
        return apiService.GetMemberAchievements(memberId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<PostChatMessageResult> postPrivateMessage(HashMap<String, String> messageDetails) {
        return apiService.postPrivateMessage(messageDetails).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Shop> fetchShopInventory(String identifier) {
        return apiService.fetchShopInventory(identifier).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> addPushDevice(Map<String, String> pushDeviceData) {
        return apiService.addPushDevice(pushDeviceData).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> deletePushDevice(String regId) {
        return apiService.deletePushDevice(regId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<ArrayList<Challenge>> getUserChallenges() {
        return apiService.getUserChallenges().compose(configureApiCallObserver());
    }

    @Override
    public Observable<TaskList> getChallengeTasks(String challengeId) {
        return apiService.getChallengeTasks(challengeId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Challenge> getChallenge(String challengeId) {
        return apiService.getChallenge(challengeId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Challenge> joinChallenge(String challengeId) {
        return apiService.joinChallenge(challengeId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> leaveChallenge(String challengeId) {
        return apiService.leaveChallenge(challengeId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> debugAddTenGems() {
        return apiService.debugAddTenGems().compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> readNotificaiton(String notificationId) {
        return apiService.readNotification(notificationId).compose(configureApiCallObserver());
    }

    public static class ErrorResponse {
        public String message;
    }

    public Observable<ContentResult>getContent() {
        return apiService.getContent(languageCode).compose(configureApiCallObserver());
    }


    @Override
    public Observable<ItemData> openMysteryItem() {
        return apiService.openMysteryItem().compose(configureApiCallObserver());
    }

}
