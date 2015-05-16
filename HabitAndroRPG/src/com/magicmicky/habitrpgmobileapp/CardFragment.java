package com.magicmicky.habitrpgmobileapp;


import java.util.ArrayList;
import java.util.List;


import com.magicmicky.habitrpgmobileapp.callbacks.TaskDeletionCallback;
import com.magicmicky.habitrpgmobileapp.callbacks.TaskScoringCallback;
import com.magicmicky.habitrpgmobileapp.prefs.PrefsActivity;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public abstract class CardFragment extends Fragment implements OnTasksChanged {
	private static final String TAG = "CardFragment";
	/**
	 * If we receive a notification of tasks (by the @{code OnTasksChanged} implementation) <br/>
	 * Before the fragment is shown, we need to save the tasks.
	 */
	private List<HabitItem> tmpTasks= new ArrayList<HabitItem>();
	protected MyAdapter mAdapter;
	private ListView mListView;
	private APIHelper mAPIHelper;
	private List<String> tmpTags = new ArrayList<String>();
	private ActionMode mActionMode;
    private static final String API_KEY ="b89ef880-7e07-4d13-8a5f-b6be25437fd8";
    private static final String USER_KEY ="710f41f1-4113-4d8a-9714-79a84edd6175";

		@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "oncreate call");
    	//setRetainInstance(true);
    	View view = inflater.inflate(R.layout.card_ex, container,false);
		mListView = (ListView) view.findViewById(R.id.task_list_view);


		((TextView) view.findViewById(R.id.title)).setText(this.getTitle());
		if( mListView.getAdapter() == null || this.mAdapter ==null) {
			this.mAdapter = this.instantiateAdapter(this.getActivity());
			Log.d(TAG + this.getTitle(),"Adapter was null... Not anymore");
			mListView.setAdapter(mAdapter);
		}
			this.mAdapter = (MyAdapter) mListView.getAdapter();

		if(tmpTasks.size() != 0) {
			Log.v(TAG + "tmpdata" + this.getTitle(), "Temp data (" + tmpTasks.size() + " tasks) already created. Sending them to the adapter...");
			this.mAdapter.updateTasks(tmpTasks);
			this.mAdapter.notifyDataSetChanged();
			tmpTasks.clear();

		} else {
			Log.v(TAG + "tmpdata" + this.getTitle(), "No temp data...");
		}

		if(tmpTags.size()!=0) {
			Log.v(TAG+"tmpdata_tags"+ this.getTitle(),"Temp ("+tmpTags.size() +" tags) already created. Sending them to the adapter...");
			this.mAdapter.filterTasks(tmpTags);
			this.mAdapter.notifyDataSetChanged();
			tmpTags.clear();
		} else {
			Log.v(TAG + "tmpdata_tags" + this.getTitle(), "No temp tags...");
		}
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				if (mActionMode != null) {
					toggleTaskSelection(i);
					return true;
				}

				// Start the CAB using the ActionMode.Callback defined above
				mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
				toggleTaskSelection(i);
				return true;
			}
		});
    	return view;
    }
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser) {

		}
		else {
			if(this.mActionMode!=null) {
				this.mActionMode.finish();
			}
		}

	}

	private void toggleTaskSelection(int pos) {
		boolean new_status = !mAdapter.isItemChecked(pos);
		mListView.setItemChecked(pos, new_status);
		mAdapter.setItemChecked(pos, new_status);
		int nbItemChecked = mAdapter.getNbItemChecked();
		if (mActionMode != null) {
			if (nbItemChecked != 0) {
				mActionMode.setTitle(getString(R.string.cab_selected, nbItemChecked));
			} else {
				mActionMode.finish();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
    	HostConfig cfg = PrefsActivity.fromContext(this.getActivity());
        this.mAPIHelper = new APIHelper(getActivity(), cfg);

	}
	protected abstract HabitType getTaskType();
	/**
	 * Filter the data depending of the type of the inherited class
	 * @param items the items to filter
	 * @return the list of {@link HabitItem} filtered
	 */
	protected abstract List<HabitItem> filterData(List<HabitItem> items);

	protected abstract String getTitle();
	public abstract MyAdapter instantiateAdapter(Context c);
	/**
	 * @return the callback
	 */
	/*public OnTasksChanged getCallback() {
		return this;
	}*/
	@Override
	public void onTagFilter(List<String> tags) {
		Log.d(TAG + "tmpdata_tagfilter"+ this.getTitle(), "Filtering tasks with " + tags.size() + " tag(s)");
		if(this.mAdapter ==null) {
		/*	Context ac = this.getActivity();
			if(ac != null) {
				Log.d(TAG +"tmpdata_tagfilter"+ this.getTitle(),"Adapter not set up: setting it up + filtering tasks");
				this.mAdapter = instantiateAdapter(ac);
				this.mAdapter.filterTasks(tags);
				this.mAdapter.notifyDataSetChanged();

			} else {
				Log.d(TAG +"tmpdata_tagfilter"+ this.getTitle(), "Unable to create the adapter. Temping tags data");
			}*/
			Log.d(TAG +"tmpdata_tagfilter"+ this.getTitle(), "Unable to create the adapter. Temping tags data");

			this.tmpTags.clear();
			this.tmpTags.addAll(tags);

		} else {
			Log.d(TAG +"tmpdata_tagfilter"+ this.getTitle(),"Adapter set up: filtering tasks");
			this.mAdapter.filterTasks(tags);
			this.mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onChange(List<HabitItem> items) {
		Log.d(TAG +"_task","Changing data (" + items.size() + " items)");
		if(this.mAdapter ==null) {
			this.tmpTasks.clear();
			this.tmpTasks.addAll(filterData(items));
			Log.d(TAG + "tmpdata" + this.getTitle() +"_callbackTaskChanged","The adapter isn't set up yet. Saving "+ tmpTasks.size() + " tasks in the temp");
			/*Context ac = this.getActivity();
			if(ac != null) {
				Log.d(TAG + "tmpdata" + this.getTitle() +"_callbackTaskChanged","Adapter created: sending temp data, deleting temp.");
				this.mAdapter = instantiateAdapter(ac);
				this.mAdapter.updateTasks(this.tmpTasks);
				this.mAdapter.notifyDataSetChanged();
				this.tmpTasks.clear();
			} else {
				Log.d(TAG + "tmpdata" + this.getTitle() +"_callbackTaskChanged", "Unable to create the adapter. What about temp data?");
			}*/

		} else {
			this.mAdapter.updateTasks(filterData(items));
			this.mAdapter.notifyDataSetChanged();
		}
	}


    /**
	 * The Adapter class that deals with the different data
	 * @author MagicMicky
	 *
	 */
	protected abstract class MyAdapter extends BaseAdapter {
		private List<HabitItem> tasks;
		private List<HabitItem> filteredTasks;
		protected Context c;
		private boolean isFiltered;
		private List<String> tags;
		private int checkedItem=-1;

		public MyAdapter(Context c, List<HabitItem> tasks) {
			Log.d("ADAPTER", "adapter called");
			this.tasks = tasks;
			this.c=c;
			this.isFiltered=false;
			this.filteredTasks = new ArrayList<HabitItem>();
			this.tags = new ArrayList<String>();
		}
		public void updateTasks(List<HabitItem> tasks) {
			this.tasks.clear();
			this.tasks.addAll(tasks);
			if(isFiltered)
				this.filterTasks(tags);
		}
		public List<HabitItem> getItems() {
			return isFiltered ? filteredTasks : tasks;
		}
		@Override
		public int getCount() {
			return isFiltered ? filteredTasks.size() : tasks.size();
		}

		@Override
		public HabitItem getItem(int pos) {
			return isFiltered ? this.filteredTasks.get(pos) : this.tasks.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}
		public String getIdString(int id) {
            Log.d(TAG, "id " + id + " --> " + getItem(id).getText() + " (" + getItem(id).getId() + ")");
			return this.getItem(id).getId();
		}
		@Override
		public abstract View getView(int position, View convertView, ViewGroup parent);

		protected OnClickListener getOnClickListener() {
			return new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v.getParent()!=null) {
						String direction = upOrDown(v, getItem(((View) v.getParent()).getId()));
						String id = getIdString(((View) v.getParent()).getId());
                        ((MainActivity) getActivity()).onPreResult();
                        Log.d("OMG", "id : " + id + " - " + direction);
						mAPIHelper.updateTaskDirection(id,direction, new TaskScoringCallback((MainActivity) getActivity()));
					} else {
                        Crouton.makeText(getActivity(), getActivity().getString(R.string.unknown_error), Style.ALERT).show();
                    }
				}
			};
		}

		protected OnClickListener buyItem = new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v.getParent()!=null) {

						HabitItem it = getItem(((View) v.getParent()).getId());
						if(it instanceof Reward.SpecialReward) {
							mAPIHelper.buyItem(((Reward.SpecialReward) it),v);
						} else {
                            Crouton.makeText(getActivity(), getActivity().getString(R.string.unknown_error), Style.ALERT).show();
                        }
				}
			}
		};

		protected String upOrDown(View v, HabitItem h) {
			return v.getId() == R.id.minus ? "down" : "up";
		}

		public void filterTasks(List<String> tagsIds) {
			//Unfiltering from a tag already filtered = the other tasks aren't there !!!!
			if(tagsIds.size()>0) {
				Log.v(TAG + "_tags", "Start filtering with " + tagsIds.size() + " tags");
				this.isFiltered=true;
				this.tags.clear();
				this.tags.addAll(tagsIds);
				List<HabitItem> tsks = new ArrayList<HabitItem>();
				for(HabitItem it: this.tasks) {
					if(it.getTags().containsAll(tagsIds)) {
						tsks.add(it);
					}
				}
				this.filteredTasks.clear();
				this.filteredTasks.addAll(tsks);
			} else {
				Log.v(TAG + "_tags", "Stop filtering");
				this.isFiltered=false;
				this.tags.clear();
				this.filteredTasks.clear();
			}
		}

		public void setItemChecked(int i, boolean new_status) {
			if(new_status)
				checkedItem = i;
			else
				checkedItem = -1;
		}
		public boolean isItemChecked(int i) {
			return checkedItem == i;
		}

		public int getNbItemChecked() {
			return checkedItem == -1 ? 0 : 1;
		}

		public int getItemCheckedPos() {
			return checkedItem;
		}
	}
	/**
	 * Get the color resources depending on a certain score
	 * @param d the score
	 * @return the color resource id
	 */
	protected int getColorRes(double d) {
		if(d<-20)
			return R.drawable.triangle_worst;
		if(d<-10)
			return R.drawable.triangle_worse;
		if(d<-1)
			return R.drawable.triangle_bad;
		if(d<5)
			return R.drawable.triangle_neutral;
		if(d<10)
			return R.drawable.triangle_better;
		return R.drawable.triangle_best;
	}

	/**
	 * The callback used by the Contextual ActionBar
	 */
	private ActionMode.Callback mActionModeCallback = new android.support.v7.view.ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
			// Inflate our menu from a resource file
			actionMode.getMenuInflater().inflate(R.menu.select_task, menu);

			// Return true so that the action mode is shown
			return true;
		}

		@Override
		public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
			// As we do not need to modify the menu before displayed, we return false.
			return false;
		}

		@Override
		public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
			// Similar to menu handling in Activity.onOptionsItemSelected()
			switch (menuItem.getItemId()) {
				case R.id.action_edit:
					HabitItem taskToUpdate = mAdapter.getItem(mAdapter.getItemCheckedPos());
					Bundle b = new Bundle();
					b.putString("type", getTaskType().toString());
                    b.putInt("pos", mAdapter.getItemCheckedPos());
					DialogFragment dialog = new AddTaskDialog();
					dialog.setArguments(b);
					dialog.show(getChildFragmentManager(), "AddTaskDialog");
					mActionMode.finish();
					return true;
				case R.id.deleteConfirmMenuBT:
					HabitItem taskToDel = mAdapter.getItem(mAdapter.getItemCheckedPos());
                    ((MainActivity) getActivity()).onPreResult();
					mAPIHelper.deleteTask(taskToDel, new TaskDeletionCallback((MainActivity) getActivity(), taskToDel));
					mActionMode.finish();
					return true;
			}

			return false;
		}

		@Override
		public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {
			mActionMode = null;
			SparseBooleanArray checked = mListView.getCheckedItemPositions();
			for(int i=0;i<checked.size();i++) {
				if(checked.valueAt(i)) {
					mListView.setItemChecked(checked.keyAt(i),false);
					mAdapter.setItemChecked(checked.keyAt(i), false);
				}
			}
		}
	};

}
