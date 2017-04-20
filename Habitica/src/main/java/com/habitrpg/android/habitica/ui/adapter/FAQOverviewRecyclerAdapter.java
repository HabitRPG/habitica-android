package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.faq.FAQDetailFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

public class FAQOverviewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_JUSTIN = 0;
    private static final int VIEW_TYPE_FAQ = 1;

    public MainActivity activity;
    private List<FAQArticle> articles;

    private PublishSubject<Void> resetWalkthroughEvents = PublishSubject.create();

    public void setArticles(List<FAQArticle> articles) {
        this.articles = articles;
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_JUSTIN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_list_item, parent, false);
            return new ResetWalkthroughViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.plain_list_item, parent, false);
            return new FAQArticleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_FAQ) {
            ((FAQArticleViewHolder) holder).bind(this.articles.get(position-1));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_JUSTIN;
        } else {
            return VIEW_TYPE_FAQ;
        }
    }

    @Override
    public int getItemCount() {
        return this.articles == null ? 1 : this.articles.size()+1;
    }

    public Observable<Void> getResetWalkthroughEvents() {
        return resetWalkthroughEvents.asObservable();
    }

    class FAQArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.textView)
        TextView textView;

        FAQArticle article;

        Context context;

        FAQArticleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            textView.setOnClickListener(this);

            context = itemView.getContext();
        }

        public void bind(FAQArticle article) {
            this.article = article;
            this.textView.setText(this.article.getQuestion());
        }

        @Override
        public void onClick(View v) {
            FAQDetailFragment fragment = new FAQDetailFragment();
            fragment.setArticle(this.article);
            activity.displayFragment(fragment);
        }
    }

    private class ResetWalkthroughViewHolder extends RecyclerView.ViewHolder {

        ResetWalkthroughViewHolder(View itemView) {
            super(itemView);
            Button button = (Button)itemView;
            button.setText(itemView.getContext().getString(R.string.reset_walkthrough));
            button.setOnClickListener(v -> resetWalkthroughEvents.onNext(null));
        }
    }
}
