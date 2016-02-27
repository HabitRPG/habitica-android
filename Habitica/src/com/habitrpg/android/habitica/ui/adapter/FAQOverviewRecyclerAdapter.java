package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.faq.FAQDetailFragment;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FAQOverviewRecyclerAdapter  extends RecyclerView.Adapter<FAQOverviewRecyclerAdapter.FAQArticleViewHolder> {

    private List<FAQArticle> articles;
    public MainActivity activity;

    public void setArticles(List<FAQArticle> articles) {
        this.articles = articles;
        this.notifyDataSetChanged();
    }

    @Override
    public FAQArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plain_list_item, parent, false);

        return new FAQArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FAQArticleViewHolder holder, int position) {
        holder.bind(this.articles.get(position));
    }

    @Override
    public int getItemCount() {
        return this.articles == null ? 0 : this.articles.size();
    }

    class FAQArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.linearLayout)
        LinearLayout linearLayout;

        @Bind(R.id.textView)
        TextView textView;

        FAQArticle article;

        Context context;

        public FAQArticleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            linearLayout.setOnClickListener(this);

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
}
