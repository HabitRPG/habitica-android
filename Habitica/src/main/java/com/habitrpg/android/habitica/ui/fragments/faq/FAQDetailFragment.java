package com.habitrpg.android.habitica.ui.fragments.faq;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FAQDetailFragment extends BaseMainFragment {
    @BindView(R.id.questionTextView)
    TextView questionTextView;

    @BindView(R.id.answerTextView)
    TextView answerTextView;

    private FAQArticle article;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_faq_detail, container, false);

        unbinder = ButterKnife.bind(this, view);

        if (this.article != null) {
            this.questionTextView.setText(this.article.getQuestion());
            this.answerTextView.setText(MarkdownParser.parseMarkdown(this.article.getAnswer()));
        }
        this.answerTextView.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
    }

    public void setArticle(FAQArticle article) {
        this.article = article;
        if (this.questionTextView != null) {
            this.questionTextView.setText(this.article.getQuestion());
        }
        if (this.answerTextView != null) {
            this.answerTextView.setText(MarkdownParser.parseMarkdown(this.article.getAnswer()));
        }
    }
}
