(function () {
    "use strict";

    const avatarTrigger = document.getElementById("avatar-trigger");
    const avatarInput = document.getElementById("avatar-input");
    if (avatarTrigger && avatarInput) {
        avatarTrigger.addEventListener("click", () => avatarInput.click());
        avatarInput.addEventListener("change", async function () {
            const file = avatarInput.files && avatarInput.files[0];
            if (!file) {
                return;
            }
            avatarTrigger.disabled = true;
            try {
                const form = new FormData();
                form.append("file", file);
                const uploaded = await QX.api(QX.ctx + "/api/v1/files/avatar", {method: "POST", body: form});
                await QX.api(QX.ctx + "/api/v1/me/avatar", {method: "PUT", body: {avatarUrl: uploaded.url}});
                const box = document.getElementById("mine-avatar");
                if (box) {
                    box.innerHTML = '<img src="' + QX.ctx + QX.escape(uploaded.url) + '" alt="头像">';
                }
                const top = document.querySelector(".qx-avatar-sm");
                if (top) {
                    top.innerHTML = '<img src="' + QX.ctx + QX.escape(uploaded.url) + '" alt="">';
                }
                QX.toast("头像已更新");
            } catch (e) {
                QX.toast(e.message || "头像上传失败", "error");
            } finally {
                avatarTrigger.disabled = false;
                avatarInput.value = "";
            }
        });
    }

    document.querySelectorAll(".mine-actions").forEach(function (actions) {
        actions.addEventListener("click", async function (e) {
            const button = e.target.closest("[data-action]");
            if (!button) {
                return;
            }
            const id = button.dataset.id;
            const action = button.dataset.action;
            if (!id) {
                return;
            }
            if (action === "delete") {
                const ok = await QX.confirm("删除后正文会被清空，记录保留但不再展示。", {
                    title: "删除这篇内容？",
                    confirmText: "删除",
                    danger: true
                });
                if (!ok) {
                    return;
                }
            }
            if (action === "withdraw") {
                const ok = await QX.confirm("撤回后内容会回到草稿，只有你自己可见。", {
                    title: "撤回审核？",
                    confirmText: "撤回"
                });
                if (!ok) {
                    return;
                }
            }
            const url = action === "delete"
                ? QX.ctx + "/api/v1/articles/" + id
                : QX.ctx + "/api/v1/articles/" + id + "/" + action;
            const method = action === "delete" ? "DELETE" : "POST";
            button.disabled = true;
            try {
                await QX.api(url, {method});
                QX.toast(action === "delete" ? "已删除" : action === "withdraw" ? "已撤回" : "已提交审核");
                setTimeout(() => location.reload(), 400);
            } catch (err) {
                QX.toast(err.message || "操作失败", "error");
                button.disabled = false;
            }
        });
    });
})();
