(function () {
  // Scroll-reveal: fade/slide in any .reveal element as it enters the viewport.
  var revealEls = document.querySelectorAll('.reveal');
  if ('IntersectionObserver' in window && revealEls.length) {
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            entry.target.classList.add('in-view');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.15 }
    );
    revealEls.forEach(function (el) { observer.observe(el); });
  } else {
    revealEls.forEach(function (el) { el.classList.add('in-view'); });
  }

  // Highlight the current page in the top nav.
  var here = window.location.pathname.replace(/\/index\.html$/, '/');
  document.querySelectorAll('.nav-links a').forEach(function (link) {
    var linkPath = link.getAttribute('href');
    if (linkPath && here.indexOf(linkPath) !== -1 && linkPath !== '/') {
      link.classList.add('active');
    }
  });

  // Animated stat counters on the homepage.
  document.querySelectorAll('.stat .num[data-count]').forEach(function (el) {
    var target = parseInt(el.getAttribute('data-count'), 10);
    var suffix = el.getAttribute('data-suffix') || '';
    var duration = 1100;
    var start = null;

    function step(ts) {
      if (!start) start = ts;
      var progress = Math.min((ts - start) / duration, 1);
      var eased = 1 - Math.pow(1 - progress, 3);
      el.textContent = Math.round(eased * target) + suffix;
      if (progress < 1) requestAnimationFrame(step);
    }

    if ('IntersectionObserver' in window) {
      var statObserver = new IntersectionObserver(
        function (entries) {
          entries.forEach(function (entry) {
            if (entry.isIntersecting) {
              requestAnimationFrame(step);
              statObserver.unobserve(entry.target);
            }
          });
        },
        { threshold: 0.4 }
      );
      statObserver.observe(el);
    } else {
      el.textContent = target + suffix;
    }
  });

  // Copy-to-clipboard button on fenced code blocks.
  document.querySelectorAll('.prose pre').forEach(function (pre) {
    var btn = document.createElement('button');
    btn.textContent = 'Copy';
    btn.setAttribute('type', 'button');
    btn.style.cssText = 'position:absolute;top:10px;right:10px;font-size:0.72rem;' +
      'padding:4px 9px;border-radius:6px;border:1px solid var(--surface-border);' +
      'background:var(--surface);color:var(--text-muted);cursor:pointer;';
    btn.addEventListener('click', function () {
      var code = pre.querySelector('code');
      var text = code ? code.textContent : pre.textContent;
      navigator.clipboard.writeText(text).then(function () {
        btn.textContent = 'Copied';
        setTimeout(function () { btn.textContent = 'Copy'; }, 1500);
      });
    });
    pre.style.position = 'relative';
    pre.appendChild(btn);
  });
})();
