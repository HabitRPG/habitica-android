package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GuildsOverviewFragment extends BaseMainFragment implements Callback<ArrayList<Group>> {

    @Bind(R.id.my_guilds_listview)
    LinearLayout  guildsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_guilds_overview, container, false);
        ButterKnife.bind(this, v);
        this.fetchGuilds();
        return v;
    }

    private void fetchGuilds() {
        this.mAPIHelper.apiService.listGroups("guilds", this);
    }

    private void loadGuilds() {
        if(user == null){
            return;
        }

        Where<Group> select = new Select()
                .from(Group.class)
                .where(Condition.column("type").eq("guild"));

        List<Group> guilds = select.queryList();
    }

    private void setGuildsOnListView(List<Group> guilds) {
        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Group guild : guilds) {
            LinearLayout entry = (LinearLayout) inflater.inflate(R.layout.plain_list_item, null);
            TextView textView = (TextView) entry.findViewById(R.id.textView);
            textView.setText(guild.name);
            this.guildsListView.addView(entry);
        }
    }

    @Override
    public void success(ArrayList<Group> groups, Response response) {
        this.setGuildsOnListView(groups);
    }

    @Override
    public void failure(RetrofitError error) {

    }
}
