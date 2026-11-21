(function () {
    "use strict";

    const ctx = document.querySelector('meta[name="ctx"]')?.content || "";
    const csrf = document.querySelector('meta[name="csrf-token"]')?.content || "";
    const palette = ["#15803d", "#1d4ed8", "#6d28d9", "#b91c1c", "#0f766e", "#a16207"];

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    function icon(name, cls) {
        return '<svg class="icon ' + (cls || "") + '" aria-hidden="true"><use href="#i-' + name + '"/></svg>';
    }

    function javaHash(text) {
        let hash = 0;
        const value = String(text || "");
        for (let i = 0; i < value.length; i++) {
            hash = (Math.imul(31, hash) + value.charCodeAt(i)) | 0;
        }
        return hash;
    }

    function coverColor(title) {
        const index = ((javaHash(title) % palette.length) + palette.length) % palette.length;
        return palette[index];
    }

    function statusLabel(status) {
        return {
            DRAFT: "草稿",
            PENDING: "待审核",
            PUBLISHED: "已发布",
            REJECTED: "已驳回",
            UNPUBLISHED: "已下架"
        }[status] || status || "";
    }

    function formatTime(value) {
        if (!value) {
            return "";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return String(value).replace("T", " ").slice(0, 16);
        }
        const p = (n) => String(n).padStart(2, "0");
        return date.getFullYear() + "-" + p(date.getMonth() + 1) + "-" + p(date.getDate())
            + " " + p(date.getHours()) + ":" + p(date.getMinutes());
    }

    function debounce(fn, wait) {
        let timer = null;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), wait);
        };
    }

    async function api(path, options) {
        const opts = options || {};
        const method = opts.method || "GET";
        const headers = Object.assign({Accept: "application/json"}, opts.headers || {});
        let body = opts.body;
        if (method !== "GET" && method !== "HEAD" && csrf) {
            headers["X-CSRF-Token"] = csrf;
        }
        if (body && !(body instanceof FormData) && typeof body !== "string") {
            headers["Content-Type"] = "application/json";
            body = JSON.stringify(body);
        }
        const response = await fetch(path, {
            method,
            headers,
            body,
            credentials: "same-origin"
        });
        const text = await response.text();
        let payload;
        try {
            payload = text ? JSON.parse(text) : {code: response.status, message: ""};
        } catch (e) {
            payload = {code: response.status, message: "请求失败"};
        }
        if (!response.ok || payload.code !== 200) {
            const error = new Error(payload.message || "请求失败");
            error.code = payload.code || response.status;
            throw error;
        }
        return payload.data;
    }

    let toastTimer = null;

    function toast(message, type) {
        let node = document.querySelector(".qx-toast");
        if (!node) {
            node = document.createElement("div");
            node.className = "qx-toast";
            node.setAttribute("role", "status");
            node.setAttribute("aria-live", "polite");
            document.body.appendChild(node);
        }
        node.textContent = message;
        node.classList.toggle("is-error", type === "error");
        node.classList.add("is-show");
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => node.classList.remove("is-show"), 2600);
    }

    function confirm(message, options) {
        const opts = options || {};
        return new Promise((resolve) => {
            const dialog = document.createElement("dialog");
            dialog.className = "qx-dialog";
            dialog.innerHTML = `
                <div class="dialog-head">
                    <h2>${escapeHtml(opts.title || "确认操作")}</h2>
                    <button class="icon-btn" type="button" data-cancel aria-label="关闭">${icon("x", "icon-sm")}</button>
                </div>
                <div class="dialog-body">
                    <p style="margin:0;font-size:14px;line-height:1.7;color:var(--fg-dim)">${escapeHtml(message)}</p>
                </div>
                <div class="dialog-foot">
                    <button class="qx-btn qx-btn-fill" type="button" data-cancel>取消</button>
                    <button class="qx-btn ${opts.danger ? "qx-btn-primary" : "qx-btn-primary"}" type="button" data-ok>
                        ${escapeHtml(opts.confirmText || "确定")}
                    </button>
                </div>`;
            document.body.appendChild(dialog);
            const close = (value) => {
                dialog.close();
                dialog.remove();
                resolve(value);
            };
            dialog.querySelectorAll("[data-cancel]").forEach((b) => b.addEventListener("click", () => close(false)));
            dialog.querySelector("[data-ok]").addEventListener("click", () => close(true));
            dialog.addEventListener("cancel", (e) => {
                e.preventDefault();
                close(false);
            });
            dialog.showModal();
        });
    }

    function go(path) {
        window.location.href = ctx + path;
    }

    window.QX = {
        ctx,
        csrf,
        escape: escapeHtml,
        icon,
        coverColor,
        statusLabel,
        formatTime,
        debounce,
        api,
        toast,
        confirm,
        go,
        palette
    };
})();
