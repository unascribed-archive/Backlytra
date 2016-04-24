// based on https://gist.github.com/insin/b1a294e440548e0d5860
var fs = require('fs-extra');
var os = require('os');
var path = require('path');

var home = os.homedir();

console.log(home);

var objects = require(path.join(home, '.minecraft/assets/indexes/1.9.json')).objects;

for (var filePath in objects) {
	if (!/\.lang$/.test(filePath)) continue;

	var copyPath = filePath.replace('minecraft/', './');
	var hash = objects[filePath].hash;
	var objectPath = path.join(home, '.minecraft/assets/objects/', hash.substring(0, 2), hash);
	console.log(objectPath, '->', copyPath);
	fs.copySync(objectPath, copyPath);
}
