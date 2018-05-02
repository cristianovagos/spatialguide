var map;
var pos = 30;
var marker;

function initMap() {
        var mapDiv = document.getElementById('map');
        var center_LatLng = new google.maps.LatLng(30,-20);
        map = new google.maps.Map(mapDiv, {
          zoom: 2,
          center: center_LatLng
        });

        point_array = JSON.parse(point_array.replace(/&quot;/g,'"'));


        for(var i =0;i<point_array.length;i++){
            new google.maps.Marker({
                                        position: new google.maps.LatLng(point_array[i].Latitude,point_array[i].Longitude),
                                        map: map,
                                        title: point_array[i].Name,
                                        icon:'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
                                  });
        }


        map.addListener('click',function (e) {

            var mycursorLatLng = new google.maps.LatLng(e.latLng.lat(), e.latLng.lng());


            if (!marker) {
                marker = new google.maps.Marker({
                    position: mycursorLatLng,
                    map: map,
                    title: "New Point"
                });
            }
            else
            {
                marker.setPosition(mycursorLatLng);
            }

            var lat = document.getElementById('id_Latitude');
            var lng = document.getElementById('id_Longitude');

            lat.value = mycursorLatLng.lat();
            lng.value = mycursorLatLng.lng();

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


