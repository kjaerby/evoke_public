
var player = undefined;

window.playVideo = function(playerID)
{
if (!player)
{
player = new PeerTubePlayer(document.getElementById(playerID));
}

player.ready.then(e => player.play());
}

window.pauseVideo = function pauseVideo(playerID)
{
if (!player)
{
player = new PeerTubePlayer(document.getElementById(playerID));
}

player.ready.then(e => player.pause());
}

window.seekVideo = function seekVideo(playerID, timestamp)
{
if (!player)
{
player = new PeerTubePlayer(document.getElementById(playerID));
}

player.ready.then(e => player.seek(timestamp));
}

window.getPlayer = function (playerID) {
if (!player)
{
player = new PeerTubePlayer(document.getElementById(playerID));
}

return player;
}