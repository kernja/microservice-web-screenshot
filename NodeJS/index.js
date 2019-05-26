//requires
var childProcess = require('child_process');
const { execFile } = require('child_process');

var path = require('path');
var AWS = require('aws-sdk');
var fs = require('fs');

//the binary we need to run
var pbinary = 'phantomjs_linux-x86_64';

//global constants
var CONST_S3BUCKET_CONFIG = process.env.TARGET_S3;
var CONST_SQSREGION_CONFIG = process.env.TARGET_SQS;

//objects that use our constants
var sqs = new AWS.SQS({ region: CONST_S3BUCKET_CONFIG });
var s3bucket = CONST_S3BUCKET_CONFIG;

//our primary event handler
exports.handler = (event, context, callback) => {
    
    //set environment
    //Set the path as described here: https://aws.amazon.com/blogs/compute/running-executables-in-aws-lambda/    
	process.env['PATH'] = process.env['PATH'] + ':' + process.env['LAMBDA_TASK_ROOT'];
    console.log("Setting environment variables");
	console.log(process.env["PATH"]);
	 
    //iterate through messages
    for (var i = 0; i < event.Records.length; i++)
    {
        //get message
        var body = event.Records[i].body;
        
		//get our JSON object
		var obj = JSON.parse(body);
		
		//print it off
		console.log("json: ", obj);
	
		//launching phantomJS
		console.log("launching phantomJS");
		loadWebsite(obj);
    }
    
    //create our response
    const response = {
        statusCode: 200,
        body: JSON.stringify({
            message: 'SQS event processed.',
            input: event,
        }),
    };
    
    //return our response
    callback(null, response);
};

function loadWebsite(json)
{
    //get objects from JSON
    var url = json["targetUrl"];
    var width = parseInt(json["width"]);
    var height = parseInt(json["height"]);
    var quality = json["quality"];
    var filename = json["filename"];
    
	// Set the path to the phantomjs binary
	var phantomPath = path.join(__dirname, pbinary);
    console.log(phantomPath);
     
	// Arguments for the phantom script
	var processArgs = [path.join(__dirname, 'phantom-script.js'), url, width, height, quality, '/tmp/' + filename];
	console.log(processArgs);
	 
	// Launch the child process
	console.log("Launching child process"); 
    var child = childProcess.spawn(phantomPath, processArgs);
    
    //define our callbacks for stderr and stdout	
    child.stderr.on('data', function(data) {
	    console.log("STDERR Response: " + data);
	});
	child.stdout.on('data', function(data) {
	    console.log("STDOUT Response: " + data);
	});
	
	//phantomjas closed out
	child.on('close', function(code, signal) {
		console.log("closing phantom JS w/ code:");
		console.log(code)
		
		//only write to S3 if successfull
		if (code == 0)
		{
			  console.log("Punting file to S3, after close");	
			  putObjectToS3(filename, '/tmp/' + filename);
		}
	});    

}
function putObjectToS3(filename, path){
    
    //create S3 instance
    var s3 = new AWS.S3();
    
    //read the file from our path
    console.log("Reading cached file");
    
    try {
        fs.readFile(path, function (err, file_buffer) {
            //create the params for the S3 file
            var params = {
                Bucket: s3bucket,
                Key: filename,
                Body: file_buffer,
                ACL:'public-read'
            };
            
            //put the object
            console.log("Putting the object into S3");            
            s3.putObject(params, function (err, pres) {
                if (err) {
                    console.log("Unable to save file to S3");
                } else {
                    console.log("File saved to S3");
                }
            });
        });
    } catch (e)
    {
    	//something happened
    	//write the error to the log
        console.log("Unable to write file to S3");
        console.log(e);
    }
}