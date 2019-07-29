package com.habitrpg.android.habitica.models.inventory;


public interface Animal {

    String getKey();

    void setKey(String key);

    String getText();

    void setText(String text);

    String getType();

    void setType(String type);

    String getAnimal();

    void setAnimal(String animal);

    String getColor();

    void setColor(String color);

    boolean getPremium();

    void setPremium(boolean premium);

    Integer getNumberOwned();

    void setNumberOwned(Integer numberOwned);
}
