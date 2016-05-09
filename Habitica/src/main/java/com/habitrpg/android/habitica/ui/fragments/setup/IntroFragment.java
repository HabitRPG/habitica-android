package com.habitrpg.android.habitica.ui.fragments.setup;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroFragment extends BaseFragment {

    @BindView(R.id.titleTextView)
    TextView titleTextView;

    @BindView(R.id.descriptionTextView)
    TextView descriptionTextView;

    @BindView(R.id.imageView)
    ImageView imageView;

    Drawable image;
    String title;
    String description;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_intro, container, false);

        ButterKnife.bind(this, v);

        if (this.image != null) {
            this.imageView.setImageDrawable(this.image);
        }

        if (this.title != null) {
            this.titleTextView.setText(this.title);
        }

        if (this.description != null) {
            this.descriptionTextView.setText(this.description);
        }

        return v;
    }

    public void setImage(Drawable image) {
        this.image = image;
        if (this.imageView != null && image != null) {
            this.imageView.setImageDrawable(image);
        }
    }

    public void setTitle(String text) {
        this.title = text;
        if (this.titleTextView != null && text != null) {
            this.titleTextView.setText(text);
        }
    }

    public void setDescription(String text) {
        this.description = text;
        if (this.descriptionTextView != null && text != null) {
            this.descriptionTextView.setText(text);
        }
    }

}
