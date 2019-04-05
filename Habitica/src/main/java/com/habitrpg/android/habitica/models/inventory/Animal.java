package com.habitrpg.android.habitica.models.inventory;


public interface Animal {

    public String getKey();

    public void setKey(String key);

    public String getText();

    public void setText(String text);

    public String getType();

    public void setType(String type);

    public String getAnimal();

    public void setAnimal(String animal);

    public String getColor();

    public void setColor(String color);

    public String getAnimalGroup();

    public void setAnimalGroup(String group);

    public boolean getPremium();

    public void setPremium(boolean premium);

    public Integer getNumberOwned();

    public void setNumberOwned(Integer numberOwned);
}
