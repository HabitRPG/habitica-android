package com.habitrpg.android.habitica.models.inventory;

public interface Animal {

    public String getKey();

    public void setKey(String key);

    public String getAnimal();

    public void setAnimal(String animal);

    public String getColor();

    public void setColor(String color);

    public String getAnimalGroup();

    public void setAnimalGroup(String group);

    public String getAnimalText();

    public void setAnimalText(String animalText);

    public String getColorText();

    public void setColorText(String colorText);

    public boolean getPremium();

    public void setPremium(boolean premium);

    public boolean getLimited();

    public void setLimited(boolean limited);

    public Integer getNumberOwned();

    public void setNumberOwned(Integer numberOwned);
}
