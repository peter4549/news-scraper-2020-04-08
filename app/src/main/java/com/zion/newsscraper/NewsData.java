package com.zion.newsscraper;
import android.os.Parcel;

import java.io.Serializable;

public class NewsData implements Serializable {
    private String title;
    private String originalLink;
    private String description;
    private String pubDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pudDate) {
        this.pubDate = pudDate;
    }
}
