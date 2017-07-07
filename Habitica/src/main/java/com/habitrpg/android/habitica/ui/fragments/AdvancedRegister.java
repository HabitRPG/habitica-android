package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.habitrpg.android.habitica.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AdvancedRegister extends AdvancedLogin {
    @BindView(R.id.al_email)
    EditText mEmail;
    @BindView(R.id.al_confirm_password)
    EditText mConfirmPassword;

    public AdvancedRegister() {
    }

    public static AdvancedRegister newInstance() {
        return new AdvancedRegister();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_advanced_register, container, false);
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

    public String getEmail() {
        return mEmail.getText().toString();
    }

    public String getConfirmPassword() {
        return mConfirmPassword.getText().toString();
    }
}