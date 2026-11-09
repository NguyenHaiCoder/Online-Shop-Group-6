document.addEventListener("click", function (event) {
    var opener = event.target.closest("[data-open]");
    if (opener) {
        event.preventDefault();
        var modal = document.getElementById("modal-" + opener.getAttribute("data-open"));
        if (modal) {
            modal.classList.add("is-open");
        }
    }
    if (event.target.closest("[data-close]")) {
        var box = event.target.closest(".modal");
        if (box) {
            box.classList.remove("is-open");
        }
    }
    if (event.target.closest("[data-nav-toggle]")) {
        var nav = document.querySelector(".main-nav");
        if (nav) {
            nav.classList.toggle("is-open");
        }
    }
});

document.addEventListener("submit", function (event) {
    var button = event.submitter;
    if (button && button.dataset.confirm && !window.confirm(button.dataset.confirm)) {
        event.preventDefault();
    }
});

document.addEventListener("keydown", function (event) {
    if (event.key !== "Escape") {
        return;
    }
    document.querySelectorAll(".modal.is-open").forEach(function (modal) {
        modal.classList.remove("is-open");
    });
});

// Use an explicit empty-image state instead of a broken image icon.
function recoverImage(image) {
    if (!(image instanceof HTMLImageElement) || image.dataset.recovered) return;
    image.dataset.recovered = 'true';
    var fallback = image.getAttribute('data-image-fallback') || '/images/image-unavailable.svg';
    if (fallback.endsWith('.svg')) image.classList.add('image-unavailable');
    image.src = fallback;
}
document.addEventListener('error', function(event) { if (event.target instanceof HTMLImageElement) recoverImage(event.target); }, true);
document.querySelectorAll('img').forEach(function(image) { if (image.complete && image.naturalWidth === 0) recoverImage(image); });

// Carousel buttons and indicators share a single current slide.
document.querySelectorAll('[data-carousel]').forEach(function(carousel) {
    var slides = Array.from(carousel.querySelectorAll('[data-slide]'));
    var dots = Array.from(carousel.querySelectorAll('[data-slide-to]'));
    var current = 0;
    function show(index) {
        if (!slides.length) return;
        current = (index + slides.length) % slides.length;
        slides.forEach(function(slide, i) { slide.hidden = i !== current; });
        dots.forEach(function(dot, i) { dot.classList.toggle('on', i === current); dot.setAttribute('aria-pressed', i === current ? 'true' : 'false'); });
    }
    carousel.addEventListener('click', function(event) {
        var target = event.target.closest('button');
        if (!target) return;
        if (target.hasAttribute('data-slide-prev')) show(current - 1);
        if (target.hasAttribute('data-slide-next')) show(current + 1);
        if (target.hasAttribute('data-slide-to')) show(Number(target.dataset.slideTo));
    });
    show(0);
});

// Keep modal focus inside the dialog and restore it when closing.
var previousFocus = null;
document.addEventListener('click', function(event) {
    var opener = event.target.closest('[data-open]');
    if (opener) {
        previousFocus = opener;
        var dialog = document.getElementById('modal-' + opener.dataset.open);
        if (dialog) {
            dialog.setAttribute('role', 'dialog'); dialog.setAttribute('aria-modal', 'true');
            var first = dialog.querySelector('input:not([type=hidden]),select,button,a');
            if (first) first.focus();
        }
    }
    if (event.target.closest('[data-close]') && previousFocus) previousFocus.focus();
});
document.addEventListener('keydown', function(event) {
    var dialog = document.querySelector('.modal.is-open');
    if (event.key === 'Escape' && previousFocus) previousFocus.focus();
    if (!dialog || event.key !== 'Tab') return;
    var items = Array.from(dialog.querySelectorAll('a[href],button:not([disabled]),input:not([type=hidden]):not([disabled]),select,textarea')).filter(function(item) { return item.getClientRects().length; });
    var first = items[0], last = items[items.length - 1];
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
    if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
});