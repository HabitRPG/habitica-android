package com.habitrpg.android.habitica;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskCreationCallback;
import com.habitrpg.android.habitica.callbacks.TaskDeletionCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.TaskUpdateCallback;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/*
 * TODO: PAS DE QRCODE DANS LES PARAMETRE SUR ANDROID<3.0
 */
public class MainActivity extends ActionBarActivity implements OnTaskCreationListener, HabitRPGUserCallback.OnUserReceived,
        TaskScoringCallback.OnTaskScored, TaskCreationCallback.OnHabitCreated, TaskUpdateCallback.OnHabitUpdated, TaskDeletionCallback.OnTaskDeleted {
    private static final String TAG = "MainActivity";
	private MyPagerAdapter mPagerAdapter;
	private ViewPager mPager;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ImageView mUserPicture;
	private TextView mUserExp;
	private TextView mUserHealth;
	private ProgressBar mProgressBar;
	private EditText mCreateTaskText;
	private ImageButton mCreateTask;
	private ImageButton mEditTask;
	private View mUserHealthBar;
	private View mUserExpBar;
	private int barWidth=0;
	private HabitRPGUser user = new HabitRPGUser();
	private APIHelper mAPIHelper;
	private ActionBarDrawerToggle mDrawerToggle;
	private View mLeftDrawer;
	private List<String> selectedTags = new ArrayList<String>();
	private TagAdapter mTagAdapter;
    private HostConfig hostConfig;
    private int nbRequests=0;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_with_drawer);

		this.hostConfig = PrefsActivity.fromContext(this);
		if(hostConfig==null|| hostConfig.getApi()==null || hostConfig.getApi().equals("") || hostConfig.getUser() == null ||hostConfig.getUser().equals("")) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finish();
		} else {
            if(this.isThisFirstTimeSinceUpdate()) {
                ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(this);
                _ChangelogDialog.show();
            }
        }
		/*
		 * Retrieve all the views
		 */
		this.mUserExpBar = this.findViewById(R.id.V_XPBar);
		this.mUserHealthBar = this.findViewById(R.id.V_HPBar);
		this.mEditTask = (ImageButton) this.findViewById(R.id.BT_editTask);
		this.mUserExp = (TextView) this.findViewById(R.id.TV_XP);
		this.mUserHealth = (TextView) this.findViewById(R.id.TV_HP);
		this.mProgressBar = (ProgressBar) this.findViewById(R.id.PB_AsyncTask);
		this.mUserPicture = (ImageView) this.findViewById(R.id.IMG_ProfilePicture);
		this.mCreateTask = (ImageButton) this.findViewById(R.id.BT_task_new);
		this.mCreateTaskText = (EditText) this.findViewById(R.id.ET_addTask);

		this.mCreateTask.setEnabled(false);
		this.mCreateTaskText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mCreateTaskText.getText() == null || mCreateTaskText.getText().toString().equals(""))
					mCreateTask.setEnabled(false);
				else
					mCreateTask.setEnabled(true);
			}
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		this.mCreateTask.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addTask(mCreateTaskText.getText().toString());
				hideKeyboard(mCreateTaskText);
			}
		});
		 this.mCreateTaskText.setOnEditorActionListener(new OnEditorActionListener() {
			 @Override
			 public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				 if (actionId == EditorInfo.IME_ACTION_DONE) {
					 addTask(mCreateTaskText.getText().toString());
					 hideKeyboard(mCreateTaskText);
					 return true;
				 }
				 return false;
			 }
		 });
		 this.mEditTask.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 Bundle b = new Bundle();
				 b.putString("type", mPager != null ? getTaskType(mPager.getCurrentItem()) : HabitType.habit.toString());
				 b.putString("text", mCreateTaskText.getText().toString());
				 DialogFragment dialog = new AddTaskDialog();
				 dialog.setArguments(b);
				 dialog.show(getSupportFragmentManager(), "AddTaskDialog");
			 }
		 });
		FragmentManager fm = super.getSupportFragmentManager();
		Fabric.with(this, new Crashlytics());
        mPagerAdapter = new MyPagerAdapter(fm);
        mPager = (ViewPager) findViewById(R.id.home_pannels_pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(0);
		this.mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int pos) {
                switch(pos) {
                    case 0://
                        mCreateTaskText.setHint(R.string.create_habit_hint);
                        break;
                    case 1://Daily
                        mCreateTaskText.setHint(R.string.create_daily_hint);
                        break;
                    case 2://To Do
                        mCreateTaskText.setHint(R.string.create_todo_hint);
                        break;
                    case 3://Reward
                        mCreateTaskText.setHint(R.string.create_reward_hint);
                        break;
                }
            }
            @Override public void onPageScrolled(int i, float v, int i2) {}
			@Override public void onPageScrollStateChanged(int i) {}
		});
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftDrawer = findViewById(R.id.left_drawer);
		mDrawerList = (ListView) findViewById(R.id.LV_drawer_tag);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
		) {
			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {	}
			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}



	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTagAdapter = new TagAdapter(this);

		mDrawerList.setAdapter(mTagAdapter);

        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView parent, View view, int position, long id) {
                selectTag(position, view);
            }
        });
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		//HostConfig config = PrefsActivity.fromContext(this);
		this.mAPIHelper = new APIHelper(this, hostConfig);
        this.onPreResult();
		mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));


	}

	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}
	private String getTaskType(int pos) {
		return pos== 1? HabitType.daily.toString()
				: pos==0 ? HabitType.habit.toString()
				: pos==3 ? HabitType.reward.toString()
				: HabitType.todo.toString();
	}
	private void notifyDataChanged() {
		if(this.barWidth==0) {
			this.barWidth =  this.findViewById(R.id.LL_header).getWidth();
		}
		this.mUserHealth.setText("" + (int) (user.getStats().getHp().doubleValue()) + "/" + user.getStats().getMaxHealth() + " " + getString(R.string.HP_default));
		this.mUserExp.setText("" + (int) user.getStats().getExp().doubleValue() + "/" + (int) (user.getStats().getToNextLevel()+user.getStats().getExp()) + " " + getString(R.string.XP_default));

		double hppct = user.getStats().getHp() / user.getStats().getMaxHealth();
		hppct*=this.barWidth;
		double xppct = user.getStats().getExp() / (user.getStats().getToNextLevel()+user.getStats().getExp());
		xppct*=this.barWidth;
		this.mUserHealthBar.setLayoutParams(new FrameLayout.LayoutParams((int) hppct, LayoutParams.MATCH_PARENT));
		this.mUserExpBar.setLayoutParams(new FrameLayout.LayoutParams((int) xppct, LayoutParams.MATCH_PARENT));
		this.getSupportActionBar().setTitle(createTitle(user));
        List<HabitItem> items = new ArrayList<>();
        items.addAll(user.getHabits());
        items.addAll(user.getDailys());
        items.addAll(user.getRewards());
        items.addAll(user.getTodos());
		mPagerAdapter.notifyFragments(items);
		((DailyFragment) mPagerAdapter.getItem(1)).setDayStart(user.getPreferences().getDayStart());
		((RewardFragment) mPagerAdapter.getItem(3)).setGold(user.getStats().getGp());

		//this.username_TV.setText(user.getName());
		this.mUserPicture.setImageBitmap(new UserPicture(user, this	).draw());

		if(this.mDrawerList!=null) {
			this.mTagAdapter.updateTags(user.getTags());
			this.mTagAdapter.notifyDataSetChanged();
		}
        if(user.getStats().getHp()==0) {
            showRevivePopUp();
        }

	}

	private String createTitle(HabitRPGUser user) {
		//return user.getName()!=null ? user.getName() + " - lvl" + user.getLvl() : getString(R.string.app_name);
        return user.getProfile().getName();
	}

	private void notifyUser(double xp, double hp, double gold,
			double lvl, double delta) {
		StringBuilder message = new StringBuilder();
		boolean neg = false;
		if(lvl > user.getStats().getLvl()) {
			message.append(getString(R.string.lvlup));
			//If user lvl up, we need to fetch again the data from the server...
            this.onPreResult();
			this.mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
			user.getStats().setLvl((int) lvl);
			Crouton.makeText(this, message, Style.CONFIRM).show();
		} else {
			if(xp>user.getStats().getExp()) {
				message.append("+ ").append(round(xp -user.getStats().getExp(),2)).append(" XP");
				user.getStats().setExp(xp + user.getStats().getExp());
			}
			if(hp != user.getStats().getHp()) {
				neg=true;
				message.append("- ").append(round(user.getStats().getHp() - hp,2)).append(" HP");
				user.getStats().setHp(hp);
			}
			if(gold > user.getStats().getGp()) {
				message.append("+ ").append(round(gold - user.getStats().getGp(),2)).append(" GP");
				user.getStats().setGp(gold);
			} else if(gold < user.getStats().getGp()) {
				neg=true;
				message.append("- ").append(round(user.getStats().getGp() - gold,2)).append(" GP");
                user.getStats().setGp(gold);
			}
			Crouton.makeText(this, message, neg ? Style.ALERT : Style.INFO).show();
		}
		notifyDataChanged();
	}

	private void addTask(String text) {
		HabitItem item;
		Log.d(TAG + "CurrentItem", this.mPager.getCurrentItem() + "");
		switch(this.mPager.getCurrentItem()) {
		case 1:
			item = new Daily(null, "", null, text, (double) 0, false, null);
			break;
		case 2:
			item = new ToDo(null, "", null, text, 0, false, "");
			break;
		case 3:
			item = new Reward(null, "", null, text, 20);
			break;
		default:
			item = new Habit(null, "", null, text, 0, true, true);

		}
		this.onTaskCreation(item,false);
	}
	private void hideKeyboard(EditText v) {
		v.setText("");
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

	}


	@Override
	public void onTaskCreation(HabitItem task, boolean editMode) {
        this.onPreResult();
		if(!editMode) {
			List<String> tagsIds = this.selectedTags;
			task.setTags(tagsIds);
			this.mAPIHelper.createUndefNewTask(task, new TaskCreationCallback(this));
		} else {
			this.mAPIHelper.uprateUndefinedTask(task, new TaskUpdateCallback(this));
		}
	}

	@Override
	public void onTaskCreationFail(String message) {
		Crouton.makeText(this, message, Style.ALERT).show();
	}
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch(item.getItemId()) {

			case R.id.action_settings:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.action_refresh:
                this.onPreResult();
	        	this.mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
	        	break;
			case R.id.action_logout:
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(getString(R.string.SP_APIToken), "")
						.putString(getString(R.string.SP_userID), "")
						.commit();
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();

		}
		return super.onOptionsItemSelected(item);
	}
    public void onPreResult() {
        mProgressBar.setVisibility(View.VISIBLE);
        this.nbRequests++;
    }
    private void afterResults() {
        this.nbRequests--;
        if(this.nbRequests<=0)
            mProgressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.user = user;
        this.notifyDataChanged();
        afterResults();
//        checkTimeZone(habitRPGUser.getTimeZoneOffset());
    }

    @Override
    public void onUserFail() {
        //TODO
        Crouton.makeText(this,R.string.unknown_error,Style.ALERT).show();
        afterResults();

    }

    @Override
    public void onTaskDataReceived(TaskDirectionData data) {
        notifyUser(data.getExp(), data.getHp(),data.getGp(),data.getLvl(),data.getDelta());
        afterResults();
        Log.d(TAG, "Successfully tasked up/down");

    }

    @Override
    public void onTaskScoringFailed() {
        Log.d(TAG, "Failed tasking up/down");
        afterResults();

    }

    @Override
    public void onTaskCreated(HabitItem habit) {
        if(habit instanceof Habit) {
            user.getHabits().add((Habit) habit);
        } else if(habit instanceof ToDo) {
            user.getTodos().add((ToDo) habit);
        } else if(habit instanceof Daily) {
            user.getDailys().add((Daily) habit);
        } else {
            user.getRewards().add((Reward) habit);
        }
        Crouton.makeText(MainActivity.this, MainActivity.this.getString(R.string.new_task_added_message), Style.INFO).show();
        notifyDataChanged();
        afterResults();

    }

    @Override
    public void onTaskCreationFail() {
        Log.d(TAG, "Failed creating task");
        afterResults();
    }

    @Override
    public void onTaskUpdated(HabitItem habit) {
        if(habit instanceof Habit) {
            updateHabit((Habit) habit);
        } else if(habit instanceof ToDo) {
            updateToDo((ToDo) habit);
        } else if(habit instanceof Daily) {
            updateDaily((Daily) habit);
        } else {
            updateReward((Reward) habit);
        }
        Crouton.makeText(MainActivity.this, MainActivity.this.getString(R.string.task_edited), Style.INFO).show();
        notifyDataChanged();
        afterResults();
    }

    @Override
    public void onTaskUpdateFail() {
        Log.d(TAG, "Failed updating task");
        afterResults();

    }


    @Override
    public void onTaskDeleted(HabitItem deleted) {
        Crouton.makeText(MainActivity.this, MainActivity.this.getString(R.string.task_deleted, deleted.getText()), Style.INFO).show();
        if(deleted instanceof Habit) {
            removeHabit((Habit) deleted);
        } else if(deleted instanceof ToDo) {
            removeToDo((ToDo) deleted);
        } else if(deleted instanceof Daily) {
            removeDaily((Daily) deleted);
        } else {
            removeReward((Reward) deleted);
        }
        afterResults();
        notifyDataChanged();

    }


    @Override
    public void onTaskDeletionFail() {

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
		private SparseArray<CardFragment>  fragments = new SparseArray<CardFragment>();
		List<HabitItem> items;
        public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			Log.v(TAG + "_FP", "Reinstanciating items");
			CardFragment h = new HabitFragment();
			CardFragment d = new DailyFragment();
			CardFragment t = new ToDoFragment();
			CardFragment r = new RewardFragment();
			fragments.put(0,h);
			fragments.put(1,d);
			fragments.put(2,t);
			fragments.put(3,r);
		}
        public int getCount() {
            return 4;
        }
		@Override
		public CardFragment getItem(int position) {
			Log.v(TAG + "_FP", "instantiating fragment " + position);
			return fragments.get(position);
		}
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            CardFragment fragment = (CardFragment) super.instantiateItem(container, position);
            Log.v(TAG + "_FP", "adding fragment" + position);
            fragments.put(position, fragment);
            if(items!=null) {
            	fragment.onChange(items);
				fragment.onTagFilter(selectedTags);
			}
			return fragment;
        }
        /*@Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        	Log.v(TAG + "_FragmentPager", "removing fragment" + position);
            fragments.remove(position);
            super.destroyItem(container, position, object);
        }*/


		public void notifyFragments(List<HabitItem> items) {
			this.items = items;
			for(int i=0;i<=3;i++) { // f : fragments) {
				CardFragment f = fragments.get(i);
				if(f!=null) {
					f.onChange(items);
					f.onTagFilter(selectedTags);
				} else {
					Log.w(TAG + "_Notify", "no fragment " + i );
				}
				this.notifyDataSetChanged();
			}
		}

		public void filterFragments(List<String> tags) {
			for(int i=0;i<=3;i++) { // f : fragments) {
				CardFragment f = fragments.get(i);
				if(f!=null)
					f.onTagFilter(tags);
				else
					Log.w(TAG + "_Filters", "no fragment " + i );
				this.notifyDataSetChanged();
			}

		}
	}

	/** Swaps fragments in the main content view */
	private void selectTag(int position, View viewClicked) {
		// Create a new fragment and specify the planet to show based on position
		String tagId = ((TagAdapter)mDrawerList.getAdapter()).getTagId(position);
		this.mDrawerList.setSelection(position);
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
			CheckedTextView cbtv = (CheckedTextView) viewClicked;
			cbtv.setChecked(!cbtv.isChecked());
		}
		if(this.selectedTags==null) {
			selectedTags = new ArrayList<String>();
		}
		if(!selectedTags.contains(tagId)) {
			selectedTags.add(tagId);
			mDrawerList.setItemChecked(position, true);
		} else {
			selectedTags.remove(tagId);
			mDrawerList.setItemChecked(position, false);
		}
		mPagerAdapter.filterFragments(selectedTags);
		mDrawerLayout.closeDrawer(mLeftDrawer);
	}




	static public Double round(Double value, int n) {
		double r = (Math.round(value.doubleValue() * Math.pow(10, n))) / (Math.pow(10, n));
		return Double.valueOf(r);
	}
	public static int getTimeZoneOffset(Calendar cal) {
		Date date = cal.getTime();
		TimeZone tz = cal.getTimeZone();
		//Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
		long msFromEpochGmt = date.getTime();
		int offsetFromUTC = tz.getOffset(msFromEpochGmt)/1000/60;
		return (-1)*offsetFromUTC;
	}

    public Habit getHabit(int pos) {
        return user.getHabits().get(pos);
    }
    public ToDo getTodo(int pos) {
        return user.getTodos().get(pos);
    }

    public Daily getDaily(int pos) {
        return user.getDailys().get(pos);
    }

    public Reward getReward(int pos) {
        return user.getRewards().get(pos);
    }


    void updateHabit(Habit item) {
        List<Habit> habits = user.getHabits();
        for(int i=0;i<habits.size();i++) {
            if(habits.get(i).getId().equals(item.getId())) {
                habits.set(i, item);
            }
        }
    }
    void updateToDo(ToDo item) {
        List<ToDo> items = user.getTodos();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
            }
        }
    }
    void updateDaily(Daily item) {
        List<Daily> items = user.getDailys();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
            }
        }
    }
    void updateReward(Reward item) {
        List<Reward> items = user.getRewards();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
            }
        }
    }

    private void removeHabit(Habit item) {
        List<Habit> items = user.getHabits();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.remove(i);
            }
        }
    }

    private void removeToDo(ToDo item) {
        List<ToDo> items = user.getTodos();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.remove(i);
            }
        }
    }

    private void removeDaily(Daily item) {
        List<Daily> items = user.getDailys();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.remove(i);
            }
        }
    }
    private void removeReward(Reward item) {
        List<Reward> items = user.getRewards();
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getId().equals(item.getId())) {
                items.remove(i);
            }
        }
    }
/*	OnHabitsAPIResult callback = new OnHabitsAPIResult() {
		Handler mainHandler;
		private int nbRequests=0;

		@Override
		public void onError(HabitRPGException error) {
			final HabitRPGException err = error;
			mainHandler = new Handler(getMainLooper());
			Runnable myRunnable = new Runnable(){
				public void run() {
					afterResults();
					String myMessage=err!=null ? err.getMessage() : null;
					if(myMessage == null)
						myMessage= getString(R.string.unknown_error);
					Crouton.makeText(MainActivity.this, myMessage, Style.ALERT).show();
				}
			};
			mainHandler.post(myRunnable);

		}

		@Override
		public void onUserItemsReceived(UserLook.UserItems userItems, Reward.SpecialReward iBought) {
			final UserLook.UserItems uItems = userItems;
            final Reward.SpecialReward itemBought = iBought;
			mainHandler = new Handler(getMainLooper());
			Runnable myRunnable = new Runnable(){
				public void run() {
					//user.addTask(task);
					if(user!=null) {
						user.getLook().setItems(uItems);
                        user.setGp(user.getGp()-itemBought.getValue());
						Crouton.makeText(MainActivity.this, MainActivity.this.getString(R.string.item_bought, itemBought.getText()), Style.INFO).show();
						notifyDataChanged();
						afterResults();
					}
				}
			};
			mainHandler.post(myRunnable);

		}

	};*/

	private void showRevivePopUp() {
		//final SpannableString mess = new SpannableString(getString(R.string.helpString));
		//Linkify.addLinks(mess, Linkify.ALL);
		AlertDialog d =new AlertDialog.Builder(this)
				.setTitle(R.string.user_dead_title)
				.setMessage(R.string.user_dead_mess).setCancelable(false)
				.setPositiveButton(R.string.string_revive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mAPIHelper.revive();
						dialog.dismiss();
					}

				}).create();
		d.show();
		((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		((TextView)d.findViewById(android.R.id.message)).setTextSize(13);
	}

	private void checkTimeZone(int currentUserTimeZone) {
		Calendar cal = Calendar.getInstance();
		int offset = getTimeZoneOffset(cal);
		if(currentUserTimeZone!= offset) {
			mAPIHelper.changeTimeZone(offset);
			Log.w(TAG + "_Offset","ChangingTimeZone");
		}

	}

    private boolean isThisFirstTimeSinceUpdate() {
        boolean res;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String version = "";
        try {
            PackageInfo _info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version = _info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(!version.equals(prefs.getString(getString(R.string.SP_last_seen_version), "OMG"))) {
            res=true;
            prefs.edit().putString(getString(R.string.SP_last_seen_version), version).commit();
        } else {
            res=false;
        }
        return res;

    }


}
