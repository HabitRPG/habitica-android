package com.habitrpg.android.habitica.ui.fragments.social;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;

public class TavernDetailFragment extends BaseFragment {

    @Inject
    UserRepository userRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.dailies_button)
    Button dailiesButton;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_tavern_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getCompositeSubscription().add(userRepository.getUser(userId).subscribe(user -> {
            this.user = user;
            this.updatePausedState();
        }, RxErrorHandler.handleEmptyError()));
    }

    private void updatePausedState() {
        if (dailiesButton == null) {
            return;
        }
        if (user.getPreferences().getSleep()) {
            dailiesButton.setText(R.string.tavern_inn_checkOut);
        } else {
            dailiesButton.setText(R.string.tavern_inn_rest);
        }
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @OnClick(R.id.dailies_button)
    public void dailiesButtonClicked() {
        userRepository.sleep(user).subscribe(user -> {}, RxErrorHandler.handleEmptyError());
    }
}
