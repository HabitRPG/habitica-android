package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.habitrpg.android.habitica.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdvancedLogin extends Fragment {
    @BindView(R.id.al_username)
    EditText mUsername;
    @BindView(R.id.al_password)
    EditText mPassword;
    @BindView(R.id.al_api_endpoint)
    EditText mAPIEndpoint;

    public AdvancedLogin() {
    }

    public static AdvancedLogin newInstance() {
        return new AdvancedLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_advanced_login, container, false);
        ButterKnife.bind(this, retVal);
        return retVal;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public String getUsername() {
        return mUsername.getText().toString();
    }

    public String getPassword() {
        return mPassword.getText().toString();
    }

    public String getAPIEndpoint() {
        return mAPIEndpoint.getText().toString();
    }
}