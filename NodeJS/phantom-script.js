//declarations
var system = require('system');
var page = require('webpage').create();
console.log("Just finished declarations in phantom-script.js");
 
//get our variables from the process
var pUrl = system.args[1];
var pWidth = parseInt(system.args[2]);
var pHeight = parseInt(system.args[3]);
var pQuality = system.args[4];
var pFilename = system.args[5];
console.log("Just finished parameters");

//create stuff
page.onError = function() {};

//render output
page.open(pUrl, function(status) {
    system.stdout.write("Opening page");
    system.stdout.write("status: " + status);
  
	if (status == "success")
	{
		//set viewport size
		page.viewportSize = {
			width: pWidth,
			height: pHeight
		};
		page.clipRect = { top: 0, left: 0, width: pWidth, height: pHeight };
		system.stdout.write("Viewport size is: " + pWidth + "," + pHeight);
		system.stdout.write("Just finished viewport in phantom-script.js");

		//save to a temp file
		system.stdout.write("Attempting to save");
		system.stdout.write(pFilename);
		page.render(pFilename, {format: 'jpeg', quality: pQuality});
 
		//writing to std-out to notify that the service was completed
		system.stdout.write("Saved file, writing std out");
		system.stdout.write("success");

		//exit
		system.stdout.write("Exiting phantom");    	
		phantom.exit();
	} else {
		//write to stderr in case of error
		system.stderr.write("Error loading page in PhantomJS, bailing");
		system.stderr.write("failure");
	}
});