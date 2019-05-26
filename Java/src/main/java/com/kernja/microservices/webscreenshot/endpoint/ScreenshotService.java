package com.kernja.microservices.webscreenshot.endpoint;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kernja.microservices.webscreenshot.endpoint.models.CaptureModel;
import com.kernja.microservices.webscreenshot.endpoint.models.RequestModel;
import com.kernja.microservices.webscreenshot.endpoint.models.ResponseModel;
import com.kernja.microservices.webscreenshot.endpoint.models.S3Model;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.*;

public class ScreenshotService implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
{
        //define our constants
        private final String CONST_S3BUCKET_CONFIG = "Target_S3Bucket";
        private final String CONST_SQS_CONFIG = "Target_SQSQueue";
        private final String CONST_RESOURCES = "strings";

        //member variables
        private String mConfig_S3Bucket;
        private String mConfig_SQSQueue;
        //our resource file with names
        ResourceBundle mResourceBundle;

        //request handler
        @Override
        public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context)
        {
            //get our resource bundle
            mResourceBundle = ResourceBundle.getBundle(CONST_RESOURCES);

            //lambda logger
            LambdaLogger logger = context.getLogger();
            logger.log(mResourceBundle.getString("log.verbose.screenshotservice.loadlambda"));

            //get environment config from Lambda dashboard
            logger.log(mResourceBundle.getString("log.verbose.screenshotservice.getenvironmentconfig"));
            mConfig_S3Bucket = System.getenv(CONST_S3BUCKET_CONFIG);
            mConfig_SQSQueue = System.getenv(CONST_SQS_CONFIG);

            //ensure that our config values aren't null
            if (mConfig_SQSQueue == null || mConfig_S3Bucket == null)
            {
                logger.log(mResourceBundle.getString("log.error.screenshotservice.missingconfigurations"));
                return createFailResponse(logger, new NullPointerException());
            } else {
                //log.verbose.screenshotservice.
                logger.log(String.format(mResourceBundle.getString("log.debug.screenshotservice.environmentvariable.s3"), mConfig_S3Bucket));
                logger.log(String.format(mResourceBundle.getString("log.debug.screenshotservice.environmentvariable.sqs"), mConfig_SQSQueue));
            }

            //successfully got configuration, continue
            //parser for JSON
            Gson gson = new GsonBuilder().create();

            //try parsing the request.
            //it can be either GET or POST, though we should check for it being POST
            S3Model s3Model = null;
            try {
                    //deserialize
                    RequestModel requestModel = null;

                    //now try to do the request ody
                    String bodyStr = event.getBody();
                    if (bodyStr != null && !bodyStr.isEmpty()) {
                            logger.log(mResourceBundle.getString("log.verbose.screenshotservice.parsebody"));
                            requestModel = gson.fromJson(bodyStr, RequestModel.class);

                            //validate
                            if (requestModel.validateModel(logger))
                            {
                                s3Model = createPlaceholderAndCaptureRequest(requestModel, logger);
                            } else {
                                throw new InvalidObjectException(mResourceBundle.getString("log.error.screenshotservice.invalidbody"));
                            }
                    } else {
                            throw new InvalidObjectException(mResourceBundle.getString("log.error.screenshotservice.missingbody"));
                    }

                    logger.log(mResourceBundle.getString("log.verbose.screenshotservice.sqs.attempt"));
                    //create our capture model model
                    CaptureModel cm = new CaptureModel(s3Model.getFilename(), requestModel.getUrl(), requestModel.getWidth(), requestModel.getHeight(),requestModel.getQuality());

                    //send a message to the other queue
                    final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
                    SendMessageResult smr = sqs.sendMessage(new SendMessageRequest()
                            .withQueueUrl(mConfig_SQSQueue)
                            .withMessageBody(gson.toJson(cm)));

                    //log additional information
                    logger.log(smr.getSdkResponseMetadata().toString());
                    logger.log(String.format(mResourceBundle.getString("log.debug.screenshotservice.sqs.sendsuccess"), smr.getMessageId(), smr.getSequenceNumber()));

                    //create our response model
                    ResponseModel responseModel = new ResponseModel(s3Model.getUrl());
                    String responseBodyString = new JSONObject(gson.toJson(responseModel)).toString();

                    //create our response object
                    APIGatewayProxyResponseEvent response = createSuccessResponse(logger, responseBodyString);

                    //log the response
                    logger.log(response.toString());

                    //return
                    return response;
            } catch (Exception e) {
                    //create and return our exception
                    return createFailResponse(logger, e);
            }
        }

        private APIGatewayProxyResponseEvent createSuccessResponse(LambdaLogger pLogger, String pResponseBody)
        {
                //create our response object
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

                //set status code
                response.setStatusCode(200);

                //create response body
                response.setBody(pResponseBody);

                //set the responsemapping
                Map map = new HashMap<String, String>();
                map.put("Access-Control-Allow-Origin", "*");
                response.setHeaders(map);

                //return
                pLogger.log(mResourceBundle.getString("log.verbose.screenshotservice.request.successupdate"));
                return response;

        }
        private APIGatewayProxyResponseEvent createFailResponse(LambdaLogger pLogger, Exception pException)
        {
                //create our response object
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

                //set status code
                response.setStatusCode(400);

                //create response body
                Map<String, String> responseBody = Collections.singletonMap("message", pException.toString());
                response.setBody(new JSONObject(responseBody).toString());

                //return
                pLogger.log(mResourceBundle.getString("log.error.screenshotservice.request.failureupdate"));
                return response;

        }

        private S3Model createPlaceholderAndCaptureRequest(RequestModel pModel, LambdaLogger pLogger) throws Exception
        {
                //create our model
                S3Model result = new S3Model();

                //generate filename
                pLogger.log(mResourceBundle.getString("log.verbose.screenshotservice.generatefilename"));
                String fileName = UUID.randomUUID().toString() + UUID.randomUUID().toString();
                String filePath = "/tmp/"  + fileName;

                try {
                        pLogger.log(mResourceBundle.getString("log.verbose.screenshotservice.decodingfile"));

                        //decode the temporary file
                        byte[] decodedBytes = Base64.getDecoder().decode(pModel.getFallbackImage());

                        File file = new File(filePath);
                        FileUtils.writeByteArrayToFile(file, decodedBytes);

                        pLogger.log(mResourceBundle.getString("log.verbose.screenshotservice.writings3"));
                        //get our client
                        AmazonS3Client s3Client = (AmazonS3Client) AmazonS3ClientBuilder.defaultClient();

                        //put the file into storage
                        s3Client.putObject(new PutObjectRequest(mConfig_S3Bucket, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));

                        //return the filename and url
                        return new S3Model(fileName, s3Client.getResourceUrl(mConfig_S3Bucket, fileName));

                } catch (Exception e)
                {
                        pLogger.log(mResourceBundle.getString("log.error.screenshotservice.errorplaceholder"));
                        pLogger.log(e.getMessage());

                        throw e;
                }
        }
}
