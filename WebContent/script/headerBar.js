var LOGIN_USER = null;
var LOGIN_SESS = null;

function loginBar_init()
{
	document.getElementById("headerBar").innerHTML =
	`
		<table cellpadding=0 cellspacing=0><tr><td class="headerBarLeft">
			<a href="/">
				<img src="/KSRuns/images/util/home.png" alt="KSRuns" title="Home" height="24px">
			</a>
		</td><td class="headerBarRight">
			<div id="loginBar"></div>
		</td></tr></table>
	`;
	displayLogout();
	checkLogin();
}

function displayLogin()
{
	document.getElementById("loginBar").innerHTML =
		'<div>Logged in as: ' + LOGIN_USER +
		'<input type="button" id="input_go" value="Sign out"/>';
	document.getElementById("input_go").addEventListener('click', logout);
	if (typeof onLogin === "function")
		onLogin();
}

function displayLogout()
{
	document.getElementById("loginBar").innerHTML =
	`
		Username: <input class="textInput" type="text" id="input_username" onkeypress="loginKeyPress(event)"/>
		Password: <input class="textInput" type="password" id="input_password" onkeypress="loginKeyPress(event)"/>
		<input class="buttonInput" type="button" id="input_go" value="Go"/>
		<div id="loginBar_error"></div>
	`;
	document.getElementById("input_go").addEventListener('click', login);
}

function loginKeyPress(event)
{
	var keyValue = event.which || event.keyCode;
	if (keyValue == 13)
		login();
}

function checkLogin()
{
	var cookie = "" + decodeURIComponent(document.cookie);
	var crumbs = cookie.split(";");
	for (var i = 0; i < crumbs.length; i++)
	{
		var crumb = crumbs[i];
		while (crumb.charAt(0) == ' ')
			crumb = crumb.substring(1);
		if (crumb.startsWith("username="))
			LOGIN_USER = crumb.substring(9, crumb.length);
		else if (crumb.startsWith("sessionID="))
			LOGIN_SESS = crumb.substring(10, crumb.length);
	}
	if (LOGIN_USER != null && LOGIN_SESS != null)
		displayLogin();
}

function makeCookie(key, value, duration)
{
	var cookie = key + "=" + value + ";";

	var d = new Date();
	d.setTime(d.getTime() + duration);
	cookie += "expires=" + d.toUTCString() + ";";

	cookie += "path=/";

	document.cookie = cookie;
}

function deleteCookie(key)
{
	document.cookie = key + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
}

function login()
{
	var requestData =
	{
			"request": "login",
			"user": document.getElementById("input_username").value,
			"password": document.getElementById("input_password").value
	};

	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function()
	{
		if (this.readyState == 4 && this.status == 200)
		{
			processLogin(xhttp.responseText);
		}
	};
	var origin = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
	if (window.location.port == 8080)
		xhttp.open("POST", origin + "/KSRuns/api/login", true);
	else
		xhttp.open("POST", origin + "/api/login", true);
	xhttp.setRequestHeader("Content-type", "application/json");
	xhttp.send(JSON.stringify(requestData));
}

function processLogin(responseStr)
{
	var response = JSON.parse(responseStr);
	if ("status" in response)
	{
		document.getElementById("loginBar_error").innerHTML = response["message"];
		if (response["status"] == 0)
		{
			makeCookie("username", response["username"], 3600000);
			makeCookie("sessionID", response["sessionID"], 3600000);
			LOGIN_USER = response["username"];
			LOGIN_SESS = response["sessionID"];
			displayLogin();
		}
	}
}

function logout()
{
	document.getElementById("loginBar").innerHTML = "Trying to log out.";

	// Close session on server
	var requestData =
	{
			"request": "logout",
			"user": LOGIN_USER,
			"sessionID": LOGIN_SESS
	};
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function()
	{
		if (this.readyState == 4 && this.status == 200)
		{
			processLogout(xhttp.responseText);
		}
	};
	var origin = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
	if (window.location.port == 8080)
		xhttp.open("POST", origin + "/KSRuns/api/login", true);
	else
		xhttp.open("POST", origin + "/api/login", true);
	xhttp.setRequestHeader("Content-type", "application/json");
	xhttp.send(JSON.stringify(requestData));
}

function processLogout(responseStr)
{
	// TODO Go back to old login bar
	document.getElementById("loginBar").innerHTML = "Successfully logged out.";

	// Destroy cookie
	deleteCookie("username");
	deleteCookie("sessionID");
}
