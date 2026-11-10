// Chatbox tro ly ao. ui.js tu nap file nay nen moi trang co ui.js deu co chatbox o goc phai duoi,
// khong can sua template. Trang nao khong muon hien chatbox thi them data-no-chatbox vao <body>.
// Hoi thoai giu trong sessionStorage de khong mat khi chuyen trang.
(function () {
    if (document.getElementById("chatbox") || (document.body && document.body.hasAttribute("data-no-chatbox"))) {
        return;
    }

    // Lay goc ung dung tu chinh duong dan file nay (.../js/chatbox.js) de dung ca khi co context path.
    var script = document.currentScript;
    var base = script && script.src ? script.src.replace(/js\/chatbox\.js(\?.*)?$/, "") : "/";

    var STORE_KEY = "nova-chat";
    var OPEN_KEY = "nova-chat-open";
    var GREETING = "Xin chào! Mình là trợ lý ảo của NOVA SHOP. Bạn cần tìm sản phẩm hay hỏi về đơn hàng, giao hàng?";

    var root = document.createElement("div");
    root.className = "chatbox";
    root.id = "chatbox";
    root.innerHTML =
        '<section class="chatbox-panel" id="chatbox-panel" aria-label="Trợ lý NOVA SHOP" hidden>' +
        '  <header class="chatbox-head">' +
        '    <span class="chatbox-dot" aria-hidden="true"></span>' +
        '    <div><strong>Trợ lý NOVA SHOP</strong><small>Hỏi về sản phẩm, đơn hàng, giao hàng</small></div>' +
        '    <button type="button" class="chatbox-icon" data-chat-reset title="Cuộc trò chuyện mới" aria-label="Cuộc trò chuyện mới">' +
        '      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/></svg>' +
        '    </button>' +
        '    <button type="button" class="chatbox-icon" data-chat-close title="Thu nhỏ" aria-label="Thu nhỏ">' +
        '      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>' +
        '    </button>' +
        '  </header>' +
        '  <div class="chatbox-log" role="log" aria-live="polite"></div>' +
        '  <form class="chatbox-form">' +
        '    <textarea name="message" rows="1" maxlength="500" placeholder="Nhập câu hỏi..." aria-label="Câu hỏi" required></textarea>' +
        '    <button type="submit" aria-label="Gửi">' +
        '      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M22 2 11 13"/><path d="M22 2 15 22l-4-9-9-4z"/></svg>' +
        '    </button>' +
        '  </form>' +
        '</section>' +
        '<button type="button" class="chatbox-toggle" aria-controls="chatbox-panel" aria-expanded="false" aria-label="Mở trợ lý ảo">' +
        '  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 12a8 8 0 0 1-11.6 7.1L3 21l1.9-6.4A8 8 0 1 1 21 12z"/></svg>' +
        '</button>';
    document.body.appendChild(root);

    var panel = root.querySelector(".chatbox-panel");
    var toggle = root.querySelector(".chatbox-toggle");
    var log = root.querySelector(".chatbox-log");
    var form = root.querySelector(".chatbox-form");
    var input = form.querySelector("textarea");
    var sendButton = form.querySelector("button[type=submit]");
    var history = load();
    var busy = false;

    function load() {
        try {
            var data = JSON.parse(sessionStorage.getItem(STORE_KEY) || "[]");
            return Array.isArray(data) ? data : [];
        } catch (e) {
            return [];
        }
    }

    function save() {
        try {
            sessionStorage.setItem(STORE_KEY, JSON.stringify(history.slice(-20)));
        } catch (e) { /* bo qua */ }
    }

    function bubble(role, text, extraClass) {
        var item = document.createElement("div");
        item.className = "chatbox-msg chatbox-" + role + (extraClass ? " " + extraClass : "");
        item.textContent = text;
        log.appendChild(item);
        log.scrollTop = log.scrollHeight;
        return item;
    }

    function render() {
        log.innerHTML = "";
        bubble("model", GREETING);
        history.forEach(function (turn) {
            bubble(turn.role, turn.text);
        });
    }

    function setOpen(open, focus) {
        panel.hidden = !open;
        root.classList.toggle("is-open", open);
        toggle.setAttribute("aria-expanded", String(open));
        toggle.setAttribute("aria-label", open ? "Đóng trợ lý ảo" : "Mở trợ lý ảo");
        try { sessionStorage.setItem(OPEN_KEY, open ? "1" : ""); } catch (e) { /* bo qua */ }
        if (open) {
            log.scrollTop = log.scrollHeight;
            if (focus) {
                input.focus();
            }
        }
    }

    function send(text) {
        busy = true;
        sendButton.disabled = true;
        var past = history.slice(-10);
        history.push({ role: "user", text: text });
        bubble("user", text);
        var typing = bubble("model", "Đang trả lời…", "is-typing");

        fetch(base + "api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Accept": "application/json" },
            body: JSON.stringify({ message: text, history: past })
        })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error("HTTP " + res.status);
                }
                return res.json();
            })
            .then(function (data) {
                typing.remove();
                var reply = data && data.reply ? data.reply : "Xin lỗi, mình chưa trả lời được.";
                if (data && data.ok) {
                    history.push({ role: "model", text: reply });
                    bubble("model", reply);
                } else {
                    // Loi khong luu vao lich su de lan sau khong gui lai cho model.
                    bubble("model", reply, "is-error");
                }
            })
            .catch(function () {
                typing.remove();
                bubble("model", "Không kết nối được tới trợ lý. Bạn thử lại sau nhé.", "is-error");
            })
            .finally(function () {
                save();
                busy = false;
                sendButton.disabled = false;
                input.focus();
            });
    }

    toggle.addEventListener("click", function () {
        setOpen(panel.hidden, true);
    });
    root.querySelector("[data-chat-close]").addEventListener("click", function () {
        setOpen(false);
        toggle.focus();
    });
    root.querySelector("[data-chat-reset]").addEventListener("click", function () {
        history = [];
        save();
        render();
        input.focus();
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        var text = input.value.trim();
        if (!text || busy) {
            return;
        }
        input.value = "";
        input.style.height = "";
        send(text);
    });

    input.addEventListener("keydown", function (event) {
        if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            form.requestSubmit();
        }
        if (event.key === "Escape") {
            event.stopPropagation();
            setOpen(false);
            toggle.focus();
        }
    });
    input.addEventListener("input", function () {
        input.style.height = "";
        input.style.height = Math.min(input.scrollHeight, 96) + "px";
    });

    render();
    var wasOpen = false;
    try { wasOpen = sessionStorage.getItem(OPEN_KEY) === "1"; } catch (e) { /* bo qua */ }
    if (wasOpen) {
        setOpen(true);
    }
})();
