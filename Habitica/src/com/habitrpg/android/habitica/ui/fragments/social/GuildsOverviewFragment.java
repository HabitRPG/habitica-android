package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GuildsOverviewFragment extends BaseMainFragment implements Callback<ArrayList<Group>>, View.OnClickListener {

    @Bind(R.id.my_guilds_listview)
    LinearLayout  guildsListView;

    @Bind(R.id.publicGuildsButton)
    Button publicGuildsButton;


    private List<Group> guilds;
    private ArrayList<String> guildIDs;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.fetchGuilds();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_guilds_overview, container, false);
        ButterKnife.bind(this, v);
        this.publicGuildsButton.setOnClickListener(this);
        if (this.guilds != null) {
            this.setGuildsOnListView();
        }
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

        this.guilds = select.queryList();
        this.setGuildsOnListView();
    }

    private void setGuildsOnListView() {
        if (this.guildsListView == null) {
            return;
        }
        this.guildIDs = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Group guild : this.guilds) {
            TextView entry = (TextView) inflater.inflate(R.layout.plain_list_item, null);
            entry.setText(guild.name);
            entry.setOnClickListener(this);
            this.guildsListView.addView(entry);
            this.guildIDs.add(guild.id);
        }
    }

    @Override
    public void success(ArrayList<Group> groups, Response response) {
        this.guilds = groups;
        this.setGuildsOnListView();
    }

    @Override
    public void failure(RetrofitError error) {

    }

    @Override
    public void onClick(View v) {
        if (v == this.publicGuildsButton) {
            PublicGuildsFragment publicGuildsFragment = new PublicGuildsFragment();
            publicGuildsFragment.memberGuildIDs = this.guildIDs;
            this.activity.displayFragment(publicGuildsFragment);
        } else {
            Integer guildIndex = ((ViewGroup)v.getParent()).indexOfChild(v);
            GuildFragment guildFragment = new GuildFragment();
            guildFragment.setGuild(this.guilds.get(guildIndex));
            guildFragment.isMember = true;
            this.activity.displayFragment(guildFragment);
        }
    }
}
