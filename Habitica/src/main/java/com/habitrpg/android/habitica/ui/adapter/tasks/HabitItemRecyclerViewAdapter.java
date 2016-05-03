package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.data5tream.emojilib.EmojiTextView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.DailyItemCardBinding;
import com.habitrpg.android.habitica.databinding.HabitItemCardBinding;
import com.habitrpg.android.habitica.databinding.RewardItemCardBinding;
import com.habitrpg.android.habitica.databinding.TodoItemCardBinding;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.habitrpg.android.habitica.events.TaskRemovedEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.FilterTasksByTagsCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.adapter.IReceiveNewEntries;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;


public class HabitItemRecyclerViewAdapter<THabitItem extends Task>
        extends RecyclerView.Adapter<HabitItemRecyclerViewAdapter.ViewHolder>
        implements IReceiveNewEntries {

    public interface IAdditionalEntries {
        void GetAdditionalEntries(IReceiveNewEntries callBack);
    }

    int layoutResource;
    private Class<ViewHolder<Task>> viewHolderClass;
    Integer displayedChecklist = null;
    String taskType;
    private ObservableArrayList<Task> filteredObservableContent;
    private ObservableArrayList<Task> observableContent;
    Context context;
    public int dailyResetOffset;

    private static final int TYPE_CELL = 1;
    private TagsHelper tagsHelper;
    private IAdditionalEntries additionalEntries;

    public HabitItemRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Class<ViewHolder<Task>> viewHolderClass, Context newContext, int dailyResetOffset) {
        this(taskType, tagsHelper, layoutResource, viewHolderClass, newContext, dailyResetOffset, null);
    }

    public HabitItemRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Class<ViewHolder<Task>> viewHolderClass,
                                        Context newContext, int dailyResetOffset, final IAdditionalEntries additionalEntries) {
        this.setHasStableIds(true);
        this.taskType = taskType;
        this.context = newContext;
        this.tagsHelper = tagsHelper;
        this.additionalEntries = additionalEntries;
        filteredObservableContent = new ObservableArrayList<>();

        this.loadContent(true);

        this.layoutResource = layoutResource;
        this.viewHolderClass = viewHolderClass;
        this.dailyResetOffset = dailyResetOffset;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        EventBus.getDefault().unregister(this);

    }

    @Subscribe
    public void onEvent(FilterTasksByTagsCommand cmd) {
        filter();
    }

    @Subscribe
    public void onEvent(TaskCheckedCommand evnt){
        if (!taskType.equals(evnt.Task.getType()))
            return;

        if(evnt.completed && evnt.Task.getType().equals("todo")){
            // remove from the list
            observableContent.remove(evnt.Task);
        }
        this.updateTask(evnt.Task);
        filter();
    }

    @Subscribe
    public void onEvent(TaskUpdatedEvent evnt) {
        if (!taskType.equals(evnt.task.getType()))
            return;
        this.updateTask(evnt.task);
        filter();
    }

    @Subscribe
    public void onEvent(TaskCreatedEvent evnt) {
        if (!taskType.equals(evnt.task.getType()))
            return;

        observableContent.add(0, evnt.task);
        filter();
    }

    @Subscribe
    public void onEvent(TaskRemovedEvent evnt) {
        Task taskToDelete = null;

        for(Task t : observableContent) {
            if(t.getId().equals(evnt.deletedTaskId)){
                taskToDelete = t;
                break;
            }
        }

        if(taskToDelete != null) {
            observableContent.remove(taskToDelete);
            filter();
        }
    }

    private void updateTask(Task task) {
        int i;
        for(i = 0; i < this.observableContent.size(); ++i) {
            if (observableContent.get(i).getId().equals(task.getId())) {
                break;
            }
        }
        if (i < observableContent.size()) {
            observableContent.set(i, task);
        }
    }

    private void filter() {
        if (this.tagsHelper.howMany() == 0) {
            filteredObservableContent = observableContent;
        } else {
            filteredObservableContent = new ObservableArrayList<>();
            filteredObservableContent.addAll(this.tagsHelper.filter(observableContent));
        }

        // Filter Due Dailies
        boolean showDueOnly = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("show_due", false);

        Log.i("MT: ", "showDueOnly: " + showDueOnly);

        if (showDueOnly) {
            ObservableArrayList<Task> filtered2 = new ObservableArrayList<>();
            filtered2.addAll(this.tagsHelper.filterDue(filteredObservableContent, dailyResetOffset));
            filteredObservableContent = filtered2;
        }

        // End Filter Due Dailies

        ((Activity) context).runOnUiThread(this::notifyDataSetChanged);
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            default:
                return TYPE_CELL;
        }
    }

    @Override
    public long getItemId(int position) {
        Task task = filteredObservableContent.get(position);
        return task.getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return filteredObservableContent.size();
    }

    @Override
    public ViewHolder<THabitItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_CELL: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(layoutResource, parent, false);

                switch (viewHolderClass.getSimpleName()) {
                    case "HabitViewHolder":
                        return new HabitItemRecyclerViewAdapter.HabitViewHolder(view);
                    case "DailyViewHolder":
                        return new HabitItemRecyclerViewAdapter.DailyViewHolder(view);
                    case "TodoViewHolder":
                        return new HabitItemRecyclerViewAdapter.TodoViewHolder(view);
                    case "RewardViewHolder":
                        return new HabitItemRecyclerViewAdapter.RewardViewHolder(view);
                }
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task item = filteredObservableContent.get(position);

        holder.bindHolder(item, position);

        if (this.displayedChecklist != null && ChecklistedViewHolder.class.isAssignableFrom(holder.getClass())) {
            ChecklistedViewHolder checklistedHolder = (ChecklistedViewHolder) holder;
            checklistedHolder.setDisplayChecklist(this.displayedChecklist == position);
        }
    }

    // todo use debounce

    // region ViewHolders

    public abstract class ViewHolder<HabitItem extends Task> extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected android.content.res.Resources resources;

        public HabitItem Item;

        @Bind(R.id.checkedTextView)
        TextView titleTextView;

        @Bind(R.id.notesTextView)
        TextView notesTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);

            ButterKnife.bind(this, itemView);

            //Re enable when we find a way to only react when a link is tapped.
            //this.notesTextView.setMovementMethod(LinkMovementMethod.getInstance());
            //this.titleTextView.setMovementMethod(LinkMovementMethod.getInstance());

            resources = itemView.getResources();
        }

        public void bindHolder(HabitItem habitItem, int position) {
            Item = habitItem;
            if (habitItem.notes == null || habitItem.notes.length() == 0) {
                notesTextView.setHeight(0);
            } else {
                notesTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }

        @Override
        public void onClick(View v) {
            if (v != itemView)
                return;

            TaskTappedEvent event = new TaskTappedEvent();
            event.Task = Item;

            EventBus.getDefault().post(event);
        }
    }

    public class HabitViewHolder extends ViewHolder<Task> {

        @Bind(R.id.btnPlus)
        Button btnPlus;

        @Bind(R.id.btnMinus)
        Button btnMinus;

        HabitItemCardBinding binding;

        public HabitViewHolder(View itemView) {
            super(itemView);


            binding = DataBindingUtil.bind(itemView);

            btnPlus.setClickable(true);
            btnPlus.setOnClickListener(this);

            btnMinus.setClickable(true);
            btnMinus.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            HabitScoreEvent event = new HabitScoreEvent();

            if (v == btnPlus) {
                event.Up = true;
                event.Habit = Item;

                EventBus.getDefault().post(event);
            } else if (v == btnMinus) {
                event.Habit = Item;

                EventBus.getDefault().post(event);
            } else super.onClick(v);
        }

        @Override
        public void bindHolder(Task habitItem, int position) {
            super.bindHolder(habitItem, position);
            binding.setHabit(habitItem);
        }
    }

    public class ChecklistedViewHolder extends ViewHolder<Task> implements CompoundButton.OnCheckedChangeListener {

        @Bind(R.id.checkBoxHolder)
        RelativeLayout checkboxHolder;

        @Bind(R.id.checkBox)
        CheckBox checkbox;

        @Bind(R.id.checklistView)
        LinearLayout checklistView;

        @Bind(R.id.checklistIndicatorWrapper)
        RelativeLayout checklistIndicatorWrapper;

        public Boolean displayChecklist;

        public ChecklistedViewHolder(View itemView) {
            super(itemView);
            checklistIndicatorWrapper.setOnClickListener(this);
            checkbox.setOnCheckedChangeListener(this);
			expandCheckboxTouchArea(checkboxHolder, checkbox);

        }

        @Override
        public void onClick(View v) {

            if (v == checklistIndicatorWrapper) {
                if (this.displayChecklist != null) {
                    this.setDisplayChecklist(!this.displayChecklist);
                } else {
                    this.setDisplayChecklist(true);
                }
                RecyclerView recyclerView = (RecyclerView)this.checklistView.getParent().getParent();
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(this.getAdapterPosition(), 15);
            } else {
                super.onClick(v);
            }

        }

        @Override
        public void bindHolder(Task habitItem, int position) {
            boolean itemChanged = Item != null && !Item.getId().equals(habitItem.getId());

            super.bindHolder(habitItem, position);
            Boolean isClickable = false;
            if (habitItem.getChecklist() != null && habitItem.getChecklist().size() > 0) {
                isClickable = true;
            }
            checklistIndicatorWrapper.setClickable(isClickable);

            if(itemChanged) {
                this.setDisplayChecklist(false);
            }
        }

        public void setDisplayChecklist(Boolean displayChecklist) {
            this.displayChecklist = displayChecklist;
            //This needs to be a LinearLayout, as ListViews can not be inside other ListViews.
            if (this.checklistView != null) {
                if (this.displayChecklist && this.Item.checklist != null) {
                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    for (ChecklistItem item : this.Item.checklist) {
                        LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.checklist_item_row, this.checklistView, false);
                        CheckBox checkbox = (CheckBox) itemView.findViewById(R.id.checkBox);
                        EmojiTextView textView = (EmojiTextView) itemView.findViewById(R.id.checkedTextView);
                        // Populate the data into the template view using the data object
                        textView.setText(item.getText());
                        checkbox.setChecked(item.getCompleted());
                        checkbox.setOnCheckedChangeListener(this);
                        RelativeLayout checkboxHolder = (RelativeLayout) itemView.findViewById(R.id.checkBoxHolder);
                        expandCheckboxTouchArea(checkboxHolder, checkbox);
                        this.checklistView.addView(itemView);
                    }
                } else {
                    this.checklistView.removeAllViewsInLayout();
                }
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == checkbox) {
                if (isChecked != Item.getCompleted()) {
                    TaskCheckedCommand event = new TaskCheckedCommand();
                    event.Task = Item;
                    event.completed =  !Item.getCompleted();

                    // it needs to be changed after the event is send -> to the server
                    // maybe a refactor is needed here
                    EventBus.getDefault().post(event);
                    Item.completed =event.completed;
                    Item.save();

                }
            } else {
                View v = (View) buttonView.getParent();
                while (v.getParent() != this.checklistView) {
                    v = (View) v.getParent();
                }
                Integer position = ((ViewGroup) v.getParent()).indexOfChild(v);
                if (Item.checklist.size() > position && isChecked != Item.checklist.get(position).getCompleted()) {
                    TaskSaveEvent event = new TaskSaveEvent();
                    Item.checklist.get(position).setCompleted(isChecked);
                    event.task = Item;
                    EventBus.getDefault().post(event);
                }
            }
        }

		public void expandCheckboxTouchArea(final View expandedView, final View checkboxView){
			expandedView.post(() -> {
                Rect rect = new Rect();
                expandedView.getHitRect(rect);
                expandedView.setTouchDelegate(new TouchDelegate(rect, checkboxView));
            });
		}
    }

    public class DailyViewHolder extends ChecklistedViewHolder {

        DailyItemCardBinding binding;

        public DailyViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            checkbox.setOnCheckedChangeListener(this);
        }

        @Override
        public void bindHolder(Task habitItem, int position) {
            super.bindHolder(habitItem, position);
            binding.setOffset(dailyResetOffset);
            binding.setDaily(habitItem);
        }

        @Override
        public void setDisplayChecklist(Boolean displayChecklist) {
            binding.setDisplayChecklist(displayChecklist);
            super.setDisplayChecklist(displayChecklist);
        }

    }

    public class TodoViewHolder extends ChecklistedViewHolder {

        TodoItemCardBinding binding;

        DateFormat dateFormat;

        public TodoViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

            this.dateFormat = android.text.format.DateFormat.getDateFormat(context);
        }

        @Override
        public void bindHolder(Task habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setTodo(habitItem);
            if (habitItem.duedate != null) {
                binding.setDuedate(this.dateFormat.format(habitItem.duedate));
            }
        }

        @Override
        public void setDisplayChecklist(Boolean displayChecklist) {
            binding.setDisplayChecklist(displayChecklist);
            super.setDisplayChecklist(displayChecklist);
        }
    }

    public class RewardViewHolder extends ViewHolder<Task> {
        RewardItemCardBinding binding;

        @Bind(R.id.btnReward)
        Button btnReward;

        public RewardViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

            btnReward.setClickable(true);
            btnReward.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == btnReward) {
                BuyRewardCommand event = new BuyRewardCommand();
                event.Reward = Item;
                EventBus.getDefault().post(event);
            } else {
                if(Item.specialTag != null && Item.specialTag.equals("item")) {
                    LinearLayout contentViewForDialog = createContentViewForGearDialog();
                    AlertDialog dialog = createGearDialog(contentViewForDialog);
                    dialog.show();
                } else {
                    TaskTappedEvent event = new TaskTappedEvent();
                    event.Task = Item;

                    EventBus.getDefault().post(event);
                }
            }
        }

        private AlertDialog createGearDialog(LinearLayout contentViewForDialog) {
            return new AlertDialog.Builder(context)
                    .setPositiveButton(R.string.reward_dialog_buy, (dialog, which) -> {
                        BuyRewardCommand event = new BuyRewardCommand();
                        event.Reward = Item;
                        EventBus.getDefault().post(event);
                    })
                    .setTitle(binding.getReward().getText())
                    .setView(contentViewForDialog)
                    .setNegativeButton(R.string.reward_dialog_dismiss, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
        }

        @NonNull
        private LinearLayout createContentViewForGearDialog() {
            String price = String.format(Locale.getDefault(), "%.0f", binding.getReward().value);
            String content = binding.getReward().getNotes();

            // External ContentView
            LinearLayout contentViewLayout = new LinearLayout(context);
            contentViewLayout.setOrientation(LinearLayout.VERTICAL);

            // Gear Image
            ImageView gearImageView = new ImageView(context);
            LinearLayout.LayoutParams gearImageLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            gearImageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            gearImageLayoutParams.setMargins(0,0,0,20);
            gearImageView.setMinimumWidth(200);
            gearImageView.setMinimumHeight(200);
            gearImageView.setLayoutParams(gearImageLayoutParams);
            DataBindingUtils.loadImage(gearImageView, "shop_" + binding.getReward().getId());

            // Gear Description
            TextView contentTextView = new TextView(context, null);
            if(!content.isEmpty()){
                contentTextView.setText(content);
            }

            // GoldPrice View
            LinearLayout goldPriceLayout = new LinearLayout(context);
            goldPriceLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams goldPriceLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            goldPriceLayoutParams.setMargins(0, 0, 0, 16);
            goldPriceLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

            goldPriceLayout.setOrientation(LinearLayout.HORIZONTAL);
            goldPriceLayout.setLayoutParams(goldPriceLayoutParams);
            goldPriceLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            // Price View
            TextView priceTextView = new TextView(context);
            priceTextView.setText(price);
            priceTextView.setPadding(10, 0, 0, 0);

            ImageView gold = new ImageView(context);
            gold.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_header_gold));
            gold.setMinimumHeight(50);
            gold.setMinimumWidth(50);
            gold.setPadding(0, 0, 5, 0);

            goldPriceLayout.addView(gold);
            goldPriceLayout.addView(priceTextView);

            if(gearImageView.getDrawable()!= null){
                contentViewLayout.addView(gearImageView);
            }
            contentViewLayout.setGravity(Gravity.CENTER_VERTICAL);

            contentViewLayout.addView(goldPriceLayout);

            if(!content.isEmpty()){
                contentViewLayout.addView(contentTextView);
            }

            return contentViewLayout;
        }

        @Override
        public void bindHolder(Task reward, int position) {
            super.bindHolder(reward, position);

            binding.setReward(reward);
        }
    }

    // endregion

    public void loadContent(boolean forced) {

        if (this.observableContent == null || forced) {
            this.observableContent = new ObservableArrayList<>();
            new Select().from(Task.class)
                    .where(Condition.column("type").eq(this.taskType))
                    .and(Condition.CombinedCondition
                                    .begin(Condition.column("completed").eq(false))
                                    .or(Condition.column("type").eq("daily"))
                    )
                    .orderBy(OrderBy.columns("position", "dateCreated").descending())
                    .async()
                    .queryList(taskTransactionListener);

        }
    }

    private TransactionListener<List<Task>> taskTransactionListener = new TransactionListener<List<Task>>() {
        @Override
        public void onResultReceived(List<Task> tasks) {
            observableContent.clear();
            observableContent.addAll(tasks);
            if (additionalEntries != null) {
                additionalEntries.GetAdditionalEntries(HabitItemRecyclerViewAdapter.this);
            }
            filter();
        }

        @Override
        public boolean onReady(BaseTransaction<List<Task>> transaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<List<Task>> transaction, List<Task> result) {
            return true;
        }
    };

    @Override
    public void GotAdditionalItems(List<Task> items) {
        this.observableContent.addAll(items);
        notifyDataSetChanged();
    }
}
