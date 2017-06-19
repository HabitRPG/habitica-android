package com.habitrpg.android.habitica.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FAQArticle extends RealmObject {

    @PrimaryKey
    private Integer position;

    private String question, answer;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
