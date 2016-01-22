package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.FAQOverviewRecyclerAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viirus on 22/01/16.
 */
public class FAQDetailFragment extends BaseFragment {
    @Bind(R.id.questionTextView)
    TextView questionTextView;

    @Bind(R.id.answerTextView)
    TextView answerTextView;

    private FAQArticle article;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_faq_detail, container, false);

        ButterKnife.bind(this, v);

        if (this.article != null) {
            this.questionTextView.setText(this.article.getQuestion());
            this.answerTextView.setText(this.article.getAnswer());
        }

        return v;
    }

    public void setArticle(FAQArticle article) {
        this.article = article;
        if (this.questionTextView != null) {
            this.questionTextView.setText(this.article.getQuestion());
        }
        if (this.answerTextView != null) {
            this.answerTextView.setText(this.article.getAnswer());
        }
    }
}
