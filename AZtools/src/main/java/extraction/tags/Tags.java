package extraction.tags;

import java.util.ArrayList;
import java.util.List;

public class Tags {

    private List<String> tags;

    public Tags(String abstrakt) {
        tags = findTags(abstrakt);
    }

    public List<String> findTags(String abstrakt) {
        tags = new ArrayList<String>();
        return tags;
    }

    public List<String> getTags() {
        return tags;
    }
}
