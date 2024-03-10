package com.example.imagesgallery.Model;

public class Tag {
    private int id;
    private String type;
    private String value;

    public Tag(String type, String value)
    {
        this.type=type;
        this.value=value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public boolean equals(Tag other)
    {
        if (other==null)
        {
            return false;
        }
        return getType().equals(other.getType()) && getValue().equals(other.getValue());
    }
}
