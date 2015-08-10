package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.DailyItemCardBinding;
import com.habitrpg.android.habitica.databinding.HabitItemCardBinding;
import com.habitrpg.android.habitica.databinding.RewardItemCardBinding;
import com.habitrpg.android.habitica.databinding.TodoItemCardBinding;
import com.habitrpg.android.habitica.events.BuyRewardTappedEvent;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskLongPressedEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.TodoCheckedEvent;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RewardItem;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class HabitItemRecyclerViewAdapter<THabitItem extends Task>
        extends RecyclerView.Adapter<HabitItemRecyclerViewAdapter.ViewHolder>
        implements FlowContentObserver.OnSpecificModelStateChangedListener {

    int layoutResource;
    private Class<ViewHolder<THabitItem>> viewHolderClass;
    List<THabitItem> contents;
    Class<THabitItem> taskClass;
    String taskType;
    private ObservableArrayList<THabitItem> observableContent;
    FlowContentObserver observer;
    Context context;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private RecyclerView.Adapter<ViewHolder> parentAdapter;


    public HabitItemRecyclerViewAdapter(String taskType, Class<THabitItem> newTaskClass, int layoutResource, Class<ViewHolder<THabitItem>> viewHolderClass, Context newContext) {
        this(taskType, newTaskClass, layoutResource, viewHolderClass, newContext, null);
    }

    public HabitItemRecyclerViewAdapter(String taskType, Class<THabitItem> newTaskClass, int layoutResource, Class<ViewHolder<THabitItem>> viewHolderClass,
                                        Context newContext, final ObservableArrayList<THabitItem> content) {
        this.taskType = taskType;
        this.context = newContext;
        this.taskClass = newTaskClass;
        observableContent = content;

        if(content == null)
        {
            this.loadContent();

            observer = new FlowContentObserver();
            observer.registerForContentChanges(this.context, this.taskClass);

            observer.addSpecificModelChangeListener(this);
        }
        else
        {
            content.addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
                @Override
                public void onChanged(ObservableList sender) {
                    handler.removeCallbacks(reloadContentRunable);
                    handler.postDelayed(reloadContentRunable, 200);
                }

                @Override
                public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
                    handler.removeCallbacks(reloadContentRunable);
                    handler.postDelayed(reloadContentRunable, 200);
                }

                @Override
                public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                    handler.removeCallbacks(reloadContentRunable);
                    handler.postDelayed(reloadContentRunable, 200);
                }

                @Override
                public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
                    handler.removeCallbacks(reloadContentRunable);
                    handler.postDelayed(reloadContentRunable, 200);
                }

                @Override
                public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                    handler.removeCallbacks(reloadContentRunable);
                    handler.postDelayed(reloadContentRunable, 200);
                }
            });

            loadContent();
        }

        this.layoutResource = layoutResource;
        this.viewHolderClass = viewHolderClass;
    }

    public void setParentAdapter(RecyclerView.Adapter<HabitItemRecyclerViewAdapter.ViewHolder> parentAdapter)
    {
        this.parentAdapter = parentAdapter;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        this.observer.unregisterForContentChanges(this.context);
        super.onViewDetachedFromWindow(holder);
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
        return position;
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public ViewHolder<THabitItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType) {
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
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
                    case "RewardItemViewHolder":
                        return new HabitItemRecyclerViewAdapter.RewardItemViewHolder(view);
                }
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task item = contents.get(position);

        holder.bindHolder(item, position);
    }

    private Handler handler = new Handler();
    private Runnable reloadContentRunable = new Runnable() {
        @Override
        public void run() {
            Log.d("Reload Content","");
            loadContent();
        }
    };

    @Override
    public void onModelStateChanged(Class<? extends Model> aClass, BaseModel.Action action, String s, String s1) {
        handler.removeCallbacks(reloadContentRunable);
        handler.postDelayed(reloadContentRunable, 200);
    }

    @BindingAdapter("bind:imageName")
    public static void loadImage(ImageView view, String imageName) {
        Picasso.with(view.getContext()).load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/shop_"+ imageName +".png").into(view);
    }

    @BindingAdapter("bind:cardColor")
    public static void setCardColor(CardView cardView, int color) {
        cardView.setCardBackgroundColor(cardView.getResources().getColor(color));
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(CheckBox view, int color) {
        ViewHelper.SetBackgroundTint(view, view.getResources().getColor(color));
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(Button view, int color) {
        ViewHelper.SetBackgroundTint(view, view.getResources().getColor(color));
    }

    public abstract class ViewHolder<THabitItem extends Task> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @InjectView(R.id.card_view)
        protected CardView cardView;

        @InjectView(R.id.checkedTextView)
        protected CheckedTextView checkedTextView;

        protected android.content.res.Resources resources;

        public THabitItem Item;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);

            itemView.setOnLongClickListener(this);
            itemView.setLongClickable(true);

            ButterKnife.inject(this, itemView);

            resources = itemView.getResources();
        }

        public void bindHolder(THabitItem habitItem, int position) {
            double itemvalue = habitItem.getValue();
            Item = habitItem;
        }

        @Override
        public void onClick(View v) {
            if (v != itemView)
                return;

            TaskTappedEvent event = new TaskTappedEvent();
            event.Task = Item;

            EventBus.getDefault().post(event);
        }

        @Override
        public boolean onLongClick(View v) {
            TaskLongPressedEvent event = new TaskLongPressedEvent();
            event.Task = Item;

            EventBus.getDefault().post(event);

            return true;
        }

    }

    public class HabitViewHolder extends ViewHolder<Task> {

        @InjectView(R.id.btnPlus)
        Button btnPlus;

        @InjectView(R.id.btnMinus)
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

    public class DailyViewHolder extends ViewHolder<Task> {
        @InjectView(R.id.checkBox)
        CheckBox checkbox;

        DailyItemCardBinding binding;

        public DailyViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }


        @Override
        public void bindHolder(Task habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setDaily(habitItem);
        }
    }

    public class TodoViewHolder extends ViewHolder<Task> implements CompoundButton.OnCheckedChangeListener {

        @InjectView(R.id.checkBox)
        CheckBox checkbox;

        TodoItemCardBinding binding;

        public TodoViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

            checkbox.setOnCheckedChangeListener(this);
        }

        @Override
        public void bindHolder(Task habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setTodo(habitItem);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TodoCheckedEvent event = new TodoCheckedEvent();
            event.ToDo = Item;

            EventBus.getDefault().post(event);
        }
    }

    public class RewardViewHolder extends ViewHolder<Reward> {
        RewardItemCardBinding binding;

        public RewardViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

            binding.btnReward.setClickable(true);
            binding.btnReward.setOnClickListener(this);
            binding.imageView3.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            BuyRewardTappedEvent event = new BuyRewardTappedEvent();

            if (v == binding.btnReward) {
                event.Reward = Item;

                EventBus.getDefault().post(event);
            } else super.onClick(v);
        }

        @Override
        public void bindHolder(Reward habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setReward(habitItem);
        }
    }

    public class RewardItemViewHolder extends ViewHolder<RewardItem> implements Target
    {
        RewardItemCardBinding binding;

        public RewardItemViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

            binding.btnReward.setClickable(true);
            binding.btnReward.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            BuyRewardTappedEvent event = new BuyRewardTappedEvent();

            if (v == binding.btnReward) {
                event.Reward = Item;

                EventBus.getDefault().post(event);
            } else super.onClick(v);
        }

        @Override
        public void bindHolder(RewardItem habitItem, int position) {
            super.bindHolder(habitItem, position);
            binding.setReward(habitItem);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            binding.imageView3.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    public void loadContent() {
        if(this.observableContent == null) {

            this.contents = new Select().from(this.taskClass)
                    .where(Condition.column("type").eq(this.taskType))
                    .queryList();
        }
        else
        {
            this.contents = observableContent;
        }

        if(parentAdapter != null)
        {
            parentAdapter.notifyDataSetChanged();
        }
        else
        {
            notifyDataSetChanged();
        }
    }
}
