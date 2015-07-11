package com.habitrpg.android.habitica;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskDeletionCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.api.TypeAdapter.TagsAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Tags;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.io.IOException;

import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.Profiler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;


public class APIHelper implements ErrorHandler, Profiler {

    private static final String TAG = "ApiHelper";
	private final ApiService apiService;
	private Context mContext;
	//private OnHabitsAPIResult mResultListener;
	//private HostConfig mConfig;
	public APIHelper(Context c, final HostConfig cfg) {
		this.mContext = c;

		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestInterceptor.RequestFacade request) {
				request.addHeader("x-api-key", cfg.getApi());
				request.addHeader("x-api-user", cfg.getUser());


			}
		};
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
				.registerTypeAdapter(Tags.class, new TagsAdapter().nullSafe())
				.registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
				.registerTypeAdapter(boolean.class, booleanAsIntAdapter)
				.create();

		Server server = new Server(cfg.getAddress());

		RestAdapter adapter = new RestAdapter.Builder()
				.setEndpoint(server.toString())
				.setErrorHandler(this)
				.setProfiler(this)
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setRequestInterceptor(requestInterceptor)
				.setConverter(new GsonConverter(gson))

				.build();
		this.apiService  = adapter.create(ApiService.class);

    }

	private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
		@Override public void write(JsonWriter out, Boolean value) throws IOException {
			if (value == null) {
				out.nullValue();
			} else {
				out.value(value);
			}
		}
		@Override public Boolean read(JsonReader in) throws IOException {
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


    public void createUndefNewTask(HabitItem item, Callback cb) {
        if(item instanceof Habit) {
            createNewTask((Habit) item, cb);
        } else if(item instanceof Daily) {
            createNewTask((Daily) item, cb);
        } else if(item instanceof ToDo) {
            createNewTask((ToDo) item, cb);
        } else if(item instanceof Reward) {
            createNewTask((Reward) item, cb);
        }
    }

	public void createNewTask(Habit habit,Callback<Habit> callback) {
        this.apiService.createItem(habit, callback);
	}
    public void createNewTask(ToDo toDo, Callback<ToDo> callback) {
        this.apiService.createItem(toDo, callback);
    }
    public void createNewTask(Daily d,Callback<Daily> callback) {
        this.apiService.createItem(d, callback);
    }
    public void createNewTask(Reward r,Callback<Reward> callback) {
        this.apiService.createItem(r, callback);
    }

	public void retrieveUser(HabitRPGUserCallback callback) {
		this.apiService.getUser(callback);
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

	public void deleteTask(HabitItem item, TaskDeletionCallback cb) {
		this.apiService.deleteTask(item.getId(), cb);
    }
    public void updateTask(Daily item, Callback<Daily> cb) {
        this.apiService.updateTask(item.getId(), item, cb);
    }
    public void updateTask(Habit item, Callback<Habit> cb) {
        this.apiService.updateTask(item.getId(), item, cb);
    }
    public void updateTask(ToDo item, Callback<ToDo> cb) {
        this.apiService.updateTask(item.getId(), item, cb);
    }
    public void updateTask(Reward item, Callback<Reward> cb) {
        this.apiService.updateTask(item.getId(), item, cb);
    }
    public void uprateUndefinedTask(HabitItem task, Callback cb) {
        if(task instanceof ToDo) {
            this.updateTask((ToDo) task, cb);
        } else if(task instanceof Daily) {
            this.updateTask((Daily) task, cb);
        } else if(task instanceof Reward) {
            this.updateTask((Reward) task, cb);
        } else if(task instanceof Habit) {
            this.updateTask((Habit) task,cb);
        }
    }


	public void buyItem(Reward.SpecialReward itemBought, View btn) {
	//	ATaskBuyItem buyItem = new ATaskBuyItem(mResultListener,btn, mConfig);
	//	buyItem.execute(itemBought);
	}
	public void changeTimeZone(int timeZoneOffset) {
//		ATaskChangeTimeZone changeTimeZone= new ATaskChangeTimeZone(mResultListener,mConfig);
	//	changeTimeZone.execute(timeZoneOffset);
	}

	public void revive() {
        Log.w(TAG, "Not done yet - revive");
//		ATaskRevive rev = new ATaskRevive(mResultListener,mConfig);
//		rev.execute();
	}

	@Override
	public Throwable handleError(RetrofitError cause) {

		retrofit.client.Response res = cause.getResponse();

		if (res != null) {
			retrofit.mime.TypedInput body = res.getBody();
		}

		if (cause.isNetworkError()) {
            final Activity activity = (Activity) this.mContext;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.network_error_title)
                            .setMessage(R.string.network_error_no_network_body)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

		}

		return cause;
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

/*
	private class ATaskGetUser extends AsyncTask<Void, Void, Void> {
    	private OnHabitsAPIResult callback;
    	private HostConfig config;
    	
    	public ATaskGetUser(OnHabitsAPIResult callback, HostConfig config) {
    		this.callback = callback;
    		this.config=config;
		}
    	@Override
    	protected void onPreExecute() {
    		this.callback.onPreResult();
    	}
		@Override
		protected Void doInBackground(Void... params) {
			GetUser getUser = new GetUser(this.callback,this.config);
			Answer as = getUser.getData();
			if(as!=null)
				as.parse();
			return null;
		}
    }
	
    private class ATaskPostUserDirection extends AsyncTask<String, Void, Void> {
    	private OnHabitsAPIResult callback;
    	private View btnClicked;
    	HostConfig config;
    	public ATaskPostUserDirection(OnHabitsAPIResult callback, View btnClicked, HostConfig config) {
    		this.callback = callback;
			this.btnClicked=btnClicked;
			this.config = config;
		}
    	@Override
    	protected void onPreExecute() {
    		if(this.btnClicked!=null)
    			this.btnClicked.setEnabled(false);
    		this.callback.onPreResult();
    	}
		@Override
		protected Void doInBackground(String... params) {
			PostTaskDirection post = new PostTaskDirection(callback, params[0], params[1], this.config);
			
			Answer as = post.getData();
			if(as!=null)
				as.parse();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void arg) {
    		if(this.btnClicked!=null)
    			this.btnClicked.setEnabled(true);	
    	
		}
    }
    
    
    private class ATaskPostTask extends AsyncTask<HabitItem, Void, Void> {
    	private OnHabitsAPIResult callback;
    	private HostConfig config;		
    	public ATaskPostTask(OnHabitsAPIResult callback, HostConfig config) {
    		this.callback = callback;
    		this.config=config;
		}

    	@Override
    	protected void onPreExecute() {
    		this.callback.onPreResult();
    	}
		@Override
		protected Void doInBackground(HabitItem... habit) {
			PostTask post = new PostTask(callback, config, habit[0]);
			Answer as = post.getData();
			if(as!=null)
				as.parse();
			return null;
		}
    }

	private class ATaskDeleteTask extends AsyncTask<HabitItem, Void, Void> {
		private OnHabitsAPIResult callback;
		private HostConfig config;
		public ATaskDeleteTask(OnHabitsAPIResult callback, HostConfig config) {
			this.callback = callback;
			this.config=config;
		}

		@Override
		protected void onPreExecute() {
			this.callback.onPreResult();
		}
		@Override
		protected Void doInBackground(HabitItem... habit) {
			DeleteTask del = new DeleteTask(callback, config, habit[0]);
			Answer as = del.getData();
			if(as!=null)
				as.parse();
			return null;
		}
	}
	private class ATaskUpdateTask extends AsyncTask<HabitItem, Void, Void> {
		private OnHabitsAPIResult callback;
		private HostConfig config;
		public ATaskUpdateTask(OnHabitsAPIResult callback, HostConfig config) {
			this.callback = callback;
			this.config=config;
		}

		@Override
		protected void onPreExecute() {
			this.callback.onPreResult();
		}
		@Override
		protected Void doInBackground(HabitItem... habit) {
			PutTask put = new PutTask(callback, config, habit[0]);
			Answer as = put.getData();
			if(as!=null)
				as.parse();
			return null;
		}
	}
	private class ATaskBuyItem extends AsyncTask<Reward.SpecialReward,Void,Void> {
		private final OnHabitsAPIResult callback;
		private final HostConfig config;
		private final View btnClicked;

		public ATaskBuyItem(OnHabitsAPIResult callback, View btnClicked, HostConfig config) {
			this.callback = callback;
			this.config=config;
			this.btnClicked = btnClicked;
        }

		@Override
		protected void onPreExecute() {
			if(this.btnClicked!=null)
				this.btnClicked.setEnabled(false);

			this.callback.onPreResult();
		}

		@Override
		protected Void doInBackground(Reward.SpecialReward... itemBought) {
			BuyItem buy = new BuyItem(callback,config,itemBought[0]);
			Answer as = buy.getData();
			if(as!=null)
				as.parse();
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if(this.btnClicked!=null)
				this.btnClicked.setEnabled(true);

		}
	}


	private class ATAskRegisterUser extends AsyncTask<String,Void,Void>{
		private final HostConfig config;
		private final OnHabitsAPIResult callback;
		private final View btnClicked;

		public ATAskRegisterUser(OnHabitsAPIResult callback, HostConfig mConfig, View btnClicked) {
			this.callback = callback;
			this.config = mConfig;
			this.btnClicked = btnClicked;
		}

		@Override
		protected void onPreExecute() {
			if(this.btnClicked!=null)
				this.btnClicked.setEnabled(false);

			this.callback.onPreResult();
		}

		@Override
		protected Void doInBackground(String... us) {
			RegisterUser reg = new RegisterUser(callback,config,us[0],us[1],us[2],us[3]);
			Answer as = reg.getData();
			if(as!=null)
				as.parse();
			return null;
		}
		@Override
		protected void onPostExecute(Void arg) {
			if(this.btnClicked!=null)
				this.btnClicked.setEnabled(true);

		}


	}

	private class ATaskConnectUser extends AsyncTask<String,Void,Void>{
		private final View btnClicked;
		private final HostConfig config;
		private final OnHabitsAPIResult callback;

		public ATaskConnectUser(OnHabitsAPIResult callback, HostConfig config, View btnClicked) {
			this.callback =callback;
			this.config=config;
			this.btnClicked=btnClicked;
		}
		@Override
		protected void onPreExecute() {
			if(this.btnClicked!=null)
				this.btnClicked.setEnabled(false);

			this.callback.onPreResult();
		}

		@Override
		protected Void doInBackground(String... us) {
			AuthUser con = new AuthUser(callback,config,us[0],us[1]);
			Answer as = con.getData();
			if(as!=null)
				as.parse();
			return null;
		}
		@Override
		protected void onPostExecute(Void arg) {
			if(this.btnClicked!=null)
				this.btnClicked.setEnabled(true);

		}

	}



	private class ATaskRevive extends AsyncTask<Void,Void,Void> {
		private final HostConfig config;
		private final OnHabitsAPIResult callback;

		public ATaskRevive(OnHabitsAPIResult callback, HostConfig config) {
			this.callback=callback;
			this.config=config;
		}
		@Override
		protected void onPreExecute() {
			this.callback.onPreResult();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			ReviveUser revive = new ReviveUser(callback,config);
			Answer as = revive.getData();
			if(as!=null)
				as.parse();
			return null;
		}

	}


	private class ATaskChangeTimeZone extends AsyncTask<Integer,Void,Void>{
		private final OnHabitsAPIResult callback;
		private final HostConfig config;

		public ATaskChangeTimeZone(OnHabitsAPIResult callback, HostConfig config) {
			this.config = config;
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(Integer... offsets) {
			WebServiceInteraction changeTimeZone = new PutTimeZone(callback, config,offsets[0]);
			try {
				WebServiceInteraction.Answer ans = changeTimeZone.getData();
				ans.parse();//parse the object using the callback
				System.out.println("finished!");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}*/
}
