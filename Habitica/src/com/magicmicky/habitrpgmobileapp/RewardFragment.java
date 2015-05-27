package com.magicmicky.habitrpgmobileapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;

public class RewardFragment extends CardFragment {

	private static final String TITLE = "Rewards";
	private RewardAdapter adapter = null;
	private TextView silver=null;
	private TextView gold=null;
	private int g=0,s=0;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		gold = (TextView)v.findViewById(R.id.gold);
		gold.setVisibility(View.VISIBLE);
		gold.setText(g+"");
		silver = (TextView) v.findViewById(R.id.silver);
		silver.setVisibility(View.VISIBLE);
		silver.setText(s+"");
		ImageView gp = (ImageView) v.findViewById(R.id.IMG_gold);
		gp.setVisibility(View.VISIBLE);
		ImageView sp = (ImageView) v.findViewById(R.id.IMG_silver);
		sp.setVisibility(View.VISIBLE);
		return v;
	}

	public void setGold(double gp) {
			this.g = (int) gp;
			this.s = (int) ((gp - g)*100);
		if(this.gold!=null && this.silver !=null) {
			this.gold.setText(g+"");
			this.silver.setText(s+"");
		}
	}
	@Override
	protected HabitType getTaskType() {
		return HabitType.reward;
	}
	@Override
	protected String getTitle() {
		return RewardFragment.TITLE;
	}
	@Override
	protected List<HabitItem> filterData(List<HabitItem> items) {
		List<HabitItem> res = new ArrayList<HabitItem>();
		for(HabitItem it:items) {
			if(it instanceof Reward) {
				res.add(it);
			}
		}
		return res;
	}
	@Override
	public MyAdapter instantiateAdapter(Context c) {
		if(adapter==null) {
			List<HabitItem> rews = new ArrayList<HabitItem>();
			this.adapter =  new RewardAdapter(c, rews);
		}
		return adapter;
	}

	protected class RewardAdapter extends MyAdapter {

		public RewardAdapter(Context c, List<HabitItem> rews) {
			super(c, rews);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        LayoutInflater inflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	if (convertView == null){  
	    		convertView = inflator.inflate(R.layout.reward, parent, false);

			}
			Button price = (Button) convertView.findViewById(R.id.plus);
			price.setCompoundDrawablesWithIntrinsicBounds(null, null,null,null);//(it.getType(), it.getLevel())), null);
			TextView reward = (TextView) convertView.findViewById(R.id.TV_reward);
			convertView.setId(position);
			if(this.getItem(position) instanceof Reward.SpecialReward) {
				Reward.SpecialReward it = (Reward.SpecialReward) this.getItem(position);
				convertView.setClickable(false);
				convertView.setLongClickable(false);
				//UserPicture usPic = new UserPicture("m", RewardFragment.this.getActivity());
				//Drawable bmpdrw = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(usPic.drawObject(it.getType(), it.getLevel()), 50, 50, true));

				//price.setCompoundDrawablesWithIntrinsicBounds(null, null, bmpdrw,null);//(it.getType(), it.getLevel())), null);
				price.setOnClickListener(buyItem);

			} else {
			 	convertView.setClickable(true);
				convertView.setLongClickable(true);
				//price.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.gold),null);//(it.getType(), it.getLevel())), null);
				price.setOnClickListener(getOnClickListener());

			}
			ATaskLoadImage loader = new ATaskLoadImage(price, getActivity());
			loader.execute(this.getItem(position));
			Reward currentItem = (Reward) this.getItem(position);

			reward.setText(currentItem.getText());
			price.setText((int) currentItem.getValue() + "");
			if(isItemChecked(position)) {
				convertView.setBackgroundResource(R.drawable.list_focused_holo);
			} else {
				convertView.setBackgroundResource(R.drawable.item_background);
			}

			return convertView;
		}
	}
	private class ATaskLoadImage extends AsyncTask<HabitItem,Void,Drawable> {
		private final Button viewToAddCompound;
        private final Context context;

        public ATaskLoadImage(Button v,Context context) {
			this.viewToAddCompound = v;
            this.context=context;
		}
		@Override
		protected Drawable doInBackground(HabitItem... item) {
			Drawable drw;
			/*if(item[0] instanceof Reward.SpecialReward) {
				Reward.SpecialReward it = (Reward.SpecialReward) item[0];
				UserPicture usPic = new UserPicture("m", RewardFragment.this.getActivity());
				drw = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(usPic.drawObject(it.getType(), it.getLevel()), dipToPixels(context,30), dipToPixels(context,30), true));

			} else {
			*/	drw = getResources().getDrawable(R.drawable.gold);
			//}
				return drw;
		}
		@Override
		protected void onPostExecute(Drawable d) {
			this.viewToAddCompound.setCompoundDrawablesWithIntrinsicBounds(null,null,d,null);
		}
        private int dipToPixels(Context context, float dipValue) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
        }

    }



}
