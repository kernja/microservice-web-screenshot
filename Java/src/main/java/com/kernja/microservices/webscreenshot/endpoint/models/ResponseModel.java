package com.kernja.microservices.webscreenshot.endpoint.models;
;

import com.google.gson.annotations.SerializedName;

public class ResponseModel {

    //define the member variable
    @SerializedName("imageUrl")
    String mUrl = "";

    //getters and setters
    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String pUrl) {
        //set the output
        this.mUrl = pUrl;
    }

    //constructor
    public ResponseModel(String pUrl)
    {
        this.setUrl(pUrl);
    }
}
