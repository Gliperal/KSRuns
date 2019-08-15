var g_levels;
var g_numLevels = 300;
var g_levelsTableWidth = -1;
var g_sizeData = [240, 24];

function resize(size)
{
	if (size == 0)
	{
		g_sizeData = [240, 24];
		document.getElementById("levelButtonStyle").setAttribute("href", "indexSmall.css");
	}
	else if (size == 1)
	{
		g_sizeData = [355, 40];
		document.getElementById("levelButtonStyle").setAttribute("href", "indexBig.css");
	}
}

function buildIndividualLevel(index)
{
	if(index >= g_numLevels)
		return '<div class="emptyLevelBG"><div class="emptyLevelSize"></div></div>';

	level = g_levels[index];
	id = level.id;
	if("code" in level)
		code = level.code;
	else
		code = "404";
	author = level.author;
	name = level.name;
	description = level.description;

	return	  '<a class="levelLink" href="level/' + code + '">				'
		+ '	<div class="levelBG">							'
		+ '		<table cellpadding=0 cellspacing=0>				'
		+ '			<tr>							'
		+ '				<td rowspan="2">				'
		+ '					<img src="images/icons/Icon' + id + '.png" width=' + g_sizeData[1] + 'px onerror="this.onerror=null;this.src=\'images/icons/Icon.png\';"/>	'
		+ '				</td>						'
		+ '				<td class="levelName">				'
		+ '					<div class="levelSizeRestrictor">	'
		+ 						name + ' (' + author + ')'
		+ '					</div>					'
		+ '				</td>						'
		+ '			</tr>							'
		+ '			<tr>							'
		+ '				<td class="levelDescription">			'
		+ '					<div class="levelSizeRestrictor">	'
		+ 						description
		+ '					</div>					'
		+ '				</td>						'
		+ '			</tr>							'
		+ '		</table>							'
		+ '	</div>									'
		+ '</a>										';
}

function buildLevelsTableRow(index, width)
{
	var rowHTML = '<tr><td>';
	lastIndex = index + width - 1;
	while(index < lastIndex)
	{
		rowHTML += buildIndividualLevel(index)
		rowHTML += '</td><td class="levelColumnSeparator"/><td>'
		index++;
	}
	rowHTML += buildIndividualLevel(index)
	rowHTML += '</td></tr>';
	return rowHTML;
}

function levelComparator(dead, parrot)
{
	deadFolder = (dead.author + " - " + dead.name).toLowerCase();
	parrotFolder = (parrot.author + " - " + parrot.name).toLowerCase();
	if(deadFolder < parrotFolder)
		return -1;
	if(deadFolder > parrotFolder)
		return 1;
	return 0;
}

function buildLevelsTable(spaceAvailable)
{
	var width = Math.floor((spaceAvailable - 100) / g_sizeData[0]);
	if(width < 1)
		width = 1;
	if(width == g_levelsTableWidth)
		// Table already perfect; move on.
		return;
	
	// Build the HTML table one row at a time
	var tableHTML = '<table cellpadding=0 cellspacing=0>';
	var index = 0;
	while(index < g_numLevels)
	{
		tableHTML +=		buildLevelsTableRow(index, width);
		tableHTML +=		'<tr class="levelRowSeparator"/>';
		index += width;
	}
	tableHTML += '</table>';
	
	document.getElementById("levelsContainer").innerHTML = tableHTML;
}

function loadLevelJSON(json)
{
	g_levels = JSON.parse(json);
	g_levels.sort(levelComparator);

	g_numLevels = g_levels.length;
}

function loadLevels()
{
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function()
	{
		if (this.readyState == 4)
		{
			if (this.status == 200)
			{
				loadLevelJSON(xhttp.responseText);
				var levelsSection = document.getElementById('dynamicLevelsContainer');
				buildLevelsTable(levelsSection.clientWidth);
			}
			else
				document.getElementById("levelsContainer").innerHTML = 'There was a problem loading the levels data. <img src="https://cdn.frankerfacez.com/emoticon/20706/1"\>';
		}
	};
	var origin = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
	if (window.location.port == 8080)
		xhttp.open("GET", origin + "/KSRuns/api/levels?queryString", true);
	else
		xhttp.open("GET", origin + "/api/levels?queryString", true);
	xhttp.send();
}

function dynamicLevelsTableInit()
{
	var levelsSection = document.getElementById('dynamicLevelsContainer');
	new ResizeSensor(levelsSection, function()
		{
			buildLevelsTable(levelsSection.clientWidth);
		}
	);
}