(function () {
    "use strict";

    const root = document.getElementById("editor-root");
    if (!root) {
        return;
    }
    const title = document.getElementById("editor-title");
    const content = document.getElementById("editor-content");
    const category = document.getElementById("editor-category");
    const count = document.getElementById("editor-count");
    const preview = document.getElementById("cover-preview");
    const previewTitle = document.getElementById("preview-title");
    const previewCat = document.getElementById("preview-cat");
    const error = document.getElementById("editor-error");
    const saveState = document.getElementById("save-state");
    const saveButton = document.getElementById("save-draft");
    const submitButton = document.getElementById("submit-review");
    const back = document.querySelector(".qx-back");
    const typeButtons = Array.from(document.querySelectorAll(".editor-type"));
    const mediaPanel = document.getElementById("editor-media");
    const mediaInput = document.getElementById("media-input");
    const mediaPick = document.getElementById("media-pick");
    const mediaThumbs = document.getElementById("media-thumbs");
    const mediaCount = document.getElementById("media-count");

    function isImageUrl(url) {
        return /\.(jpe?g|png|webp)$/i.test(url || "");
    }

    function isVideoUrl(url) {
        return /\.(mp4|webm)$/i.test(url || "");
    }

    function mediaLimitReached() {
        return mediaType === "VIDEO" ? mediaUrls.length >= 1 : mediaUrls.length >= 9;
    }

    let mode = root.dataset.mode || "new";
    let id = root.dataset.id || "";
    let coverColor = root.dataset.cover || "";
    let status = root.dataset.status || "DRAFT";
    let mediaType = (root.dataset.mediaType || "TEXT").toUpperCase();
    let mediaUrls = [];
    const mediaByType = {IMAGE: [], VIDEO: []};
    try {
        mediaUrls = JSON.parse(root.dataset.mediaUrls || "[]") || [];
    } catch (e) {
        mediaUrls = [];
    }
    if (mediaType === "IMAGE") {
        mediaByType.IMAGE = mediaUrls.filter(isImageUrl).slice(0, 9);
    } else if (mediaType === "VIDEO") {
        mediaByType.VIDEO = mediaUrls.filter(isVideoUrl).slice(0, 1);
    }
    let savedSnapshot = snapshot();
    let busy = false;

    function textLength(html) {
        const div = document.createElement("div");
        div.innerHTML = html || "";
        return (div.textContent || "").replace(/\s+/g, "").length;
    }

    function snapshot() {
        return JSON.stringify({
            title: title.value.trim(),
            content: content.innerHTML.trim(),
            mediaType: mediaType,
            mediaUrls: mediaUrls,
            categoryId: category.value || ""
        });
    }

    function dirty() {
        return snapshot() !== savedSnapshot;
    }

    function setError(message) {
        if (!message) {
            error.hidden = true;
            error.textContent = "";
            return;
        }
        error.hidden = false;
        error.textContent = message;
    }

    function renderMediaThumbs() {
        if (!mediaThumbs) {
            return;
        }
        mediaThumbs.innerHTML = mediaUrls.map(function (url, index) {
            const src = QX.ctx + QX.escape(url);
            const media = mediaType === "VIDEO"
                ? '<video src="' + src + '" muted preload="metadata"></video>'
                : '<img src="' + src + '" alt="">';
            return '<div class="media-thumb">' + media
                + '<button type="button" data-remove="' + index + '" aria-label="移除">'
                + QX.icon("x") + '</button></div>';
        }).join("");
    }

    function syncMediaUi() {
        typeButtons.forEach(function (button) {
            button.classList.toggle("is-active", button.dataset.type === mediaType);
        });
        if (mediaPanel) {
            mediaPanel.hidden = mediaType === "TEXT";
        }
        if (mediaInput) {
            mediaInput.accept = mediaType === "VIDEO"
                ? "video/mp4,video/webm" : "image/jpeg,image/png,image/webp";
            mediaInput.multiple = mediaType !== "VIDEO";
        }
        if (mediaPick) {
            mediaPick.textContent = mediaType === "VIDEO" ? "选择视频" : "添加图片";
            mediaPick.disabled = mediaType !== "TEXT" && mediaLimitReached();
        }
        if (mediaCount) {
            const total = mediaType === "VIDEO" ? 1 : 9;
            mediaCount.textContent = mediaType === "TEXT" ? "" : mediaUrls.length + " / " + total;
        }
        renderMediaThumbs();
    }

    async function uploadMedia(files) {
        if (!files || !files.length || mediaType === "TEXT") {
            return;
        }
        const remaining = mediaType === "VIDEO" ? 1 : 9 - mediaUrls.length;
        const list = Array.from(files).slice(0, Math.max(remaining, 0));
        mediaPick.disabled = true;
        try {
            for (const file of list) {
                const form = new FormData();
                form.append("file", file);
                const kind = mediaType === "VIDEO" ? "video" : "image";
                const result = await QX.api(QX.ctx + "/api/v1/files/media?kind=" + kind,
                    {method: "POST", body: form});
                if (mediaType === "VIDEO") {
                    mediaUrls = [result.url];
                } else {
                    mediaUrls.push(result.url);
                }
            }
            if (mediaType === "VIDEO") {
                mediaUrls = mediaUrls.filter(isVideoUrl).slice(0, 1);
            } else if (mediaType === "IMAGE") {
                mediaUrls = mediaUrls.filter(isImageUrl).slice(0, 9);
            }
            if (mediaByType[mediaType]) {
                mediaByType[mediaType] = mediaUrls.slice();
            }
            syncMediaUi();
            updatePreview();
        } catch (e) {
            QX.toast(e.message || "上传失败", "error");
        } finally {
            syncMediaUi();
            mediaInput.value = "";
        }
    }

    function updatePreview() {
        const titleText = title.value.trim() || "未命名";
        const option = category.options[category.selectedIndex];
        const categoryText = option && option.value ? (option.dataset.path || option.textContent.trim()) : "未分类";
        previewTitle.textContent = titleText;
        previewCat.textContent = categoryText;
        preview.style.background = mode === "edit" && coverColor ? coverColor : QX.coverColor(titleText);
        let previewImage = preview.querySelector(".preview-media-img");
        if (mediaType === "IMAGE" && mediaUrls.length) {
            if (!previewImage) {
                previewImage = document.createElement("img");
                previewImage.className = "preview-media-img";
                preview.prepend(previewImage);
            }
            previewImage.src = QX.ctx + mediaUrls[0];
        } else if (previewImage) {
            previewImage.remove();
        }
        count.textContent = textLength(content.innerHTML) + " 字";
        if (saveState) {
            saveState.textContent = !id || dirty() ? "未保存" : "已保存";
            saveState.style.color = (!id || dirty()) ? "var(--qx-warn)" : "var(--fg-mute)";
        }
    }

    function setStatusChip(next) {
        status = next || status;
        let chip = document.getElementById("editor-status-chip");
        if (!chip && status) {
            chip = document.createElement("span");
            chip.id = "editor-status-chip";
            chip.className = "status-chip";
            document.querySelector(".qx-topbar-edit > div").prepend(chip);
        }
        if (chip) {
            chip.className = "status-chip status-" + status;
            chip.textContent = QX.statusLabel(status);
        }
    }

    async function save(silent) {
        if (busy) {
            return null;
        }
        if (mediaType === "TEXT" && textLength(content.innerHTML) === 0) {
            setError("正文不能为空");
            content.focus();
            return null;
        }
        if (mediaType !== "TEXT" && mediaUrls.length === 0) {
            setError(mediaType === "VIDEO" ? "请上传视频" : "请上传图片");
            return null;
        }
        if (mediaType === "VIDEO" && mediaUrls.length !== 1) {
            setError("视频帖只能有 1 个视频");
            return null;
        }
        if (mediaType === "IMAGE" && mediaUrls.length > 9) {
            setError("图片最多 9 张");
            return null;
        }
        setError("");
        busy = true;
        saveButton.disabled = true;
        submitButton.disabled = true;
        const payload = {
            title: title.value,
            content: content.innerHTML,
            mediaType: mediaType,
            mediaUrls: mediaType === "TEXT" ? [] : mediaUrls,
            categoryId: category.value ? Number(category.value) : null
        };
        try {
            const data = id
                ? await QX.api(QX.ctx + "/api/v1/articles/" + id, {method: "PUT", body: payload})
                : await QX.api(QX.ctx + "/api/v1/articles", {method: "POST", body: payload});
            id = String(data.id);
            mode = "edit";
            coverColor = data.coverColor || coverColor;
            mediaType = data.mediaType || mediaType;
            mediaUrls = data.mediaUrls || mediaUrls;
            if (mediaByType[mediaType]) {
                mediaByType[mediaType] = mediaUrls.slice();
            }
            root.dataset.mode = "edit";
            root.dataset.id = id;
            root.dataset.cover = coverColor;
            syncMediaUi();
            savedSnapshot = snapshot();
            setStatusChip(data.status);
            history.replaceState(history.state, "", QX.ctx + "/creator/articles/" + id + "/edit");
            updatePreview();
            if (!silent) {
                QX.toast("已保存");
            }
            return data;
        } catch (e) {
            setError(e.message || "保存失败");
            QX.toast(e.message || "保存失败", "error");
            return null;
        } finally {
            busy = false;
            saveButton.disabled = false;
            submitButton.disabled = false;
        }
    }

    async function submitReview() {
        if (busy) {
            return;
        }
        let data = null;
        if (!id || dirty()) {
            data = await save(true);
            if (!data) {
                return;
            }
        }
        if (status === "PENDING" || (data && data.status === "PENDING")) {
            QX.toast("已提交审核");
            setTimeout(() => QX.go("/creator/mine?status=PENDING"), 350);
            return;
        }
        busy = true;
        submitButton.disabled = true;
        saveButton.disabled = true;
        try {
            await QX.api(QX.ctx + "/api/v1/articles/" + id + "/submit", {method: "POST"});
            QX.toast("已提交审核");
            setTimeout(() => QX.go("/creator/mine?status=PENDING"), 350);
        } catch (e) {
            QX.toast(e.message || "提交失败", "error");
            setError(e.message || "提交失败");
        } finally {
            busy = false;
            submitButton.disabled = false;
            saveButton.disabled = false;
        }
    }

    title.addEventListener("input", updatePreview);
    content.addEventListener("input", updatePreview);
    category.addEventListener("change", updatePreview);

    saveButton.addEventListener("click", () => save(false));
    submitButton.addEventListener("click", submitReview);

    typeButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            if (mediaType === "IMAGE") {
                mediaByType.IMAGE = mediaUrls.filter(isImageUrl).slice(0, 9);
            } else if (mediaType === "VIDEO") {
                mediaByType.VIDEO = mediaUrls.filter(isVideoUrl).slice(0, 1);
            }
            mediaType = button.dataset.type || "TEXT";
            mediaUrls = mediaType === "TEXT" ? [] : (mediaByType[mediaType] || []).slice();
            syncMediaUi();
            updatePreview();
        });
    });

    if (mediaPick && mediaInput) {
        mediaPick.addEventListener("click", function () {
            mediaInput.click();
        });
        mediaInput.addEventListener("change", function () {
            uploadMedia(mediaInput.files);
        });
    }

    if (mediaThumbs) {
        mediaThumbs.addEventListener("click", function (e) {
            const button = e.target.closest("[data-remove]");
            if (!button) {
                return;
            }
            mediaUrls.splice(Number(button.dataset.remove), 1);
            if (mediaByType[mediaType]) {
                mediaByType[mediaType] = mediaUrls.slice();
            }
            syncMediaUi();
            updatePreview();
        });
    }

    if (back) {
        back.addEventListener("click", async function (e) {
            if (!dirty()) {
                return;
            }
            e.preventDefault();
            const leave = await QX.confirm("当前修改还没有保存，确定离开吗？", {
                title: "放弃修改？",
                confirmText: "离开",
                danger: true
            });
            if (leave) {
                window.location.href = back.href;
            }
        });
    }

    window.addEventListener("beforeunload", function (e) {
        if (dirty()) {
            e.preventDefault();
            e.returnValue = "";
        }
    });

    updatePreview();
    syncMediaUi();
})();
