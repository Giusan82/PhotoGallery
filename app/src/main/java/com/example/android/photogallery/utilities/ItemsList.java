package com.example.android.photogallery.utilities;


public class ItemsList {

    private static final String NO_TITLE = "null";
    private String mID;
    private String mTitle; //this is a title of picture
    private String mAuthor; //this is a author of picture
    private String mImage_small;
    private String mImage_raw;
    private int mNumberOfViews; //number of views
    private int mDownloads; //number of downloads
    private int mLikes; //number of likes
    private String mPublishedDate; //this gets the date of publication

    /**
     * Constructor
     */
    public ItemsList(String id, String title, String author, String imageUrl_small, String image_raw, int views, int downloads, int likes, String publishedDate) {
        this.mID = id;
        this.mTitle = title;
        this.mAuthor = author;
        this.mImage_small = imageUrl_small;
        this.mImage_raw = image_raw;
        this.mNumberOfViews = views;
        this.mDownloads = downloads;
        this.mLikes = likes;
        this.mPublishedDate = publishedDate;
    }

    /**
     * Getter
     **/
    public String getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getImage_small() {
        return mImage_small;
    }

    public String getImage_raw() {
        return mImage_raw;
    }

    public int getNumberOfViews() {
        return mNumberOfViews;
    }

    public int getDownloads() {
        return mDownloads;
    }

    public int getLikes() {
        return mLikes;
    }

    public String getPublishedDate() {
        return mPublishedDate;
    }

    /**
     * Setter
     **/
    public void setID(String id) {
        this.mID = id;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public void setImage_small(String image_small) {
        this.mImage_small = image_small;
    }

    public void setImage_raw(String image_raw) {
        this.mImage_raw = image_raw;
    }

    public void setNumberOfViews(int numberOfViews) {
        this.mNumberOfViews = numberOfViews;
    }

    public void setDownloads(int downloads) {
        this.mDownloads = downloads;
    }

    public void setLikes(int likes) {
        this.mLikes = likes;
    }

    public void setPublishedDate(String date) {
        this.mPublishedDate = date;
    }

    /**
     * Returns whether or not there is an title for that item.
     */
    public Boolean hasTitle() {
        return !mTitle.equals(NO_TITLE);
    }

    @Override
    public String toString() {
        return "ItemsList{" +
                "mID='" + mID + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mAuthor='" + mAuthor + '\'' +
                ", mImage_small='" + mImage_small + '\'' +
                ", mImage_thumb='" + mImage_raw + '\'' +
                ", mNumberOfViews='" + mNumberOfViews + '\'' +
                ", mDownloads='" + mDownloads + '\'' +
                ", mLikes='" + mLikes + '\'' +
                ", mPublishedDate='" + mPublishedDate + '\'' +
                '}';
    }
}
