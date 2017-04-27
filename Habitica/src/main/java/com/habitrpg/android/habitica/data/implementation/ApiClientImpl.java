package com.habitrpg.android.habitica.data.implementation;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.amplitude.api.Amplitude;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.ErrorResponse;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.api.ApiService;
import com.habitrpg.android.habitica.api.Server;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.database.CheckListItemExcludeStrategy;
import com.habitrpg.android.habitica.helpers.PopupNotificationsManager;
import com.habitrpg.android.habitica.models.AchievementResult;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.PurchaseValidationRequest;
import com.habitrpg.android.habitica.models.PurchaseValidationResult;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.SubscriptionValidationRequest;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.auth.UserAuth;
import com.habitrpg.android.habitica.models.auth.UserAuthResponse;
import com.habitrpg.android.habitica.models.auth.UserAuthSocial;
import com.habitrpg.android.habitica.models.auth.UserAuthSocialTokens;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.responses.BuyResponse;
import com.habitrpg.android.habitica.models.responses.FeedResponse;
import com.habitrpg.android.habitica.models.responses.HabitResponse;
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.responses.Status;
import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.responses.UnlockResponse;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Purchases;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.utils.BooleanAsIntAdapter;
import com.habitrpg.android.habitica.utils.ChallengeDeserializer;
import com.habitrpg.android.habitica.utils.ChatMessageDeserializer;
import com.habitrpg.android.habitica.utils.ChatMessageListDeserializer;
import com.habitrpg.android.habitica.utils.ChecklistItemSerializer;
import com.habitrpg.android.habitica.utils.ContentDeserializer;
import com.habitrpg.android.habitica.utils.CustomizationDeserializer;
import com.habitrpg.android.habitica.utils.DateDeserializer;
import com.habitrpg.android.habitica.utils.EggListDeserializer;
import com.habitrpg.android.habitica.utils.FAQArticleListDeserilializer;
import com.habitrpg.android.habitica.utils.FeedResponseDeserializer;
import com.habitrpg.android.habitica.utils.FoodListDeserializer;
import com.habitrpg.android.habitica.utils.GroupSerialization;
import com.habitrpg.android.habitica.utils.HatchingPotionListDeserializer;
import com.habitrpg.android.habitica.utils.EquipmentListDeserializer;
import com.habitrpg.android.habitica.utils.MountListDeserializer;
import com.habitrpg.android.habitica.utils.MountMapDeserializer;
import com.habitrpg.android.habitica.utils.PetListDeserializer;
import com.habitrpg.android.habitica.utils.PetMapDeserializer;
import com.habitrpg.android.habitica.utils.PurchasedDeserializer;
import com.habitrpg.android.habitica.utils.QuestCollectDeserializer;
import com.habitrpg.android.habitica.utils.QuestListDeserializer;
import com.habitrpg.android.habitica.utils.RemindersItemSerializer;
import com.habitrpg.android.habitica.utils.SkillDeserializer;
import com.habitrpg.android.habitica.utils.TaskListDeserializer;
import com.habitrpg.android.habitica.utils.TaskSerializer;
import com.habitrpg.android.habitica.utils.TaskTagDeserializer;
import com.habitrpg.android.habitica.utils.TutorialStepListDeserializer;
import com.habitrpg.android.habitica.utils.UserDeserializer;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class ApiClientImpl implements Action1<Throwable>, ApiClient {
    private final GsonConverterFactory gsonConverter;
    private final HostConfig hostConfig;
    private final Retrofit retrofitAdapter;
    private final PopupNotificationsManager popupNotificationsManager;

    private CrashlyticsProxy crashlyticsProxy;
    private Context context;

    // I think we don't need the ApiClientImpl anymore we could just use ApiService
    private final ApiService apiService;

    private final Observable.Transformer apiCallTransformer =
            observable -> ((Observable) observable)
                    .map(new Func1<HabitResponse, Object>() {
                        @Override
                        public Object call(HabitResponse habitResponse) {
                            if (habitResponse.notifications != null) {
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
    public ApiClientImpl(GsonConverterFactory gsonConverter, HostConfig hostConfig, CrashlyticsProxy crashlyticsProxy, PopupNotificationsManager popupNotificationsManager, Context context) {
        this.gsonConverter = gsonConverter;
        this.hostConfig = hostConfig;
        this.context = context;
        this.crashlyticsProxy = crashlyticsProxy;
        this.popupNotificationsManager = popupNotificationsManager;
        this.popupNotificationsManager.setApiClient(this);

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
                    Request.Builder builder = original.newBuilder();
                    if (this.hostConfig.getUser() != null) {
                        builder = builder
                                .header("x-api-key", this.hostConfig.getApi())
                                .header("x-api-user", this.hostConfig.getUser());
                    }
                    builder = builder.header("x-client", "habitica-android");
                    if (userAgent != null) {
                        builder = builder.header("user-agent", userAgent);
                    }
                    Request request = builder.method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
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
        Type taskTagClassListType = new TypeToken<RealmList<Tag>>() {
        }.getType();
        Type skillListType = new TypeToken<List<Skill>>() {
        }.getType();
        Type customizationListType = new TypeToken<RealmList<Customization>>() {
        }.getType();
        Type tutorialStepListType = new TypeToken<RealmList<TutorialStep>>() {
        }.getType();
        Type faqArticleListType = new TypeToken<RealmList<FAQArticle>>() {
        }.getType();
        Type itemDataListType = new TypeToken<RealmList<Equipment>>() {
        }.getType();
        Type eggListType = new TypeToken<RealmList<Egg>>() {
        }.getType();
        Type foodListType = new TypeToken<RealmList<Food>>() {
        }.getType();
        Type hatchingPotionListType = new TypeToken<RealmList<HatchingPotion>>() {
        }.getType();
        Type questContentListType = new TypeToken<RealmList<QuestContent>>() {
        }.getType();
        Type petMapType = new TypeToken<Map<String, Pet>>() {
        }.getType();
        Type mountMapType = new TypeToken<Map<String, Mount>>() {
        }.getType();
        Type petListType = new TypeToken<RealmList<Pet>>() {
        }.getType();
        Type mountListType = new TypeToken<RealmList<Mount>>() {
        }.getType();
        Type questCollectListType = new TypeToken<RealmList<QuestCollect>>() {
        }.getType();
        Type chatMessageListType = new TypeToken<RealmList<ChatMessage>>() {
        }.getType();

        //Exclusion strategy needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new CheckListItemExcludeStrategy())
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        return field.getDeclaredClass().equals(ModelAdapter.class);
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
                .registerTypeAdapter(itemDataListType, new EquipmentListDeserializer())
                .registerTypeAdapter(eggListType, new EggListDeserializer())
                .registerTypeAdapter(foodListType, new FoodListDeserializer())
                .registerTypeAdapter(hatchingPotionListType, new HatchingPotionListDeserializer())
                .registerTypeAdapter(questContentListType, new QuestListDeserializer())
                .registerTypeAdapter(petListType, new PetListDeserializer())
                .registerTypeAdapter(mountListType, new MountListDeserializer())
                .registerTypeAdapter(petMapType, new PetMapDeserializer())
                .registerTypeAdapter(mountMapType, new MountMapDeserializer())
                .registerTypeAdapter(ChatMessage.class, new ChatMessageDeserializer())
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(ContentResult.class, new ContentDeserializer())
                .registerTypeAdapter(FeedResponse.class, new FeedResponseDeserializer())
                .registerTypeAdapter(Challenge.class, new ChallengeDeserializer())
                .registerTypeAdapter(User.class, new UserDeserializer())
                .registerTypeAdapter(questCollectListType, new QuestCollectDeserializer())
                .registerTypeAdapter(chatMessageListType, new ChatMessageListDeserializer())
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
        } else if (throwableClass.equals(retrofit2.adapter.rxjava.HttpException.class)) {
            HttpException error = (HttpException) throwable;
            ErrorResponse res = getErrorResponse(error);
            int status = error.code();

            if (error.response().raw().request().url().toString().endsWith("/user/push-devices")) {
                //workaround for an error that sometimes displays that the user already has this push device
                return;
            }

            if (status >= 400 && status < 500) {
                if (res != null && res.getDisplayMessage().length() > 0) {
                    showConnectionProblemDialog("", res.getDisplayMessage());
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
        Response<?> response = error.response();
        Converter<ResponseBody, ?> errorConverter =
                gsonConverter
                        .responseBodyConverter(ErrorResponse.class, new Annotation[0], retrofitAdapter);
        try {
            return (ErrorResponse) errorConverter.convert(response.errorBody());
        } catch (IOException e) {
            return new ErrorResponse();
        }
    }

    public Observable<User> retrieveUser(boolean withTasks) {

        Observable<User> userObservable = this.getUser();

        if (withTasks) {
            Observable<TaskList> tasksObservable = this.getTasks();

            userObservable = Observable.zip(userObservable, tasksObservable,
                    (habitRPGUser, tasks) -> {
                        List<Task> sortedTasks = new ArrayList<>();
                        sortedTasks.addAll(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getHabits()));
                        sortedTasks.addAll(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getDailys()));
                        sortedTasks.addAll(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getTodos()));
                        sortedTasks.addAll(sortTasks(tasks.tasks, habitRPGUser.getTasksOrder().getRewards()));

                        Realm.getDefaultInstance().executeTransactionAsync(realm -> realm.insertOrUpdate(sortedTasks));

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
        showConnectionProblemDialog(context.getString(resourceTitleString), context.getString(resourceMessageString));
    }

    private void showConnectionProblemDialog(final String resourceTitleString, final String resourceMessageString) {
        HabiticaApplication.currentActivity.runOnUiThread(() -> {
            if (!(HabiticaApplication.currentActivity).isFinishing() && displayedAlert == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HabiticaApplication.currentActivity)
                        .setTitle(resourceTitleString)
                        .setMessage(resourceMessageString)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> displayedAlert = null);

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
        if (crashlyticsProxy != null) {
            crashlyticsProxy.setUserIdentifier(this.hostConfig.getUser());
            crashlyticsProxy.setUserName(this.hostConfig.getUser());
        }
        Amplitude.getInstance().setUserId(this.hostConfig.getUser());
    }

    @Override
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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
    public Observable<User> getUser() {
        return apiService.getUser().compose(configureApiCallObserver());
    }

    @Override
    public Observable<User> updateUser(Map<String, Object> updateDictionary) {
        return apiService.updateUser(updateDictionary).compose(configureApiCallObserver());
    }

    @Override
    public Observable<User> registrationLanguage(String registrationLanguage) {
        return apiService.registrationLanguage(registrationLanguage).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<Equipment>> getInventoryBuyableGear() {
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
    public Observable<Void> validateSubscription(SubscriptionValidationRequest request) {
        return apiService.validateSubscription(request).map(habitResponse -> {
            if (habitResponse.notifications != null) {
                popupNotificationsManager.showNotificationDialog(habitResponse.notifications);
            }
            return habitResponse.getData();
        });
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
    public Observable<User> sellItem(String itemType, String itemKey) {
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
    public Observable<List<String>> postTaskNewPosition(String id, String position) {
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
    public Observable<User> revive() {
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
    public Observable<User> changeClass() {
        return apiService.changeClass().compose(configureApiCallObserver());
    }

    @Override
    public Observable<User> changeClass(String className) {
        return apiService.changeClass(className).compose(configureApiCallObserver());
    }

    @Override
    public Observable<User> disableClasses() {
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
    public Observable<PostChatMessageResult> postGroupChat(String groupId, Map<String, String> message) {
        return apiService.postGroupChat(groupId, message).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> deleteMessage(String groupId, String messageId) {
        return apiService.deleteMessage(groupId, messageId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<User>> getGroupMembers(String groupId, Boolean includeAllPublicFields) {
        return apiService.getGroupMembers(groupId, includeAllPublicFields).compose(configureApiCallObserver());
    }

    @Override
    public Observable<List<User>> getGroupMembers(String groupId, Boolean includeAllPublicFields, String lastId) {
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
        return apiService.forceStartQuest(groupId, group).compose(configureApiCallObserver());
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
        return apiService.validatePurchase(request).map(habitResponse -> {
            if (habitResponse.notifications != null) {
                popupNotificationsManager.showNotificationDialog(habitResponse.notifications);
            }
            return habitResponse.getData();
        });
    }

    @Override
    public Observable<User> changeCustomDayStart(Map<String, Object> updateObject) {
        return apiService.changeCustomDayStart(updateObject).compose(configureApiCallObserver());
    }

    @Override
    public Observable<User> getMember(String memberId) {
        return apiService.getMember(memberId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<AchievementResult> getMemberAchievements(String memberId) {
        return apiService.getMemberAchievements(memberId).compose(configureApiCallObserver());
    }

    @Override
    public Observable<PostChatMessageResult> postPrivateMessage(Map<String, String> messageDetails) {
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
    public Observable<List<Challenge>> getUserChallenges() {
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
    public Observable<Void> leaveChallenge(String challengeId, LeaveChallengeBody body) {
        return apiService.leaveChallenge(challengeId, body).compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> debugAddTenGems() {
        return apiService.debugAddTenGems().compose(configureApiCallObserver());
    }

    @Override
    public Observable<Void> readNotificaiton(String notificationId) {
        return apiService.readNotification(notificationId).compose(configureApiCallObserver());
    }

    public Observable<ContentResult> getContent() {
        return apiService.getContent(languageCode).compose(configureApiCallObserver());
    }


    @Override
    public Observable<Equipment> openMysteryItem() {
        return apiService.openMysteryItem().compose(configureApiCallObserver());
    }

}