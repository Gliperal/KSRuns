var g_rotators = [];
var g_twoPI = 2 * Math.PI;

function spawn_rotator(angle, distance, speed, type, id)
{
	var rotator_element = document.createElement("IMG");
	rotator_element.src = "images/util/Rotator" + type + ".png";
	rotator_element.className = "rotator";
	rotator_element.id = id;
	rotator_element.alt = "#";
	document.getElementById("rotators").appendChild(rotator_element);
	g_rotators.push({
		"angle": angle * Math.PI / 180,
		"distance": distance / 120,
		"speed": speed * Math.PI / 180 / 10,
		"type": type,
		"id": id
	});
}

function update_rotators()
{
	var rotator_center =
	{
		"x": document.getElementById("rotators").clientWidth / 2,
		"y": document.getElementById("rotators").clientHeight / 2
	};
	var rotator_max_radius = Math.min(rotator_center.x, rotator_center.y);
	for (var i = 0; i < g_rotators.length; i++)
	{
		var rotator = g_rotators[i];
		var x = rotator_center.x + rotator.distance * rotator_max_radius * Math.cos(rotator.angle);
		var y = rotator_center.y + rotator.distance * rotator_max_radius * Math.sin(rotator.angle);
		var img_size;
		if (rotator.type == "C")
			img_size = 3 * rotator_max_radius / 120;
		else
			img_size = 5 * rotator_max_radius / 120;
		rotator.element.style.left = x + "px";
		rotator.element.style.top = y + "px";
		rotator.element.width = img_size;
		rotator.element.height = img_size;
		rotator.angle += rotator.speed;
		if (rotator.angle > g_twoPI)
			rotator.angle -= g_twoPI;
		if (rotator.angle < 0)
			rotator.angle += g_twoPI;
	}
}

function animate_rotators()
{
	setInterval(update_rotators, 1000 / 50);
}

function init_rotators()
{
	for(var i = 0; i < 15; i++)
		spawn_rotator(24 * i, 50, 5, "A", "rotator-a-" + i);
	for(var i = 0; i < 15; i++)
		spawn_rotator(12 * i, 60, -2, "B", "rotator-b-" + i);
	for(var i = 0; i < 90; i++)
		spawn_rotator(4 * i, 70 + Math.random()*23, 1 + Math.random()*4, "C", "rotator-c-" + i);
	for(var i = 0; i < 9; i++)
		spawn_rotator(40 * i, 30, -3, "A", "rotator-d-" + i);
	for(var i = 0; i < 8; i++)
		spawn_rotator(15 * i, 40, 3, "E", "rotator-e-" + i);
	for (var i = 0; i < g_rotators.length; i++)
		g_rotators[i].element = document.getElementById(g_rotators[i].id);
	update_rotators();
	animate_rotators();
}