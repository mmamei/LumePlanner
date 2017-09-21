function parsePlan(items) {
    //console.log(items);

    var m = {};
    if ((items.departure.place_id === "0" && items.arrival.place_id === "00")
        || items.departure.place_id === items.arrival.place_id ) {

        m[items.departure.place_id] = {
            lat : items.departure.geometry.coordinates[1],
            lng: items.departure.geometry.coordinates[0],
            message:  "<p style='background-color:white !important;color:#084265 !important;'><b>Partenza:" + items.departure_time + "</b><br />" +
            "<b>Fine:" + items.arrival_time + "</b><br />" +
            format_name(items.arrival.display_name)+"</p>",
            icon: L.divIcon({
                type: 'div',
                className: 'marker',
                html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
            })
        };

    } else {

        m[items.arrival.place_id] = {
            lat : items.arrival.geometry.coordinates[1],
            lng: items.arrival.geometry.coordinates[0],
            message:  "<p style='background-color:white !important;color:#084265 !important;'><b>Arrivo:" + items.arrival_time + "</b><br />" +
            format_name(items.arrival.display_name)+"</p>",
            icon: L.divIcon({
                type: 'div',
                className: 'marker',
                html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-flag-checkered fa-stack-2x\"></i></span>"
            })
        };

        m[items.departure.place_id] = {
            lat : items.departure.geometry.coordinates[1],
            lng: items.departure.geometry.coordinates[0],
            message:  "<p style='background-color:white !important;color:#084265 !important;'><b>Partenza:"+items.departure_time+"</b><br />" +
            format_name(items.departure.display_name)+"</p>",
            icon: L.divIcon({
                type: 'div',
                className: 'marker',
                //html: "<span class=\"fa-col-green fa-stack fa-lg\"><i class=\"fa fa-home fa-stack-2x\"></i></span>"
                html: "<span class=\"fa-col-blue\"><i class=\"fa fa-dot-circle-o fa-3x fa-rotate-dyn\"></i></span>"
            })
        };
    }

    var j;
    for (j = 0; j < items.visited.length; j+=1) {
        m[items.visited[j].visit.place_id] = {
            lat : items.visited[j].visit.geometry.coordinates[1],
            lng: items.visited[j].visit.geometry.coordinates[0],
            message:  "<p style='background-color:white !important;color:#084265 !important;'><b>"+"Arrivo:"+items.visited[j].arrival_time +
            "</b><br /><b>Partenza:" + items.visited[j].departure_time
            +"</b><br />"+format_name(items.visited[j].visit.display_name)+"</p>",
            icon: L.divIcon({
                type: 'div',
                className: 'marker',
                html: "<span class=\"fa-col-red fa-stack fa-lg\"><i class=\"fa fa-circle fa-stack-2x\"></i><i class=\"fa fa-inverse fa-stack-1x\">"+(j+1)+"</i></span>"
            })


        };

    }
    var i;
    for (i = j+1; (i-j-1) < items.to_visit.length; i+=1) {
        m[items.to_visit[i-j-1].visit.place_id] = {
            lat : items.to_visit[i-j-1].visit.geometry.coordinates[1],
            lng: items.to_visit[i-j-1].visit.geometry.coordinates[0],
            message:  "<p style='background-color:white !important;color:#084265 !important;'><b>"+"Arrivo:"+items.to_visit[i-j-1].arrival_time +
            "</b><br /><b>Partenza:" + items.to_visit[i-j-1].departure_time
            +"</b><br />"+format_name(items.to_visit[i-j-1].visit.display_name)+"</p>",
            icon: L.divIcon({
                type: 'div',
                className: 'marker',
                html: "<span class=\"fa-col-blue fa-stack fa-lg\"><i class=\"fa fa-circle fa-stack-2x\"></i><i class=\"fa fa-inverse fa-stack-1x\">"+i+"</i></span>"

            })
        };
    }


    var markers = new L.LayerGroup();
    //console.log(m);
    for(x in m)
        L.marker(m[x],{icon:m[x].icon}).bindPopup(m[x].message).addTo(markers)



    //L.geoJSON(geojson.data, {style: geojson.style}).addTo(mymap);

    /*
     console.log(format_name(items.departure.display_name)+" start at: "+items.departure_time)
     console.log(format_name(items.arrival.display_name)+" arrive at: "+items.arrival_time)
     console.log(items.to_visit.length)
     for(var i=0; i<items.to_visit.length;i++) {
     console.log(format_name(items.to_visit[i].visit.display_name)+" arrive at: "+items.to_visit[i].arrival_time+" depart at: "+items.to_visit[i].departure_time )
     }
     */
    //$("#table").append("<h3>Itinerary</h3>")


    var desc = "";
    desc = desc.concat("<div class=\"alert alert-info\">"+format_name(items.departure.display_name)+"<br><strong>parti alle: </strong>"+items.departure_time+"</div>");
    desc = desc.concat("<div class=\"alert alert-info\">"+format_name(items.arrival.display_name)+"<br><strong>arrivi alle: </strong>"+items.arrival_time+"</div>");
    for(var i=0; i<items.to_visit.length;i++)
        desc = desc.concat("<div class=\"alert alert-info\">"+format_name(items.to_visit[i].visit.display_name)+
            "<br><span style='font-size: small'><strong> arrivi alle: </strong>"+items.to_visit[i].arrival_time+"<br><strong> parti alle: </strong>"+items.to_visit[i].departure_time+"</span></div>")

    return {"markers":markers,"desc":desc}
}



var plans_desc = {};
var plans_markers = {};





$(document).ready(function() {

    var user = JSON.parse(window.localStorage.getItem("user"));
    visitplan = JSON.parse(window.sessionStorage.getItem("visitplan"));
    var type_of_plan = JSON.parse(window.sessionStorage.getItem("type_of_plan"));
    //console.log(visitplan.plans)
    type_of_plan = Object.keys(visitplan.plans)[2];
    console.log(visitplan);

    for (k in visitplan.plans) {
        var info = parsePlan(visitplan.plans[k]);
        var newKey = markerKey2Name(k);

        plans_desc[newKey] = info.desc;
        plans_markers[newKey] = info.markers
    }

    console.log("------");
    console.log(plans_markers);



    var newplan = true; // used to resume plan

    visitplan.selected = type_of_plan;
    //console.log(type_of_plan)


    var minLat = 1000;
    var minLon = 1000;
    var maxLat = -1000;
    var maxLon = -1000;
    for(var i=0; i<visitplan.plans.asis.to_visit.length;i++) {

        var lat = visitplan.plans.asis.to_visit[i].visit.geometry.coordinates[1];
        var lon = visitplan.plans.asis.to_visit[i].visit.geometry.coordinates[0];

        minLat = Math.min(minLat, lat);
        minLon = Math.min(minLon, lon);
        maxLat = Math.max(maxLat, lat);
        maxLon = Math.max(maxLon, lon)
    }


    var lat  = visitplan.plans[type_of_plan].departure.geometry.coordinates[1];
    var lng  = visitplan.plans[type_of_plan].departure.geometry.coordinates[0];
    console.log(type_of_plan+" --> "+markerName2Key(type_of_plan));
    mymap = L.map('mapid',{
        attributionControl: false,
        scrollWheelZoom: true,
        doubleClickZoom: true,
        zoomControl: true,
        touchZoom: true,
        dragging: true,
        layers: [plans_markers[markerKey2Name(type_of_plan)]]
    });


    mymap.fitBounds([
        [minLat, minLon],
        [maxLat, maxLon]
    ]);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(mymap);
    L.control.layers(plans_markers,null).addTo(mymap);
    //$.when(translateObjKeys(plans_markers)).done(function(){L.control.layers(plans_markers,null).addTo(mymap)})

    mymap.on('baselayerchange', function(eo) {
        console.log(markerName2Key(eo.name));
        type_of_plan = markerName2Key(eo.name)
    });

    /*
    new L.Control.jQueryDialog({
        dialogId: 'dialog-about',
        tooltip: "How to use this thing",
        iconClass: 'fa fa-question-circle'
    }).addTo(mymap);
    */

    L.easyButton('fa-book fa-lg', function(btn, map) {
        $("#dialog-about").dialog('option', 'title', markerKey2Name(type_of_plan));
        $("#dialog-about").html(plans_desc[markerKey2Name(type_of_plan)]);
        $('#dialog-about').dialog('open');
    }).addTo(mymap);


    $('#dialog-about').dialog({
        modal: true, autoOpen: false, closeOnEsc: false, draggable: false, maxWidth:700,
    });



    /*
    $(".path").click(function(event) {
        console.log(event.target.id);
        type_of_plan = event.target.id;
        visitplan.selected = type_of_plan;
        $(".path").attr('class', 'btn btn-primary btn-lg path');
        $("#"+event.target.id).attr('class', 'btn btn-success btn-lg path');

        drawMapAndTable(mymap, visitplan.plans[type_of_plan]);
    });
    */


    $("#start").click(function(){
        $(this).css("opacity","0.5");
        window.sessionStorage.setItem("type_of_plan",JSON.stringify(type_of_plan));

        if(newplan) {
            $.postJSON(conf.dita_server+"accept_plan",visitplan,function(data, status){
                console.log(data);
                if(data) window.location.href = "next_step.html";
                else console.log("Cannot create plan")
            });
        }
        else window.location.href = "next_step.html"
    });
});