package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.DailyItemCardBinding;
import com.habitrpg.android.habitica.databinding.HabitItemCardBinding;
import com.habitrpg.android.habitica.databinding.RewardItemCardBinding;
import com.habitrpg.android.habitica.databinding.TodoItemCardBinding;
import com.habitrpg.android.habitica.ui.helpers.HabitColorHelper;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HabitItemRecyclerViewAdapter<THabitItem extends HabitItem>
        extends RecyclerView.Adapter<HabitItemRecyclerViewAdapter.ViewHolder>
        implements FlowContentObserver.OnSpecificModelStateChangedListener {

    int layoutResource;
    private Class<ViewHolder<THabitItem>> viewHolderClass;
    List<THabitItem> contents;
    Class<THabitItem> taskClass;
    FlowContentObserver observer;
    Context context;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;


    public HabitItemRecyclerViewAdapter(Class<THabitItem> newTaskClass, int layoutResource, Class<ViewHolder<THabitItem>> viewHolderClass, Context newContext) {

        this.context = newContext;
        this.taskClass = newTaskClass;
        this.loadContent();

        observer = new FlowContentObserver();
        observer.registerForContentChanges(this.context, this.taskClass);

        observer.addSpecificModelChangeListener(this);

        this.layoutResource = layoutResource;
        this.viewHolderClass = viewHolderClass;
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
                }
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HabitItem item = contents.get(position);

        holder.bindHolder(item, position);
    }

    @Override
    public void onModelStateChanged(Class<? extends Model> aClass, BaseModel.Action action, String s, String s1) {
        //TODO: Not load all content every time something changed
        this.loadContent();
    }

    public abstract class ViewHolder<THabitItem extends HabitItem> extends RecyclerView.ViewHolder {

        @InjectView(R.id.card_view)
        protected CardView cardView;

        @InjectView(R.id.checkedTextView)
        protected CheckedTextView checkedTextView;

        protected android.content.res.Resources resources;

        public THabitItem Item;

        public void SetCardBackgroundColor(int color) {
            if (cardView != null) {
                cardView.setCardBackgroundColor(color);
            }
        }

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);

            resources = itemView.getResources();
        }

        public void bindHolder(THabitItem habitItem, int position) {
            double itemvalue = habitItem.getValue();
            int itemColorRes = HabitColorHelper.GetItemColorByValue(itemvalue);

            SetCardBackgroundColor(resources.getColor(itemColorRes));
            Item = habitItem;
        }
    }

    public class HabitViewHolder extends ViewHolder<Habit> {

        @InjectView(R.id.btnPlus)
        Button btnPlus;

        @InjectView(R.id.btnMinus)
        Button btnMinus;

        HabitItemCardBinding binding;

        public HabitViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void bindHolder(Habit habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setHabit(habitItem);

            double itemvalue = habitItem.getValue();
            int btnColorRes = HabitColorHelper.GetItemButtonColorByValue(itemvalue);

            ViewHelper.SetBackgroundTint(btnPlus, resources.getColor(btnColorRes));
            ViewHelper.SetBackgroundTint(btnMinus, resources.getColor(btnColorRes));
        }
    }

    public class DailyViewHolder extends ViewHolder<Daily> {
        @InjectView(R.id.checkBox)
        CheckBox checkbox;

        DailyItemCardBinding binding;

        public DailyViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }


        @Override
        public void bindHolder(Daily habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setDaily(habitItem);

            double itemvalue = habitItem.getValue();
            int btnColorRes = HabitColorHelper.GetItemButtonColorByValue(itemvalue);

            ViewHelper.SetBackgroundTint(checkbox, resources.getColor(btnColorRes));
        }
    }

    public class TodoViewHolder extends ViewHolder<ToDo> {

        @InjectView(R.id.checkBox)
        CheckBox checkbox;

        TodoItemCardBinding binding;

        public TodoViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void bindHolder(ToDo habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setTodo(habitItem);

            double itemvalue = habitItem.getValue();
            int btnColorRes = HabitColorHelper.GetItemButtonColorByValue(itemvalue);

            ViewHelper.SetBackgroundTint(checkbox, resources.getColor(btnColorRes));
        }
    }

    public class RewardViewHolder extends ViewHolder<Reward> {
        RewardItemCardBinding binding;

        public RewardViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void bindHolder(Reward habitItem, int position) {
            super.bindHolder(habitItem, position);

            binding.setReward(habitItem);
        }
    }

    public void loadContent() {
        this.contents = new Select().from(this.taskClass).queryList();
        notifyDataSetChanged();
    }
}
