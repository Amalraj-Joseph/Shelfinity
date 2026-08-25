(function () {
  // Highlight the current page in the top nav.
  var here = window.location.pathname.replace(/\/index\.html$/, '/');
  document.querySelectorAll('.nav-links a').forEach(function (link) {
    var linkPath = link.getAttribute('href');
    if (linkPath && here.indexOf(linkPath) !== -1 && linkPath !== '/') {
      link.classList.add('active');
    }
  });

  // Copy-to-clipboard button on fenced code blocks.
  document.querySelectorAll('.prose pre').forEach(function (pre) {
    var btn = document.createElement('button');
    btn.textContent = 'Copy';
    btn.type = 'button';
    btn.className = 'copy-btn';
    btn.addEventListener('click', function () {
      var code = pre.querySelector('code');
      var text = code ? code.textContent : pre.textContent;
      navigator.clipboard.writeText(text).then(function () {
        btn.textContent = 'Copied';
        setTimeout(function () { btn.textContent = 'Copy'; }, 1500);
      });
    });
    pre.appendChild(btn);
  });
})();
