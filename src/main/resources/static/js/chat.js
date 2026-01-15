// /static/js/chat.js
(() => {
    const form = document.getElementById("composerForm");
    const ta = document.getElementById("prompt");
    const streamingToggle = document.getElementById("streaming");
    const messagesEl = document.getElementById("messages");

    if (!form || !ta) return;

    // Render existing assistant messages as Markdown on page load
    if (window.marked) {
        document.querySelectorAll(".msg--assistant .msg__content").forEach(el => {
            const md = el.textContent;
            if (md) el.innerHTML = marked.parse(md);
        });
    }

    // --- UX: textarea auto-grow ---
    const resize = () => {
        ta.style.height = "auto";
        ta.style.height = Math.min(220, ta.scrollHeight) + "px";
    };
    ta.addEventListener("input", resize);
    resize();

    // Enter submits (Shift+Enter newline)
    ta.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    const scrollToBottom = () => {
        if (!messagesEl) return;
        messagesEl.scrollTop = messagesEl.scrollHeight;
    };

    const setBusy = (busy) => {
        const btn = form.querySelector('button[type="submit"]');
        if (btn) btn.disabled = busy;
        ta.disabled = busy;
        if (!busy) ta.focus();
    };

    // Find or create message list container
    const ensureMessageList = () => {
        let list = document.querySelector(".message-list");
        if (list) return list;

        // If empty state is shown, replace it
        const container = messagesEl?.querySelector('div[th\\:if], div'); // best-effort
        if (messagesEl) {
            // wipe the visible area and create list
            messagesEl.innerHTML = '<div class="message-list"></div>';
            list = document.querySelector(".message-list");
        }
        return list;
    };

    const avatarSrcFor = (role) => (role === "USER" ? "/image/user.png" : "/image/assistant.png");
    const roleLabel = (role) => (role === "USER" ? "USER" : "ASSISTANT");

    // Create bubble and return content element for streaming append
    const appendMessage = (role, text) => {
        const list = ensureMessageList();
        if (!list) return null;

        const isUser = role === "USER";

        const msg = document.createElement("div");
        msg.className = "msg " + (isUser ? "msg--user" : "msg--assistant");

        const avatar = document.createElement("div");
        avatar.className = "msg__avatar";

        const img = document.createElement("img");
        img.src = avatarSrcFor(role);
        img.alt = "avatar";
        avatar.appendChild(img);

        const bubble = document.createElement("div");
        bubble.className = "msg__bubble";

        const r = document.createElement("div");
        r.className = "msg__role";
        r.textContent = roleLabel(role);

        const content = document.createElement("div");
        content.className = "msg__content";
        content.textContent = text ?? "";

        bubble.appendChild(r);
        bubble.appendChild(content);

        msg.appendChild(avatar);
        msg.appendChild(bubble);

        list.appendChild(msg);
        scrollToBottom();

        return content;
    };

    // Extract chatId (works even if you don't add data-chat-id)
    const getChatId = () => {
        // Preferred: data-chat-id if you decide to add it later
        const dataId = form.getAttribute("data-chat-id");
        if (dataId) return dataId;

        // Fallback: parse from action: /chats/{chatId}/messages
        const action = form.getAttribute("action") || "";
        const m = action.match(/\/chats\/(\d+)\/messages/);
        return m ? m[1] : null;
    };

    // Streaming submit interceptor
    form.addEventListener("submit", (e) => {
        const useStreaming = !!(streamingToggle && streamingToggle.checked);
        if (!useStreaming) return; // normal POST -> server rerender

        e.preventDefault();

        const chatId = getChatId();
        const prompt = (ta.value || "").trim();

        if (!chatId) {
            console.error("Cannot determine chatId for streaming. Add data-chat-id to form or keep action as /chats/{id}/messages");
            return;
        }
        if (!prompt) return;

        // optimistic UI
        appendMessage("USER", prompt);
        const assistantContent = appendMessage("ASSISTANT", "");
        if (!assistantContent) return;

        // clear input
        ta.value = "";
        resize();
        setBusy(true);

        const params = new URLSearchParams({userPrompt: prompt, _: Date.now().toString()});
        const url = `/api/chats/${encodeURIComponent(chatId)}/messages/stream?${params.toString()}`;
        console.log(`url ${url}`)

        const es = new EventSource(url);
        es.addEventListener("open", () => console.debug("SSE opened", url));

        let receivedAny = false;
        let fulltext = "";

        es.onmessage = (evt) => {
            if (!evt.data) return;
            receivedAny = true;
            // если вдруг бэк шлёт маркер завершения простым текстом
            if (evt.data === "[DONE]" || evt.data === "__DONE__" || evt.data === "DONE") {
                es.close();
                setBusy(false);
                return;
            }
            const json = JSON.parse(evt.data);
            const token = json.text;
            if (!token) return;
            fulltext += token;
            assistantContent.innerHTML = marked.parse(fulltext);
            scrollToBottom();
        };

        es.onerror = (e) => {
            try {
                es.close();
            } catch (_) {
            }

            if (!assistantContent.textContent) {
                assistantContent.textContent = receivedAny
                    ? "Connection closed."
                    : "Streaming failed: server did not provide an SSE stream (check DevTools → Network → /messages/stream).";
            }

            setBusy(false);
        };
    });
})();