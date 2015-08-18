package com.magicmicky.habitrpgwrapper.lib.api.TypeAdapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by magicmicky on 15/05/15.
 */
public class TagsAdapter extends TypeAdapter<List<TaskTag>>{

    @Override
    public void write(JsonWriter out, List<TaskTag> value) throws IOException {
        out.beginObject();
        for(TaskTag tag : value) {
            out.name(tag.getTag().getId());
            out.value(true);
        }
        out.endObject();
    }

    private boolean alreadyContainsTag(List<TaskTag> list, String idToCheck)
    {
        for(TaskTag t : list)
        {
            if(t.getTag().getId().equals(idToCheck)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<TaskTag> read(JsonReader in) throws IOException {
        List<TaskTag> tags = new ArrayList<>();
        List<Tag> allTags = new Select()
                .from(Tag.class)
                .queryList();
        boolean isClosed=false;
        do {
            switch(in.peek()) {
                case BEGIN_OBJECT:
                    in.beginObject();
                    break;
                case NAME:
                    String tagId = in.nextName();

                    if(in.nextBoolean()) {
                        TaskTag taskTag = new TaskTag();
                        for (Tag tag : allTags) {
                            if (tag.getId().equals(tagId)) {
                                taskTag.setTag(tag);

                                if(!alreadyContainsTag(tags, tagId))
                                {
                                    tags.add(taskTag);
                                }

                                break;
                            }
                        }
                    }
                    break;
                case END_OBJECT:
                    in.endObject();
                    isClosed=true;
                    break;
                case BEGIN_ARRAY:
                    in.beginArray();
                    break;

                case END_ARRAY:
                    in.endArray();
                    isClosed = true;
                    break;
                default:
            }
        } while(!isClosed);
        return tags;
    }
}
