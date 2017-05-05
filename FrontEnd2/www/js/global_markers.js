function Markers() {
    this.hotels  = new L.LayerGroup();
    this.attractions  = new L.LayerGroup();
    this.monuments = new L.LayerGroup();
    this.museums = new L.LayerGroup();
    this.restaurants = new L.LayerGroup();
    this.parks = new L.LayerGroup();
    this.historical = new L.LayerGroup();
    this.religious = new L.LayerGroup();
}


var markerIcons = {};
markerIcons["hotels"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-bed',
    markerColor: 'orange'
});
markerIcons["attractions"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-map-marker',
    markerColor: 'red'
});
markerIcons["monuments"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-home',
    markerColor: 'darkred'
});
markerIcons["museums"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-home',
    markerColor: 'blue'
});
markerIcons["restaurants"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-cutlery',
    markerColor: 'darkgreen'
});
markerIcons["parks"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-tree',
    markerColor: 'green'
});
markerIcons["historical"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-history',
    markerColor: 'purple'
});
markerIcons["religious"] = L.AwesomeMarkers.icon({
    //icon: 'glyphicon-cog',
    prefix: 'fa',
    icon: 'fa-microchip',
    markerColor: 'darkpuple'
});
