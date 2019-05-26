package com.kernja.microservices.webscreenshot.endpoint.models;

public class S3Model {

    //member variables
    private String mFilename;
    private String mUrl;

    //getters and setters
    public String getFilename() {
        return mFilename;
    }
    public void setFilename(String mFilename) {
        this.mFilename = mFilename;
    }

    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    //constructors
    public S3Model() { };

    public S3Model(String pFilename, String pUrl)
    {
        mFilename = pFilename;
        mUrl = pUrl;
    }
}
