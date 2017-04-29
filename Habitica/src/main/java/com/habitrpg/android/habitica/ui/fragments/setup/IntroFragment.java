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
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroFragment extends BaseFragment {

    @BindView(R.id.subtitleTextView)
    TextView subtitleTextView;

    @BindView(R.id.titleTextView)
    TextView titleTextView;

    @BindView(R.id.titleImageView)
    ImageView titleImageView;

    @BindView(R.id.descriptionTextView)
    TextView descriptionTextView;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.container_view)
    ViewGroup containerView;

    Drawable image;
    Drawable titleImage;
    String subtitle;
    String title;
    String description;
    Integer backgroundColor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_intro, container, false);

        unbinder = ButterKnife.bind(this, v);

        if (this.image != null) {
            this.imageView.setImageDrawable(this.image);
        }

        if (this.titleImage != null) {
            this.titleImageView.setImageDrawable(this.titleImage);
        }

        if (this.subtitle != null) {
            this.subtitleTextView.setText(this.subtitle);
        }

        if (this.title != null) {
            this.titleTextView.setText(this.title);
        }

        if (this.description != null) {
            this.descriptionTextView.setText(this.description);
        }

        if (this.backgroundColor != null) {
            this.containerView.setBackgroundColor(this.backgroundColor);
        }

        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    public void setImage(Drawable image) {
        this.image = image;
        if (this.imageView != null && image != null) {
            this.imageView.setImageDrawable(image);
        }
    }

    public void setTitleImage(Drawable image) {
        this.titleImage = image;
        if (this.titleImageView != null && image != null) {
            this.titleImageView.setImageDrawable(image);
        }
    }

    public void setSubtitle(String text) {
        this.subtitle = text;
        if (this.subtitleTextView != null && text != null) {
            this.subtitleTextView.setText(text);
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

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        if (containerView != null) {
            containerView.setBackgroundColor(color);
        }
    }

}
