package com.magicmicky.habitrpgmobileapp;

import com.magicmicky.habitrpgmobileapp.callbacks.HabitRPGUserCallback;
import com.magicmicky.habitrpgmobileapp.callbacks.TaskDeletionCallback;
import com.magicmicky.habitrpgmobileapp.callbacks.TaskScoringCallback;
import com.magicmicky.habitrpgwrapper.lib.HabitRPGInteractor;
import com.magicmicky.habitrpgwrapper.lib.api.HabitItemCallback;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.api.TaskDirectionCallback;
import com.magicmicky.habitrpgwrapper.lib.api.UserCallback;
import com.magicmicky.habitrpgwrapper.lib.api.VoidCallback;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;

import android.content.Context;
import android.util.Log;
import android.view.View;

import retrofit.Callback;


public class APIHelper {

    private static final String TAG = "ApiHelper";
    private Context mContext;
	//private OnHabitsAPIResult mResultListener;
	//private HostConfig mConfig;
    private HabitRPGInteractor hrpg;
	public APIHelper(Context c, HostConfig cfg) {
		this.mContext = c;
	    this.hrpg = new HabitRPGInteractor(cfg.getApi(), cfg.getUser(), new Server(cfg.getAddress()));
    }

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
        this.hrpg.createItem(habit, callback);
	}
    public void createNewTask(ToDo toDo, Callback<ToDo> callback) {
        this.hrpg.createItem(toDo, callback);
    }
    public void createNewTask(Daily d,Callback<Daily> callback) {
        this.hrpg.createItem(d, callback);
    }
    public void createNewTask(Reward r,Callback<Reward> callback) {
        this.hrpg.createItem(r, callback);
    }

	public void retrieveUser(HabitRPGUserCallback callback) {
		this.hrpg.getUser(callback);
	}

	public void updateTaskDirection(String id, String direction, TaskScoringCallback callback) {
		TaskDirection td;
        if(direction.equals(TaskDirection.up.toString())) {
            td = TaskDirection.up;
        } else {
            td = TaskDirection.down;
        }
        this.hrpg.postTaskDirection(id,td, callback);
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
        this.hrpg.connectUser(auth, callback);
	}

	public void deleteTask(HabitItem item, TaskDeletionCallback cb) {
		this.hrpg.deleteItem(item.getId(), cb);
    }
    public void updateTask(Daily item, Callback<Daily> cb) {
        this.hrpg.updateItem(item.getId(), item, cb);
    }
    public void updateTask(Habit item, Callback<Habit> cb) {
        this.hrpg.updateItem(item.getId(), item, cb);
    }
    public void updateTask(ToDo item, Callback<ToDo> cb) {
        this.hrpg.updateItem(item.getId(), item, cb);
    }
    public void updateTask(Reward item, Callback<Reward> cb) {
        this.hrpg.updateItem(item.getId(), item, cb);
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
