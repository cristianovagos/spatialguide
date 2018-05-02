var map;
var pos = 30;

function initMap() {
        var mapDiv = document.getElementById('map');
        var center_LatLng = new google.maps.LatLng(30,-20);
        map = new google.maps.Map(mapDiv, {
          zoom: 2,
          center: center_LatLng
        });

        heatpoint_array = JSON.parse(heatpoint_array.replace(/&quot;/g,'"'));

        var heatMapData = [];
        for(var i=0;i<heatpoint_array.length;i++){
            var lat = heatpoint_array[i].Latitude;
            var lng = heatpoint_array[i].Longitude;
            heatMapData.push({location: new google.maps.LatLng(lat, lng), weight: 1});
        }

        new google.maps.visualization.HeatmapLayer({
          data: heatMapData,
          map: map
        });


}