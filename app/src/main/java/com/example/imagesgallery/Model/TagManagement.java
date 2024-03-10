package com.example.imagesgallery.Model;

import java.util.ArrayList;

public class TagManagement {
    private ArrayList<Tag> tagArrayList;

    public TagManagement() {
        tagArrayList= new ArrayList<Tag>();
    }

    public boolean isExistTag(Tag tag) {
        boolean exists =false;
        for (Tag t: this.tagArrayList) {
            if (t.getId()==tag.getId())
            {
                exists=true;
                break;
            }
        }
        return exists;
    }

    public void addTag(Tag tag) {
        if (isExistTag(tag)==false) {
            tag.setId(this.tagArrayList.size());
            this.tagArrayList.add(tag);
        }
    }


}
