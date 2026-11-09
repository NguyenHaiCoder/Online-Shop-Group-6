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
