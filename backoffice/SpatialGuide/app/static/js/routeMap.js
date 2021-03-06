var map;
var pos = 30;

function initMap() {
        var mapDiv = document.getElementById('map');
        var center_LatLng = new google.maps.LatLng(30,-20);
        map = new google.maps.Map(mapDiv, {
          zoom: 2,
          center: center_LatLng
        });

        point_array = JSON.parse(point_array.replace(/&quot;/g,'"'));

        var markers= new Array();
        for(var i =0;i<point_array.length;i++){
            markers[i] = new google.maps.Marker({
                                    position: new google.maps.LatLng(point_array[i].Latitude,point_array[i].Longitude),
                                    map: map,
                                    title: point_array[i].Name,
                                    icon:'http://maps.google.com/mapfiles/ms/icons/blue-dot.png' });
        }

        if (markers.length != 0){
            var bounds = new google.maps.LatLngBounds();
            for (var i = 0; i < markers.length; i++) {
                bounds.extend(markers[i].getPosition());
            }

            map.fitBounds(bounds);
        }

}