package com.habitrpg.android.habitica;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.crashlytics.android.Crashlytics;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.database.CheckListItemExcludeStrategy;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.InAppPurchasesApiService;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.api.TypeAdapter.TagsAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Purchases;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
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
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.utils.ChecklistItemSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ContentDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.CustomizationDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.DateDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.EggListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FAQArticleListDeserilializer;
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
import com.magicmicky.habitrpgwrapper.lib.utils.TutorialStepListDeserializer;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.Profiler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;


public class APIHelper implements ErrorHandler, Profiler {

    private static final String TAG = "ApiHelper";
    // I think we don't need the APIHelper anymore we could just use ApiService
    public final ApiService apiService;
    private final InAppPurchasesApiService inAppPurchasesService;
    private HostConfig cfg;

    //private OnHabitsAPIResult mResultListener;
    //private HostConfig mConfig;
    public APIHelper(final HostConfig cfg) {
        this.cfg = cfg;
        Crashlytics.getInstance().core.setUserIdentifier(cfg.getUser());
        Crashlytics.getInstance().core.setUserName(cfg.getUser());
        Amplitude.getInstance().setUserId(cfg.getUser());

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("x-api-key", cfg.getApi());
                request.addHeader("x-api-user", cfg.getUser());
                request.addHeader("x-client", "habitica-android");
            }
        };

        Type taskTagClassListType = new TypeToken<List<TaskTag>>() {
        }.getType();


        Type taskClassListType = new TypeToken<List<Task>>() {}.getType();
        Type skillListType = new TypeToken<List<Skill>>() {}.getType();
        Type checklistType = new TypeToken<List<ChecklistItem>>() {}.getType();
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

        //Exclusion stratety needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
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
                .registerTypeAdapter(taskTagClassListType, new TagsAdapter())
                .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(skillListType, new SkillDeserializer())
                .registerTypeAdapter(ChecklistItem.class, new ChecklistItemSerializer())
                .registerTypeAdapter(taskClassListType, new TaskListDeserializer())
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
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(ContentResult.class, new ContentDeserializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        Server server = new Server(cfg.getAddress());

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(server.toString())
                .setErrorHandler(this)
                .setProfiler(this)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))

                .build();
        this.apiService = adapter.create(ApiService.class);

        server = new Server(cfg.getAddress(), false);

        adapter = new RestAdapter.Builder()
                .setEndpoint(server.toString())
                .setErrorHandler(this)
                .setProfiler(this)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))

                .build();

        this.inAppPurchasesService = adapter.create(InAppPurchasesApiService.class);
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


    public void createNewTask(Task item, Callback cb) {
        this.apiService.createItem(item, cb);
    }

    public void retrieveUser(final HabitRPGUserCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                apiService.getUser(callback);
            }
        }).start();
    }

    public void updateTaskDirection(String id, TaskDirection direction, TaskScoringCallback callback) {
        this.apiService.postTaskDirection(id, direction.toString(), callback);
    }

    public void registerUser(String username, String email, String password, String confirmPassword, Callback<UserAuthResponse> callback) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        auth.setConfirmPassword(confirmPassword);
        auth.setEmail(email);
        this.apiService.registerUser(auth, callback);
    }

    public void connectUser(String username, String password, Callback<UserAuthResponse> callback) {
        UserAuth auth = new UserAuth();
        auth.setUsername(username);
        auth.setPassword(password);
        this.apiService.connectLocal(auth, callback);
    }

	public void connectSocial(String userId, String accessToken, Callback<UserAuthResponse> callback) {
		UserAuthSocial auth = new UserAuthSocial();
		auth.setNetwork("facebook");
        UserAuthSocialTokens authResponse = new UserAuthSocialTokens();
        authResponse.setClient_id(userId);
        authResponse.setAccess_token(accessToken);
        auth.setAuthResponse(authResponse);
		this.apiService.connectSocial(auth, callback);
	}

    public void updateTask(Task item, Callback cb) {
        this.apiService.updateTask(item.getId(), item, cb);
    }

    public class ErrorResponse{
        public String err;
    }

    public class ErrorListResponse {
        public List<String> err;
    }

	@Override
	public Throwable handleError(RetrofitError cause) {

        if (cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
            //It also handles timeouts
            showConnectionProblemDialog(R.string.network_error_no_network_body);
            return cause;
        } else if (cause.getKind().equals(RetrofitError.Kind.HTTP)) {
            Crashlytics.getInstance().core.log(Log.INFO, "Habitica", "Error loading " + cause.getUrl());
            retrofit.client.Response response = cause.getResponse();

            ErrorResponse res = null;

            try {
                res = new ErrorResponse();
                res.err = (String) cause.getBodyAs(String.class);
            } catch (RuntimeException e) {
                try {
                    res = (ErrorResponse) cause.getBodyAs(ErrorResponse.class);
                } catch (RuntimeException e2) {
                    try {
                        ErrorListResponse resList = (ErrorListResponse) cause.getBodyAs(ErrorListResponse.class);
                        if (resList.err != null && resList.err.size() >= 1) {
                            res = new ErrorResponse();
                            res.err = resList.err.get(0);
                        }
                    } catch (RuntimeException e3) {
                        res = null;
                    }

                }
            }

            int status = response.getStatus();
            if (status == 401) {
                if(res != null && res.err != null && !res.err.isEmpty()) {
                    showConnectionProblemDialog("", res.err);
                } else {
                    showConnectionProblemDialog(R.string.authentication_error_title, R.string.authentication_error_body);
                }

                return cause;
            } else if (status >= 500 && status < 600) {
                this.showConnectionProblemDialog(R.string.internal_error_api);
                return cause;
            } else if (status == 404 && cause.getUrl().endsWith("party/chat")) {
                return cause;
            } else if (status == 400) {
                if(res != null && res.err != null && !res.err.isEmpty()) {
                    showConnectionProblemDialog("", res.err);
                }
                return cause;
            }
		}
        this.showConnectionProblemDialog(R.string.internal_error_api);

        return cause;
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
        HabiticaApplication.currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (!(HabiticaApplication.currentActivity).isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HabiticaApplication.currentActivity)
                            .setTitle(resourceTitleString)
                            .setMessage(resourceMessageString)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                    if (!resourceTitleString.isEmpty()) {
                        builder.setIcon(R.drawable.ic_warning_black);
                    }

                    builder.show();
                }
            }
        });
    }

	@Override
	public Object beforeCall() {
		return null;
	}

	@Override
	public void afterCall(RequestInformation requestInfo, long elapsedTime, int statusCode, Object beforeCallData) {

	}

	public void toggleSleep(Callback<Void> cb){
		apiService.sleep(cb);
	}

	public void reviveUser(HabitRPGUserCallback cb) {
        apiService.revive(cb);
    }

    public PurchaseValidationResult validatePurchase(PurchaseValidationRequest request)
    {
       return inAppPurchasesService.validatePurchase(cfg.getUser(), cfg.getApi(), request);
    }
}
