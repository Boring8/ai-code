package com.easen.aicode.core.anchorPoint;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * 在 HTML 中注入稳定锚点，用于后续“局部 AST 修改”的元素定位。
 *
 * 规则：
 * - 保留已有的 data-aicode-id，不做重写
 * - 仅对候选元素注入（区块容器/交互控件/标题/具有语义属性的节点）
 * - ID 使用短随机串，写入后即稳定
 */
public final class HtmlStableAnchorInjector {

    public static final String ANCHOR_ATTR = "data-aicode-id";

    private HtmlStableAnchorInjector() {
    }

    /**
     * 为 HTML 注入 data-aicode-id（保留已有值）。
     *
     * @param html 原始 HTML
     * @return 注入后的 HTML
     */
    public static String injectStableAnchors(String html) {
        if (StrUtil.isBlank(html)) {
            return html;
        }

        Document document = Jsoup.parse(html);
        // 尽量减少格式化带来的 diff
        document.outputSettings().prettyPrint(false);

        Element body = document.body();
        if (body == null) {
            return html;
        }

        Set<String> usedIds = new HashSet<>();
        for (Element element : document.getAllElements()) {
            if (element.hasAttr(ANCHOR_ATTR)) {
                String existing = element.attr(ANCHOR_ATTR);
                if (StrUtil.isNotBlank(existing)) {
                    usedIds.add(existing);
                }
            }
        }

        for (Element element : body.getAllElements()) {
            if (element == body) {
                continue;
            }
            if (!isCandidate(element)) {
                continue;
            }
            if (element.hasAttr(ANCHOR_ATTR) && StrUtil.isNotBlank(element.attr(ANCHOR_ATTR))) {
                continue;
            }

            String id = generateUniqueId(usedIds);
            if (StrUtil.isNotBlank(id)) {
                element.attr(ANCHOR_ATTR, id);
                usedIds.add(id);
            }
        }

        return document.outerHtml();
    }

    private static String generateUniqueId(Set<String> usedIds) {
        for (int i = 0; i < 10; i++) {
            String id = "aic_" + IdUtil.fastSimpleUUID().substring(0, 10);
            if (!usedIds.contains(id)) {
                return id;
            }
        }
        return null;
    }

    /**
     * 候选节点：覆盖“区块容器 + 交互控件 + 标题 + 语义节点”，避免过度膨胀。
     */
    private static boolean isCandidate(Element element) {
        String tag = element.tagName();
        if ("html".equalsIgnoreCase(tag) || "head".equalsIgnoreCase(tag) || "body".equalsIgnoreCase(tag)) {
            return false;
        }
        // 预留跳过开关（如后续需要）
        if (element.hasAttr("data-aicode-skip")) {
            return false;
        }

        if (element.hasAttr("id") || !element.classNames().isEmpty() || element.hasAttr("role") || element.hasAttr("aria-label")) {
            return true;
        }

        return switch (tag.toLowerCase()) {
            case "header", "nav", "main", "footer", "section", "article", "aside", "form", "div" -> true;
            case "button", "a", "input", "textarea", "select" -> true;
            case "h1", "h2", "h3", "h4", "h5", "h6" -> true;
            default -> false;
        };
    }
}

