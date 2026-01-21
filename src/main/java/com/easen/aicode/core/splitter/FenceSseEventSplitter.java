package com.easen.aicode.core.splitter;

import cn.hutool.json.JSONUtil;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 将大模型输出按 ```html ... ``` / ``` 分隔为 explain 与 code 两类 SSE 事件。
 * <p>
 * 规则：
 * <ul>
 *   <li>遇到 {@code ```html}（大小写不敏感）表示“代码区开始”</li>
 *   <li>遇到 {@code ```} 表示“代码区结束”</li>
 *   <li>分隔符本身不输出</li>
 * </ul>
 * 说明：目前先处理 HTML 单文件场景。
 */
public final class FenceSseEventSplitter {

    private FenceSseEventSplitter() {
    }

    /**
     * 将流式文本分割并映射为 SSE 事件流（事件名：explain / code）。
     * <p>
     * 注意：内部使用 {@link Flux#defer} 保证每个订阅者都有独立的分段状态。
     */
    public static Flux<ServerSentEvent<String>> split(Flux<String> contentFlux) {
        return Flux.defer(() -> {
            HtmlFenceSegmenter segmenter = new HtmlFenceSegmenter();
            return contentFlux
                    .concatMap(chunk -> Flux.fromIterable(segmenter.accept(chunk)))
                    .concatWith(Flux.fromIterable(segmenter.finish()))
                    .filter(Objects::nonNull)
                    .map(FenceSseEventSplitter::toSse);
        });
    }

    private static ServerSentEvent<String> toSse(Segment segment) {
        String jsonData = JSONUtil.toJsonStr(Map.of("d", segment.content()));
        return ServerSentEvent.<String>builder()
                .event(segment.type().eventName)
                .data(jsonData)
                .build();
    }

    private enum SegmentType {
        EXPLAIN("explain"),
        CODE("code");

        private final String eventName;

        SegmentType(String eventName) {
            this.eventName = eventName;
        }
    }

    private record Segment(SegmentType type, String content) {
    }

    /**
     * 处理流式 chunk 的围栏分段器（可跨 chunk 识别分隔符）。
     */
    private static final class HtmlFenceSegmenter {
        private static final String START_FENCE = "```html";
        private static final String END_FENCE = "```";
        // 为了跨 chunk 识别 START_FENCE，需要保留至少 START_FENCE.length() - 1 个字符
        private static final int KEEP_EXPLAIN_TAIL = START_FENCE.length() - 1;
        // 为了跨 chunk 识别 END_FENCE，需要保留至少 END_FENCE.length() - 1 个字符
        private static final int KEEP_CODE_TAIL = END_FENCE.length() - 1;

        private final StringBuilder buffer = new StringBuilder();
        private boolean inCode = false;

        List<Segment> accept(String chunk) {
            if (chunk == null || chunk.isEmpty()) {
                return List.of();
            }
            buffer.append(chunk);
            return drain(false);
        }

        List<Segment> finish() {
            return drain(true);
        }

        private List<Segment> drain(boolean flushAll) {
            List<Segment> out = new ArrayList<>();
            while (true) {
                if (!inCode) {
                    int startIdx = indexOfIgnoreCase(buffer, START_FENCE);
                    if (startIdx >= 0) {
                        // 输出分隔符前的说明
                        if (startIdx > 0) {
                            emitIfNotEmpty(out, SegmentType.EXPLAIN, buffer.substring(0, startIdx));
                        }
                        // 丢弃 ```html 这一行（含后续空白与可选换行）
                        int cut = startIdx + START_FENCE.length();
                        cut = skipSpaces(buffer, cut);
                        cut = skipSingleNewline(buffer, cut);
                        buffer.delete(0, cut);
                        inCode = true;
                        continue;
                    }
                    if (flushAll) {
                        emitIfNotEmpty(out, SegmentType.EXPLAIN, buffer.toString());
                        buffer.setLength(0);
                        break;
                    }
                    // 保留尾部用于跨 chunk 识别 ```html
                    int safeLen = Math.max(0, buffer.length() - KEEP_EXPLAIN_TAIL);
                    if (safeLen > 0) {
                        emitIfNotEmpty(out, SegmentType.EXPLAIN, buffer.substring(0, safeLen));
                        buffer.delete(0, safeLen);
                    }
                    break;
                } else {
                    int endIdx = buffer.indexOf(END_FENCE);
                    if (endIdx >= 0) {
                        // 输出分隔符前的代码
                        if (endIdx > 0) {
                            emitIfNotEmpty(out, SegmentType.CODE, buffer.substring(0, endIdx));
                        }
                        // 丢弃 ```（含可选换行）
                        int cut = endIdx + END_FENCE.length();
                        cut = skipSingleNewline(buffer, cut);
                        buffer.delete(0, cut);
                        inCode = false;
                        continue;
                    }
                    if (flushAll) {
                        emitIfNotEmpty(out, SegmentType.CODE, buffer.toString());
                        buffer.setLength(0);
                        break;
                    }
                    // 保留尾部用于跨 chunk 识别 ```
                    int safeLen = Math.max(0, buffer.length() - KEEP_CODE_TAIL);
                    if (safeLen > 0) {
                        emitIfNotEmpty(out, SegmentType.CODE, buffer.substring(0, safeLen));
                        buffer.delete(0, safeLen);
                    }
                    break;
                }
            }
            return out;
        }

        private static void emitIfNotEmpty(List<Segment> out, SegmentType type, String text) {
            if (text == null || text.isEmpty()) {
                return;
            }
            out.add(new Segment(type, text));
        }

        private static int indexOfIgnoreCase(CharSequence src, String needle) {
            String s = src.toString();
            return s.toLowerCase().indexOf(needle.toLowerCase());
        }

        private static int skipSpaces(CharSequence src, int idx) {
            int i = idx;
            while (i < src.length()) {
                char c = src.charAt(i);
                if (c == ' ' || c == '\t') {
                    i++;
                } else {
                    break;
                }
            }
            return i;
        }

        private static int skipSingleNewline(CharSequence src, int idx) {
            int i = idx;
            if (i >= src.length()) {
                return i;
            }
            char c = src.charAt(i);
            if (c == '\n') {
                return i + 1;
            }
            if (c == '\r') {
                if (i + 1 < src.length() && src.charAt(i + 1) == '\n') {
                    return i + 2;
                }
                return i + 1;
            }
            return i;
        }
    }
}

