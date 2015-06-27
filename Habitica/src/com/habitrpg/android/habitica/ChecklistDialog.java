package com.habitrpg.android.habitica;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Checklist;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;

import java.util.Objects;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by MagicMicky on 30/04/14.
 * Updated by Negue on 21/06/15
 */
public class ChecklistDialog<THabitItem extends Checklist> implements DialogInterface.OnClickListener {
    private final Context mContext;
    private Checklist checklist;
    private THabitItem item;
    private Callback<THabitItem> updateCallback;
    private final LayoutInflater mInflater;
    private final APIHelper mAPIHelper;
    private CheckListUpdater checklistUpdater;
    private boolean mEditMode;

    public ChecklistDialog(Context act, APIHelper apiHelper, THabitItem todo, Checklist checklist) {
        this.mContext = act;
        this.item = todo;

        this.checklist = todo != null ? todo : checklist;
        this.mInflater = LayoutInflater.from(mContext);
        this.mAPIHelper = apiHelper;
        this.checklistUpdater = null;
        mEditMode = false;
    }

    public ChecklistDialog(Context context, CheckListUpdater checkListUpdater, THabitItem todo, Checklist checklist, boolean editMode) {
        this(context, null, todo, checklist);

        this.checklistUpdater = checkListUpdater;
        this.mEditMode = editMode;
    }

    public void show() {
        View convertView = mInflater.inflate(R.layout.checklist_dialog, null);
        AlertDialog d = new AlertDialog.Builder(mContext)
                .setTitle(item != null ? item.getText() : mEditMode
                        ? mContext.getString(R.string.checklist_title_edit)
                        : mContext.getString(R.string.checklist_title_add))
                .setView(convertView)
                .setPositiveButton(mContext.getString(R.string.update_btn), this)
                .setNegativeButton(mContext.getString(R.string.dialog_cancel_button), null)
                .create();
        ListView lv = (ListView) convertView.findViewById(R.id.LV_checklist);

        final CheckListAdapter adapter = new CheckListAdapter(mContext, checklist);

        lv.setAdapter(adapter);
        d.show();

        if (mAPIHelper == null) {
            convertView.findViewById(R.id.RL_addItem).setVisibility(View.VISIBLE);
            ImageButton btn = (ImageButton) convertView.findViewById(R.id.BT_addItem);
            final EditText addItem = (EditText) convertView.findViewById(R.id.ET_addItem);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (addItem.getText() != null && addItem.getText().length() > 0) {
                        Checklist.ChecklistItem item = new Checklist.ChecklistItem();
                        item.setText(addItem.getText().toString());
                        adapter.addItem(item);
                        adapter.notifyDataSetChanged();
                        addItem.setText("");
                    }
                }
            });
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (checklistUpdater != null) {
            checklistUpdater.onChecklistChosen(checklist);
        } else {
            if (item instanceof ToDo) {
                mAPIHelper.updateTask((ToDo) item, new Callback<ToDo>() {
                    @Override
                    public void success(ToDo toDo, Response response) {

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            if (item instanceof Daily) {
                mAPIHelper.updateTask((Daily) item, new Callback<Daily>() {
                    @Override
                    public void success(Daily daily, Response response) {

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }
        }
    }

    private class CheckListAdapter extends BaseAdapter {

        private final Checklist checklist;
        private final Context mContext;

        public CheckListAdapter(Context context, Checklist list) {
            this.checklist = list;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return checklist.getSize();
        }

        @Override
        public Checklist.ChecklistItem getItem(int position) {
            return checklist.getItems().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflator.inflate(R.layout.checklist_dialog_list_item, parent, false);
            }
            CheckBox completed = (CheckBox) convertView.findViewById(R.id.plus);
            TextView text = (TextView) convertView.findViewById(R.id.TV_title);
            Checklist.ChecklistItem currentItem = this.getItem(position);
            text.setText(currentItem.getText());
            convertView.setId(position);
            completed.setChecked(currentItem.isCompleted());
            completed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getParent() != null) {
                        getItem(((View) v.getParent()).getId()).setCompleted(((CheckBox) v).isChecked());
                    }
                }
            });


            ImageButton delete = (ImageButton) convertView.findViewById(R.id.BT_delete);

            if(mAPIHelper == null) {
                delete.setVisibility(View.VISIBLE);
            }
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v .getParent() != null){
                        removeItem(((View)v.getParent()).getId());
                    }
                }
            });

            return convertView;
        }

        private void removeItem(int pos) {
            this.checklist.getItems().remove(pos);

            this.notifyDataSetChanged();
        }

        public void addItem(Checklist.ChecklistItem item) {
            this.checklist.addItem(item);
        }
    }

    public interface CheckListUpdater {
        void onChecklistChosen(Checklist list);
    }
}