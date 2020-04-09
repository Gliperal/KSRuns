function runTimeToString(time)
{
	if (time == null)
		return "unknown";
	var str = "";
	if (time >= 3600)
		str += Math.floor(time / 3600) + ":";
	str += Math.floor((time / 60) % 60) + ":";
	if (time % 60 < 10)
		str += "0";
	str += time % 60;
	return str;
}

function runDateToString(date)
{
	if (date == null)
		return "unknown";
	else
		return date.toString();
}

function runComparator_time(run1, run2)
{
	if (run1.runTime == run2.runTime)
		return runComparator_date(run1, run2);
	if (run1.runTime < 0)
		return 1;
	if (run2.runTime < 0)
		return -1;
	return run1.runTime - run2.runTime;
}

function runComparator_date(run1, run2)
{
	if (run1.date == null)
		return 1;
	if (run2.date == null)
		return -1;
	return new Date(run1.date).getTime() - new Date(run2.date).getTime();
}

function categoryComparator_id(cat1, cat2)
{
	return cat1.id - cat2.id;
}

var regex_youtube = /^(http:\/\/|https:\/\/|)(www.|)(youtube.com|youtu.be)/i
var regex_twitch = /^(http:\/\/|https:\/\/|)(www.|)twitch.tv/i

function videoLink(url)
{
	if (url == null)
		return "";

	var text = "Video";
	if (regex_youtube.test(url))
		text = "Watch on YouTube";
	if (regex_twitch.test(url))
		text = "Watch on Twitch";
	return '<a class="videoLink" href="' + url + '" target="_blank">' + text + '</a>'
}

function playerLink(name, link)
{
	if (link == null)
		return name.toString();
	else
		return '<a class="playerLink" href="' + link + '" target="_blank">' + name + '</a>';
}
