function verifyRun(runID, verifyOrReject)
{
	var requestData =
	{
			"request": "verify-run",
			"user": LOGIN_USER,
			"sessionID": LOGIN_SESS,
			"pendingRunID": runID,
			"verify": verifyOrReject
	};
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function()
	{
		if (this.readyState == 4 && this.status == 200)
		{
			var response = JSON.parse(xhttp.responseText);
			if ("status" in response)
				document.getElementById("verificationInfo").innerHTML = response["message"];
			else
				document.getElementById("verificationInfo").innerHTML = "Unknown server response.";
		}
	};
	var origin = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
	if (window.location.port == 8080)
		xhttp.open("POST", origin + "/KSRuns/api/submit", true);
	else
		xhttp.open("POST", origin + "/api/submit", true);
	xhttp.setRequestHeader("Content-type", "application/json");
	xhttp.send(JSON.stringify(requestData));
}
