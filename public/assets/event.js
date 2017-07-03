if (!!window.EventSource) {
  var source = new EventSource('api/events');
} else {
  // Result to xhr polling :(
}

source.addEventListener('message', function(e) {
   //var data = JSON.parse(e.data);
    document.body.innerHTML += e.data + '<br>';
}, false);

source.addEventListener('open', function(e) {
  // Connection was opened.
}, false);

source.addEventListener('error', function(e) {
  if (e.readyState == EventSource.CLOSED) {
    // Connection was closed.
  }
}, false);