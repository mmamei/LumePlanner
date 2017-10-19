




 var mIcons = {
 "resting": "fa,fa-bed,orange",
 "attractions":"fa,fa-map-marker,red",
 "tree":"fa,fa-tree,darkgreen",
 "eating":"fa,fa-cutlery,darkred",
 "parks":"fa,fa-tree,green",
 "parking":"fa,fa-product-hunt,purple",
 "medical":"fa,fa-ambulance,gray",
 "lifestyle":"fa,fa-glass,black",
 "notterossa":"fa,fa-moon-o,red"
 };



var markerIcons = {};
for(k in mIcons) {
    var x = mIcons[k].split(",");
    for(var size=2;size<=5;size++) {
        markerIcons[k+size] = L.divIcon({
            type: 'div',
            className: 'marker',
            html: "<span class='fa-stack fa-" + size + "x'>" +
            "<i class='fa fa-circle fa-stack-2x' style='color:" + x[2] + "'></i> " +
            "<i class='fa " + x[1] + " fa-stack-1x fa-inverse'></i> " +
            "</span>"
        })
    }
}


var startIcon = L.divIcon({
    type: 'div',
    className: 'marker',
    html: "<span class=\"fa-col-blue fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
});

var endIcon = L.divIcon({
    type: 'div',
    className: 'marker',
    html: "<span class='fa-stack fa-lg'>" +
    "<i class='fa fa-circle fa-stack-2x'></i> " +
    "<i class='fa fa-flag fa-stack-1x fa-inverse'></i> " +
    "</span>"
});

var centerIcon = L.divIcon({
    type: 'div',
    className: 'marker',
    html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
});

var pathStyle = {
    fillColor: "green",
    weight: 5,
    opacity: 1,
    color: 'blue',
    dashArray: '3',
    fillOpacity: 0.7
};