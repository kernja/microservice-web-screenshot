package com.kernja.microservices.webscreenshot.endpoint.models;

import com.google.gson.annotations.SerializedName;

public class CaptureModel {

    //define the member variables
    @SerializedName("filename")
    String mFilename;

    @SerializedName("targetUrl")
    String mUrl;

    @SerializedName("width")
    int mWidth;

    @SerializedName("height")
    int mHeight;

    @SerializedName("quality")
    int mQuality;

    //constructor
    public CaptureModel(String pFilename, String pTargetUrl, int pWidth, int pHeight, int pQuality)
    {
        mFilename = pFilename;
        mUrl = pTargetUrl;
        mWidth = pWidth;
        mHeight = pHeight;
        mQuality = pQuality;
    }

}
