(function () {
    "use strict";

    const feed = document.getElementById("feed");
    const skeleton = document.getElementById("feed-skeleton");
    const searchInput = document.getElementById("global-search");
    const modal = document.getElementById("detail-modal");
    const detailDialog = modal ? modal.querySelector(".qx-detail") : null;
    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    const state = {
        keyword: new URLSearchParams(location.search).get("keyword") || "",
        categoryId: new URLSearchParams(location.search).get("categoryId") || "",
        sort: new URLSearchParams(location.search).get("sort") || "latest",
        page: 1,
        size: 24,
        loading: false,
        seq: 0
    };

    function cardHtml(a) {
        const title = QX.escape(a.title);
        const color = QX.escape(a.coverColor || "#15803d");
        const category = QX.escape(a.categoryName || "未分类");
        const author = QX.escape(a.authorName || "");
        const initial = QX.escape((a.authorName || "?").slice(0, 1));
        const mediaType = a.mediaType || "TEXT";
        let cover;
        if (mediaType === "IMAGE" && a.coverUrl) {
            cover = '<a class="cover cover--media" href="' + QX.ctx + '/articles/' + a.id + '" data-detail="' + a.id + '" aria-label="' + title + '">'
                + '<img class="cover-img" src="' + QX.ctx + QX.escape(a.coverUrl) + '" alt="">'
                + '<span class="cover-cat">' + category + '</span></a>';
        } else if (mediaType === "VIDEO") {
            cover = '<a class="cover cover--media" href="' + QX.ctx + '/articles/' + a.id + '" data-detail="' + a.id + '"'
                + ' style="background:' + color + '" aria-label="' + title + '">'
                + '<span class="cover-cat">' + category + '</span>'
                + '<span class="cover-play">' + QX.icon("play") + '</span></a>';
        } else {
            cover = '<a class="cover" href="' + QX.ctx + '/articles/' + a.id + '" data-detail="' + a.id + '"'
                + ' style="background:' + color + '" aria-label="' + title + '">'
                + '<span class="cover-cat">' + category + '</span>'
                + '<strong class="cover-title">' + title + '</strong></a>';
        }
        const feedTitle = mediaType === "TEXT" ? ""
            : '<a class="feed-title" href="' + QX.ctx + '/articles/' + a.id + '" data-detail="' + a.id + '">' + title + '</a>';
        return '<article class="feed-card">' + cover + feedTitle
            + '<div class="feed-meta">'
            + '<span class="feed-author"><span class="mini-avatar">' + initial + '</span>'
            + '<span>' + author + '</span></span>'
            + '<span class="feed-stats">' + QX.icon("heart", "icon-xs") + (a.likeCount || 0) + '</span>'
            + '</div></article>';
    }

    function emptyHtml() {
        return '<div class="feed-empty">' + QX.icon("search")
            + '<h2>还没有匹配的内容</h2><p>换个关键词或分类再试试。</p>'
            + '<a class="qx-btn qx-btn-primary" href="' + QX.ctx + '/creator/articles/new">写一篇</a></div>';
    }

    function updateCount(total) {
        const node = document.querySelector(".feed-head .muted");
        if (node) {
            node.textContent = total + " 篇";
        }
    }

    function updateLoadMore(data) {
        let button = document.getElementById("load-more");
        const hasMore = data.page * data.size < data.total;
        if (!hasMore) {
            if (button) {
                button.parentElement.remove();
            }
            return;
        }
        if (!button) {
            const wrap = document.createElement("div");
            wrap.style.cssText = "text-align:center;padding:18px 0 8px";
            wrap.innerHTML = '<button class="qx-btn qx-btn-fill" id="load-more">加载更多</button>';
            feed.parentElement.appendChild(wrap);
            button = wrap.querySelector("button");
            button.addEventListener("click", loadMore);
        }
        button.dataset.page = data.page;
        button.dataset.size = data.size;
        button.disabled = false;
    }

    function animateCards(nodes) {
        if (reduced || !window.gsap) {
            return;
        }
        gsap.from(nodes, {
            clipPath: "inset(0 0 18% 0)",
            opacity: 0,
            duration: 0.45,
            ease: "power2.out",
            stagger: 0.05,
            clearProps: "clipPath,opacity"
        });
    }

    async function fetchFeed(reset) {
        if (state.loading) {
            return;
        }
        state.loading = true;
        const seq = ++state.seq;
        if (reset) {
            state.page = 1;
            if (skeleton) {
                skeleton.hidden = false;
            }
        }
        const params = new URLSearchParams();
        if (state.keyword) {
            params.set("keyword", state.keyword);
        }
        if (state.categoryId) {
            params.set("categoryId", state.categoryId);
        }
        params.set("sort", state.sort);
        params.set("page", reset ? 1 : state.page + 1);
        params.set("size", state.size);
        try {
            const data = await QX.api(QX.ctx + "/api/v1/articles?" + params.toString());
            if (seq !== state.seq) {
                return;
            }
            state.page = data.page;
            if (skeleton) {
                skeleton.hidden = true;
            }
            const oldEmpty = document.querySelector(".feed-empty");
            if (oldEmpty) {
                oldEmpty.remove();
            }
            if (reset) {
                feed.innerHTML = data.list.map(cardHtml).join("");
            } else {
                feed.insertAdjacentHTML("beforeend", data.list.map(cardHtml).join(""));
            }
            if (data.total === 0) {
                feed.insertAdjacentHTML("afterend", emptyHtml());
            }
            updateCount(data.total);
            updateLoadMore(data);
            const cards = reset ? Array.from(feed.children) : Array.from(feed.children).slice(-data.list.length);
            animateCards(cards);
            syncUrl();
        } catch (e) {
            if (skeleton) {
                skeleton.hidden = true;
            }
            QX.toast(e.message || "加载失败", "error");
        } finally {
            state.loading = false;
        }
    }

    function syncUrl() {
        const params = new URLSearchParams();
        if (state.keyword) {
            params.set("keyword", state.keyword);
        }
        if (state.categoryId) {
            params.set("categoryId", state.categoryId);
        }
        if (state.sort && state.sort !== "latest") {
            params.set("sort", state.sort);
        }
        const query = params.toString();
        history.replaceState(history.state, "", QX.ctx + "/discover" + (query ? "?" + query : ""));
    }

    function loadMore() {
        fetchFeed(false);
    }

    const debouncedSearch = QX.debounce(function () {
        state.keyword = searchInput.value.trim();
        fetchFeed(true);
    }, 250);

    if (searchInput) {
        searchInput.addEventListener("input", debouncedSearch);
        searchInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                e.preventDefault();
                state.keyword = searchInput.value.trim();
                fetchFeed(true);
            }
        });
    }

    document.querySelectorAll(".channel").forEach(function (channel) {
        channel.addEventListener("click", function (e) {
            e.preventDefault();
            const href = channel.getAttribute("href") || "";
            const url = new URL(href, location.origin);
            state.categoryId = url.searchParams.get("categoryId") || "";
            document.querySelectorAll(".channel").forEach((c) => c.classList.remove("is-active"));
            channel.classList.add("is-active");
            fetchFeed(true);
        });
    });

    document.addEventListener("click", function (e) {
        const link = e.target.closest("[data-detail]");
        if (link) {
            e.preventDefault();
            openDetail(link.dataset.detail, link);
            return;
        }
        if (e.target === modal) {
            closeDetail(true);
        }
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && modal && !modal.hidden) {
            closeDetail(true);
        }
    });

    async function openDetail(id, source, options) {
        if (!modal) {
            return;
        }
        const opts = options || {};
        try {
            const data = await QX.api(QX.ctx + "/api/v1/articles/" + id);
            if (source) {
                const cover = source.querySelector(".cover") || source;
                if (window.Flip && window.gsap && !reduced) {
                    try {
                        const flipState = Flip.getState(cover);
                        modal.hidden = false;
                        Flip.from(flipState, {
                            targets: modal.querySelector(".qx-detail-media"),
                            duration: 0.32,
                            ease: "power3.out",
                            absolute: true,
                            scale: true
                        });
                    } catch (ignored) {
                        modal.hidden = false;
                    }
                } else {
                    modal.hidden = false;
                }
            } else {
                modal.hidden = false;
            }
            fillDetail(data);
            document.body.style.overflow = "hidden";
            if (opts.push !== false) {
                history.pushState({detail: id}, "", QX.ctx + "/articles/" + id);
            }
            setTimeout(() => document.getElementById("detail-close")?.focus(), 40);
        } catch (e) {
            QX.toast(e.message || "内容打不开", "error");
        }
    }

    function fillDetail(a) {
        const media = document.getElementById("detail-media");
        const mediaType = a.mediaType || "TEXT";
        const mediaUrls = a.mediaUrls || [];
        detailDialog.classList.toggle("is-media", mediaType !== "TEXT");
        if (mediaType === "IMAGE" && mediaUrls.length) {
            media.style.background = "#000";
            media.innerHTML = '<div class="media-gallery">' + mediaUrls.map(function (url) {
                return '<img src="' + QX.ctx + QX.escape(url) + '" alt="">';
            }).join("") + '</div>';
        } else if (mediaType === "VIDEO" && mediaUrls.length) {
            media.style.background = "#000";
            media.innerHTML = '<video class="media-video" controls preload="metadata" src="'
                + QX.ctx + QX.escape(mediaUrls[0]) + '"></video>';
        } else {
            media.style.background = a.coverColor || "#15803d";
            media.innerHTML = '<span class="cover-cat" id="detail-cover-cat"></span>'
                + '<strong class="cover-title" id="detail-cover-title"></strong>';
            document.getElementById("detail-cover-cat").textContent = a.categoryName || "未分类";
            document.getElementById("detail-cover-title").textContent = a.title || "";
        }
        document.getElementById("detail-avatar").textContent = (a.authorName || "?").slice(0, 1);
        document.getElementById("detail-author").textContent = a.authorName || "";
        document.getElementById("detail-title").textContent = a.title || "";
        document.getElementById("detail-content").innerHTML = a.content || "";
        document.getElementById("detail-meta").innerHTML =
            "<span>" + QX.escape(QX.formatTime(a.publishTime || a.updateTime)) + "</span>"
            + "<span>" + QX.escape(a.categoryName || "未分类") + "</span>";
        document.getElementById("detail-like").textContent = a.likeCount || 0;
        document.getElementById("detail-favorite").textContent = a.favoriteCount || 0;
        document.getElementById("detail-comment").textContent = a.commentCount || 0;
        const open = document.getElementById("detail-open");
        open.href = QX.ctx + "/articles/" + a.id;
    }

    function closeDetail(fromUser) {
        if (!modal || modal.hidden) {
            return;
        }
        modal.hidden = true;
        document.body.style.overflow = "";
        if (fromUser && history.state && history.state.detail) {
            history.back();
        } else if (fromUser) {
            history.replaceState(null, "", QX.ctx + "/discover");
        }
    }

    const closeButton = document.getElementById("detail-close");
    if (closeButton) {
        closeButton.addEventListener("click", () => closeDetail(true));
    }

    window.addEventListener("popstate", function () {
        const match = location.pathname.match(/\/articles\/(\d+)$/);
        if (match) {
            if (modal && modal.hidden) {
                openDetail(match[1], null, {push: false});
            } else if (modal && !modal.hidden) {
                openDetail(match[1], null, {push: false});
            }
        } else if (modal && !modal.hidden) {
            closeDetail(false);
        }
    });

    if (document.getElementById("load-more")) {
        document.getElementById("load-more").addEventListener("click", loadMore);
    }

    animateCards(Array.from(feed.children));

    const initialDetail = location.pathname.match(/\/articles\/(\d+)$/);
    if (initialDetail) {
        openDetail(initialDetail[1], null, {push: false});
    }
})();
