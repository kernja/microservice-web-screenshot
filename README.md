# Web Capture

A simple microservice that captures a screenshot of a URL and saves the image to an S3 bucket. 

This project serves as an example of how to:

* Read request body
* Return a 200 SUCCESS result
* Return a 4xx FAILURE result
* Serialize and deserialize objects with Gson
* Package code into a ZIP file for deployment
* Save files to an S3 bucket
* Use the simple queue service
* Utilize both Java and Node.J8 in a Lambda micro service 
* Enable CORS with Lambda proxy services
* Run executables from a Lambda container
* Internationalize text (Java)

## Tools and Technologies

This project assumes that you are using:

* IntelliJ community
* Java 8
* Node.JS 8
* AWS Lambda
* AWS API Gateway w/ Lambda Proxy integration

## Running the Code

As there are two separate projects, and using AWS, there's a bit more configuration involved.

The gist:

* Create a simple queue service (SQS)
* Create an API gateway
* Create an S3 bucket
* Create two Lambda microservices, one Java and one Node.JS
	* Hook up the API Gateway as a trigger to the Java microservice
	* Hook up the SQS as a trigger to the Node.JS microservice
* Configure microservices to use appropriate configurations and permissions to access
	* S3 putting/reading files
	* SQS putting/reading messages
	* CloudWatch creating logs
* Configure environment variables within Lambda dashboard
	* Java
		* Target_S3Bucket
		* Target_SQSQueue
	* NodeJS
		* TARGET_S3
		* TARGET_SQS
 
## Sample Project

There's a demo project under the "Example" folder, that will perform the actions needed to:

* Submit a JSON request with a placeholder image to screenshot a URL, and
* Allow the user to refresh the placeholder image to determine if the URL has a screenshot

## Acknowledgements

* Readme borrowed from [PurpleBooth](https://gist.githubusercontent.com/PurpleBooth/109311bb0361f32d87a2/raw/8254b53ab8dcb18afc64287aaddd9e5b6059f880/README-Template.md)
* [PhantomJS](http://phantomjs.org/) was used for screenshotting
* Project was inspired by [TylerPachal](https://github.com/TylerPachal/lambda-node-phantom)