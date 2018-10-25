function levelCmp(levelA, levelB)
{
	var nameA = levelA["name"];
	var nameB = levelB["name"];
	return nameA.localeCompare(nameB);
}


function catCmp(catA, catB)
{
	return catA["id"] - catB["id"];
}


function loadInputFields()
{
	g_categoriesByLevel = JSON.parse(categoriesByLevelJSON);
	
	sortStuff();
	
	var levelSelect = document.getElementById("select_level");
	if (levelSelect != null)
	{
		// Default level (empty)
		var x = document.createElement("option");
		x.text = "-";
		levelSelect.add(x);
		
		// The rest
		for(var i = 0; i < g_categoriesByLevel.length; i++)
		{
			var level = g_categoriesByLevel[i];
			var x = document.createElement("option");
			x.text = level["name"];
			levelSelect.add(x);
		}
	}
	
	// Time selection
	var timerH = document.getElementById("select_time_hrs");
	var timerM = document.getElementById("select_time_mins");
	var timerS = document.getElementById("select_time_secs");
	if (timerS != null)
	{
		for( var i = 0; i < 24; i++)
		{
			var x = document.createElement("option");
			x.text = i;
			timerH.add(x);
		}
		
		for( var i = 0; i < 60; i++)
		{
			var x = document.createElement("option");
			x.text = i;
			timerM.add(x);
			x = document.createElement("option");
			x.text = i;
			timerS.add(x);
		}
	}
	
	// Date selection
	var dateInput = document.getElementById("input_date");
	if (dateInput != null)
	{
		var today = new Date();
		var year = today.getFullYear();
		var month = today.getMonth() + 1;
		var day = today.getDate();
		if (month < 10)
			month = "0" + month;
		if (day < 10)
			day = "0" + day;
		dateInput.value = year + "-" + month + "-" + day;
	}
}


function sortStuff()
{
	// alphabetize levels
	g_categoriesByLevel.sort(levelCmp);
	
	// order categories by ID
	for(var i = 0; i < g_categoriesByLevel.length; i++)
	{
		var level = g_categoriesByLevel[i];
		var categories = level["categories"];
		categories.sort(catCmp);
	}
}


function updateLevel()
{
	// Load elements
	var levelSelect = document.getElementById("select_level");
	var catSelect = document.getElementById("select_category");
	
	// Delete all options
	catSelect.innerHTML = "";
	
	// Load categories
	if (levelSelect.selectedIndex == 0)
		return;
	var level = g_categoriesByLevel[levelSelect.selectedIndex-1];
	var categories = level["categories"];
	
	// Default category (empty)
	var x = document.createElement("option");
	x.text = "-";
	catSelect.add(x);

	// The rest
	for(var i = 0; i < categories.length; i++)
	{
		var cat = categories[i];
		var x = document.createElement("option");
		x.text = cat["category"];
		catSelect.add(x);
	}
}


function sendRequest(requestData)
{
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function()
	{
		if (this.readyState == 4 && this.status == 200)
		{
			processSubmit(xhttp.responseText);
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


function submit_level()
{
	if (LOGIN_USER == null)
	{
		document.getElementById("error").innerHTML = "You must be logged in to submit levels."
		return false;
	}
	
	var code = document.getElementById("input_code").value;
	var name = document.getElementById("input_name").value;
	var author = document.getElementById("input_author").value;
	var desc = document.getElementById("input_desc").value;
	var download = document.getElementById("input_download").value;
	
	if (code == "")
	{
		document.getElementById("error").innerHTML = "Please input a code for the level."
		return;
	}
	if (name == "")
	{
		document.getElementById("error").innerHTML = "Please input the level name."
		return;
	}
	if (author == "")
	{
		document.getElementById("error").innerHTML = "Please input the level author."
		return;
	}
	
	// Submit new level to database
	var requestData =
	{
			"request": "submit-level",
			"user": LOGIN_USER,
			"sessionID": LOGIN_SESS,
			"code": code,
			"name": name,
			"author": author
	};
	if (desc != "")
		requestData["description"] = desc;
	if (download != "")
		requestData["download"] = download;
	sendRequest(requestData);
}


function submit_cat()
{
	if (LOGIN_USER == null)
	{
		document.getElementById("error").innerHTML = "You must be logged in to submit categories."
		return false;
	}
	
	var levelSelect = document.getElementById("select_level");
	if (levelSelect.selectedIndex == 0)
	{
		document.getElementById("error").innerHTML = "Please select a level."
		return;
	}
	var level = g_categoriesByLevel[levelSelect.selectedIndex-1];
	var levelID = level["id"];
	
	var category = document.getElementById("input_category").value;
	var victory;
	var victoryType = document.getElementById("select_victory").value;
	if (victoryType == "WIN Tile")
	{
		var cutscene = document.getElementById("input_victory").value;
		if (cutscene == "")
			victory = "WIN";
		else
			victory = "WIN:" + cutscene;
	}
	else if (victoryType == "Cutscene")
	{
		var cutscene = document.getElementById("input_victory").value;
		if (cutscene == "")
			victory = "CUT";
		else
			victory = "CUT:" + cutscene;
	}
	else if (victoryType == "Other")
	{
		victory = document.getElementById("input_victory").value;
		if (victory == "")
		{
			document.getElementById("error").innerHTML = "Please input a win condition."
			return;
		}
	}
	
	var difficulty = document.getElementById("input_difficulty").value;
	var cheating = document.getElementById("input_cheating").checked;
	var timeS = document.getElementById("input_time_start").value;
	var timeE = document.getElementById("input_time_end").value;
	var moreRules = document.getElementById("input_more_rules").value;
	
	// Submit new category to database
	var requestData =
	{
			"request": "submit-category",
			"user": LOGIN_USER,
			"sessionID": LOGIN_SESS,
			"levelID": levelID,
			"categoryName": category,
			"victory": victory,
			"cheating": cheating,
	};
	if (difficulty != "")
		requestData["difficulty"] = difficulty;
	if (timeS != "")
		requestData["timeStart"] = timeS;
	if (timeE != "")
		requestData["timeEnd"] = timeE;
	if (moreRules != "")
		requestData["moreRules"] = moreRules;
	sendRequest(requestData);
}


function submit_run()
{
	if (LOGIN_USER == null)
	{
		document.getElementById("error").innerHTML = "You must be logged in to submit runs."
		return false;
	}
	
	var levelSelect = document.getElementById("select_level");
	var catSelect = document.getElementById("select_category");
	if (levelSelect.selectedIndex == 0)
	{
		document.getElementById("error").innerHTML = "Please select a level."
		return;
	}
	if (catSelect.selectedIndex == 0)
	{
		document.getElementById("error").innerHTML = "Please select a category."
		return;
	}
	
	var timerH = document.getElementById("select_time_hrs");
	var timerM = document.getElementById("select_time_mins");
	var timerS = document.getElementById("select_time_secs");
	var time = timerH.selectedIndex*3600 + timerM.selectedIndex*60 + timerS.selectedIndex;
	if (time == 0)
	{
		document.getElementById("error").innerHTML = "Please input a time."
		return;
	}
	
	var level = g_categoriesByLevel[levelSelect.selectedIndex-1];
	var levelID = level["id"];
	var categories = level["categories"];
	var cat = categories[catSelect.selectedIndex-1];
	var catID = cat["id"];
	var date = document.getElementById("input_date").value;
	var video = document.getElementById("input_video").value;
	
	// Submit new level to database
	var requestData =
	{
			"request": "submit-run",
			"user": LOGIN_USER,
			"sessionID": LOGIN_SESS,
			"levelID": levelID,
			"categoryID": catID,
			"time": time,
			"date": date
	};
	if (video != "")
		requestData["video"] = video;
	sendRequest(requestData);
}


function processSubmit(responseStr)
{
	var response = JSON.parse(responseStr);
	if ("status" in response)
		document.getElementById("error").innerHTML = response["message"];
}


function init()
{
	loginBar_init();
}
