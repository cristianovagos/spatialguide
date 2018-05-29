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

          // Create the search box and link it to the UI element.
        var input = document.getElementById('pac-input');
        var searchBox = new google.maps.places.SearchBox(input);
        map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

        // Bias the SearchBox results towards current map's viewport.
        map.addListener('bounds_changed', function() {
          searchBox.setBounds(map.getBounds());
        });

        // Listen for the event fired when the user selects a prediction and retrieve
        // more details for that place.
        searchBox.addListener('places_changed', function() {
          var places = searchBox.getPlaces();

          if (places.length == 0) {
            return;
          }

          // For each place, get the icon, name and location.
          var bounds = new google.maps.LatLngBounds();
          places.forEach(function(place) {
            if (!place.geometry) {
              console.log("Returned place contains no geometry");
              return;
            }

            if (place.geometry.viewport) {
              // Only geocodes have viewport.
              bounds.union(place.geometry.viewport);
            } else {
              bounds.extend(place.geometry.location);
            }
          });
          map.fitBounds(bounds);
        });
}