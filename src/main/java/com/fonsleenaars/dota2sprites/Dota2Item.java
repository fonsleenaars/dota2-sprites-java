package com.fonsleenaars.dota2sprites;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Dota2Item {
    private int id;
    private String name;
    private String localizedName;

    public Dota2Item() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

}
