package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PartyInviteFragment extends BaseFragment {

    public boolean isEmailInvite;

    @Bind(R.id.inviteDescription)
    TextView inviteDescription;

    @Bind(R.id.invitationWrapper)
    LinearLayout invitationWrapper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_party_invite, container, false);
        ButterKnife.bind(this, v);

        if (isEmailInvite) {
            inviteDescription.setText(getString(R.string.invite_email_description));
        } else {
            inviteDescription.setText(getString(R.string.invite_id_description));
        }

        addInviteField();

        return v;
    }

    @OnClick(R.id.addInviteButton)
    public void addInviteField() {
        EditText editText = new EditText(getContext());
        if (isEmailInvite) {
            editText.setHint(R.string.email);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            editText.setHint(R.string.user_id);
        }
        invitationWrapper.addView(editText);
    }

    public String[] getValues() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < invitationWrapper.getChildCount();i++) {
            EditText valueEditText = (EditText)invitationWrapper.getChildAt(i);
            if (valueEditText.getText().toString().length() > 0) {
                values.add(valueEditText.getText().toString());
            }
        }
        return values.toArray(new String[values.size()]);
    }
}
