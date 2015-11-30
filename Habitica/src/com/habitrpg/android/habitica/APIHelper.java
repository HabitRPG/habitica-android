package com.habitrpg.android.habitica;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

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
import com.habitrpg.android.habitica.callbacks.TaskDeletionCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.InAppPurchasesApiService;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.api.TypeAdapter.TagsAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.SkillList;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocial;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocialTokens;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.utils.SkillDeserializer;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
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
    private Context mContext;
    private HostConfig cfg;

    //private OnHabitsAPIResult mResultListener;
    //private HostConfig mConfig;
    public APIHelper(Context c, final HostConfig cfg) {
        this.mContext = c;
        this.cfg = cfg;

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("x-api-key", cfg.getApi());
                request.addHeader("x-api-user", cfg.getUser());
            }
        };

        Type taskTagClassListType = new TypeToken<List<TaskTag>>() {
        }.getType();


        //Exclusion stratety needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
        Gson gson = new GsonBuilder()
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
                .registerTypeAdapter(SkillList.class, new SkillDeserializer())
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

    public void registerUser(View btnClicked, String username, String email, String password, String confirmPassword) {

        //   ATAskRegisterUser reg = new ATAskRegisterUser(mResultListener,mConfig,btnClicked);
        //	String[] params = {username,email,password,confirmPassword};
        //	reg.execute(params);

    }

    public void connectUser(String username, String password, Callback<UserAuthResponse> callback) {
        //	ATaskConnectUser con = new ATaskConnectUser(mResultListener,mConfig,btnClicked);
        //	String[] params = {username,password};
        //	con.execute(params);
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

	public void deleteTask(Task item, TaskDeletionCallback cb) {
        this.apiService.deleteTask(item.getId(), cb);
    }

    public void updateTask(Task item, Callback cb) {
        this.apiService.updateTask(item.getId(), item, cb);
    }

	@Override
	public Throwable handleError(RetrofitError cause) {
        final Activity activity = (Activity) this.mContext;

        if (cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
            //It also handles timeouts
            showConnectionProblemDialog(activity, R.string.network_error_no_network_body);
            return cause;
        } else if (cause.getKind().equals(RetrofitError.Kind.HTTP)) {
            int status = cause.getResponse().getStatus();
            if (status == 401) {
                showConnectionProblemDialog(activity, R.string.authentication_error_title, R.string.authentication_error_body);
                return cause;
            } else if (status >= 500 && status < 600) {
                showConnectionProblemDialog(activity,R.string.internal_error_api);
                return cause;
            }
		}
        showConnectionProblemDialog(activity, R.string.internal_error_api);

        return cause;
	}

    private void showConnectionProblemDialog(final Activity activity, final int resourceMessageString) {
        showConnectionProblemDialog(activity, R.string.network_error_title, resourceMessageString);
    }

    private void showConnectionProblemDialog(final Activity activity, final int resourceTitleString, final int resourceMessageString){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle(resourceTitleString)
                        .setMessage(resourceMessageString)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(R.drawable.ic_warning_black)
                        .show();
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
