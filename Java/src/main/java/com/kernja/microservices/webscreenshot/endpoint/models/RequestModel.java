package com.kernja.microservices.webscreenshot.endpoint.models;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.annotations.SerializedName;

import java.net.URL;

public class RequestModel {

    //define the member variables
    @SerializedName("targetUrl")
    String mUrl;

    @SerializedName("width")
    int mWidth;

    @SerializedName("height")
    int mHeight;

    @SerializedName("quality")
    int mQuality;

    @SerializedName("fallbackImage")
    String mFallback;

    //getters and setters
    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String pUrl) {
        this.mUrl = pUrl;
    }
    public int getWidth() {
        return mWidth;
    }
    public void setWidth(int pWidth) {
        this.mWidth = pWidth;
    }
    public int getHeight() {
        return mHeight;
    }
    public void setHeight(int pHeight) { this.mHeight = pHeight; }
    public int getQuality() {
        return mQuality;
    }
    public void setQuality(int pQuality) {
        this.mQuality = pQuality;
    }
    public String getFallbackImage() {
        return mFallback;
    }
    public void setFallbackImage(String pUrl) {
        this.mFallback = pUrl;
    }

    //model validation
    public boolean validateModel(LambdaLogger pLogger) {

        //if logger is null, return false
        if (pLogger == null)
        {
            return false;
        }

        //try seeing if the URLs we have are valid
        pLogger.log("Validating website URL.");
        try {
            URL u = new URL(mUrl);
            pLogger.log("URL was validated.");
        } catch (Exception e)
        {
            pLogger.log("Invalid URL. Bailing.");
            pLogger.log(e.getStackTrace().toString());
            return false;
        }

        //ensure that the image string is not null
        pLogger.log("Validating fallback image.");
        if (mFallback == null || mFallback.isEmpty())
        {
            pLogger.log("Fallback image was null or empty. Bailing.");
            return false;
        }

        pLogger.log("Verifying that the fallback image doesn't have Base64 prefix.");
        if (mFallback.contains(","))
        {
            pLogger.log("Removing prefix.");
            mFallback = mFallback.split(",")[1];
        }

        pLogger.log("Verifying that the fallback image isn't huge.");
        if (mFallback.length() > 128000)
        {
            pLogger.log("Fallback image is bigger than 128KB, give or take. Bailing.");
            return false;
        }

        //ensure that our width and height are valid
        pLogger.log("Validating width and height");

        //if too small
        if (mWidth < 640) {
            mWidth = 640;
            pLogger.log("Adjusting width to be no smaller than 640.");
        }
        if (mHeight < 480)
        {
            mHeight = 480;
            pLogger.log("Adjusting height to be no smaller than 480.");
        }

        //now if too big
        if (mWidth > 3000) {
            mWidth = 3000;
            pLogger.log("Adjusting width to be no larger than 3000.");
        }
        if (mHeight > 3000)
        {
            mHeight = 3000;
            pLogger.log("Adjusting height to be no larger than 3000.");
        }

        //validate quality
        pLogger.log("Validating quality");
        if (mQuality > 100){
            mQuality = 100;
            pLogger.log("Adjusting quality so it's not larger than 100.");
        } else if (mQuality < 0)
        {
            mQuality = 0;
            pLogger.log("Adjusting quality so it's not smaller than 0.");
        }

        //return
        pLogger.log("Model was successfully validated.");
        return true;
    }
}
