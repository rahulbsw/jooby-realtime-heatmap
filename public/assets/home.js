var data = {
  id: null,
  name: null,
  latitude: null,
  longitude: null
};

var map, heatmap;
var isInitialLoadDone = false;
const maxPoints = 20000;
var markers = [];

function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 2,
    center: {
      lat: 37.775,
      lng: -122.434
    },
    mapTypeId: 'satellite'
  });

  heatmap = new google.maps.visualization.HeatmapLayer({
    data: [],
    map: map
  });

}

// Helper functions
function isValidLatLng(data) {
  return ((typeof data.latitude === 'number' && data.latitude <= 90 && data.latitude >= -90) &&
    (typeof data.longitude === 'number' && data.longitude <= 180 && data.longitude >= -180))
}

var clearOverflow = function(size) {
  var heatmapData = heatmap.getData();
  while (heatmapData.getLength() > size) {
    heatmapData.pop();
  }
}

function drop(position) {
  addMarkerWithTimeout(new google.maps.LatLng(position.latitude, position.longitude), 1000);
}

function addMarkerWithTimeout(position, timeout) {
  var marker = new google.maps.Marker({
    map: heatmap.getMap(),
    animation: google.maps.Animation.DROP,
    position: position
  });
  window.setTimeout(function() {
    marker.setMap(null);
  }, timeout);
}

function clearMarkers() {
  for (var i = 0; i < markers.length; i++) {
    markers[i].setMap(null);
  }
  markers = [];
}

// Add new point to heatmap
var addToMap = function(newPosition) {
  var weight = 1;
  heatmap.getData().push({
    location: new google.maps.LatLng(newPosition.latitude, newPosition.longitude),
    weight: weight
  });
};

// Remove old data from the heatmap when a point is removed from firebase.
var removeFromMap = function(data) {
  var heatmapData = heatmap.getData();
  var i = 0;
  while (heatmapData.getLength() >= i + 1 && (data.latitude != heatmapData.getAt(i).location.lat() || data.longitude != heatmapData.getAt(i).location.lng())) {
    i++;
  }
  heatmapData.removeAt(i);
};

// SSE start
if (!!window.EventSource) {
  var source = new EventSource('api/events');
} else {
  // Result to xhr polling :(
}

source.addEventListener('message', function(e) {
  var data = JSON.parse(e.data);
  if(isValidLatLng(data)){
  //console.log(data)
  addToMap(data); // add to heatmap
  clearOverflow(maxPoints);
  drop(data);
  }
}, false);

source.addEventListener('open', function(e) {
  // Connection was opened.
  isInitialLoadDone = true;
}, false);

source.addEventListener('error', function(e) {
  if (e.readyState == EventSource.CLOSED) {
    // Connection was closed.
  }
}, false);

//SSE END