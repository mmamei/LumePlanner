var tpIcons = {};
var mezzi = ["AE","AS","AU","ES","IC","R","walk"];


for(var i=0; i<mezzi.length;i++) {
    tpIcons[mezzi[i]] = L.icon({
        iconUrl: 'img/travelplanner/mezzi/'+mezzi[i]+'.png',
        iconSize:     [17, 21], // size of the icon
        iconAnchor:   [8, 10], // point of the icon which will correspond to marker's location
        popupAnchor:  [10, 10] // point from which the popup should open relative to the iconAnchor
    });
}




