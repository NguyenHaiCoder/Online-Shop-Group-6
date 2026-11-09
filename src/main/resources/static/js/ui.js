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