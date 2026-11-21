(function () {
    "use strict";

    const avatarBox = document.getElementById("me-avatar");
    const avatarTrigger = document.getElementById("avatar-trigger");
    const avatarInput = document.getElementById("avatar-input");
    const editToggle = document.getElementById("edit-toggle");
    const panel = document.getElementById("profile-edit-panel");
    const form = document.getElementById("profile-form");
    const nicknameInput = document.getElementById("profile-nickname");
    const bioInput = document.getElementById("profile-bio");
    const saveButton = document.getElementById("profile-save");
    const cancelButton = document.getElementById("profile-cancel");
    const nicknameText = document.getElementById("account-name");
    const bioText = document.getElementById("me-bio");

    function setAvatar(url) {
        avatarBox.innerHTML = "";
        if (url) {
            const img = document.createElement("img");
            img.src = QX.ctx + url;
            img.alt = "头像";
            avatarBox.appendChild(img);
        } else {
            avatarBox.textContent = (nicknameText.textContent || "?").trim().slice(0, 1);
        }
        const top = document.querySelector(".qx-avatar-sm");
        if (top) {
            top.innerHTML = "";
            if (url) {
                const img = document.createElement("img");
                img.src = QX.ctx + url;
                img.alt = "";
                top.appendChild(img);
            } else {
                top.textContent = (nicknameText.textContent || "?").trim().slice(0, 1);
            }
        }
    }

    if (avatarTrigger && avatarInput) {
        avatarTrigger.addEventListener("click", function () {
            avatarInput.click();
        });
        avatarInput.addEventListener("change", async function () {
            const file = avatarInput.files && avatarInput.files[0];
            if (!file) {
                return;
            }
            avatarTrigger.disabled = true;
            try {
                const data = new FormData();
                data.append("file", file);
                const uploaded = await QX.api(QX.ctx + "/api/v1/files/avatar", {
                    method: "POST",
                    body: data
                });
                const user = await QX.api(QX.ctx + "/api/v1/me/profile", {
                    method: "PUT",
                    body: {avatarUrl: uploaded.url}
                });
                setAvatar(user.avatarUrl);
                QX.toast("头像已更新");
            } catch (e) {
                QX.toast(e.message || "头像上传失败", "error");
            } finally {
                avatarTrigger.disabled = false;
                avatarInput.value = "";
            }
        });
    }

    function setEditing(editing) {
        panel.hidden = !editing;
        editToggle.setAttribute("aria-expanded", editing ? "true" : "false");
        if (editing) {
            nicknameInput.focus();
        }
    }

    function resetForm() {
        nicknameInput.value = (nicknameText.textContent || "").trim();
        bioInput.value = bioText.textContent.trim();
    }

    if (editToggle && panel && form) {
        editToggle.addEventListener("click", function () {
            const willOpen = panel.hidden;
            if (willOpen) {
                resetForm();
            }
            setEditing(willOpen);
        });
        cancelButton.addEventListener("click", function () {
            resetForm();
            setEditing(false);
        });

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            const nickname = nicknameInput.value.trim();
            if (!nickname) {
                QX.toast("昵称不能为空", "error");
                nicknameInput.focus();
                return;
            }
            saveButton.disabled = true;
            try {
                const user = await QX.api(QX.ctx + "/api/v1/me/profile", {
                    method: "PUT",
                    body: {nickname: nickname, bio: bioInput.value}
                });
                nicknameText.textContent = user.nickname;
                const bio = (user.bio || "").trim();
                bioText.textContent = bio;
                bioText.hidden = !bio;
                const topName = document.querySelector(".qx-user-name");
                if (topName) {
                    topName.textContent = user.nickname;
                }
                if (!user.avatarUrl) {
                    setAvatar(null);
                }
                resetForm();
                setEditing(false);
                QX.toast("资料已保存");
            } catch (e) {
                QX.toast(e.message || "保存失败", "error");
            } finally {
                saveButton.disabled = false;
            }
        });
    }
})();
