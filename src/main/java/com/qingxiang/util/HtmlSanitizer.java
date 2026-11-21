package com.qingxiang.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * 正文 HTML 白名单：允许结构标签与 http/https 链接，丢弃脚本、样式、图片与事件属性。
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.none()
            .addTags("p", "br", "strong", "em", "b", "i", "u",
                    "ul", "ol", "li", "h2", "h3", "blockquote", "code", "pre", "a")
            .addAttributes("a", "href", "title")
            .addProtocols("a", "href", "http", "https");

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String clean = Jsoup.clean(html, "", SAFELIST,
                new Document.OutputSettings().prettyPrint(false));
        Document doc = Jsoup.parseBodyFragment(clean);
        doc.outputSettings().prettyPrint(false);
        doc.select("a[href]").attr("rel", "nofollow noopener").attr("target", "_blank");
        return doc.body().html().trim();
    }

    /** 纯文本长度，用于“正文不能为空”的判断，避免只有空标签也算有内容。 */
    public static int textLength(String html) {
        if (html == null || html.isBlank()) {
            return 0;
        }
        return Jsoup.parseBodyFragment(html).text().trim().length();
    }
}
