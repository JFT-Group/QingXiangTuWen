package com.qingxiang.util;

import com.qingxiang.vo.CapabilityVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把权限码翻译成账号页的能力卡片；API 仍返回原始 code，只有页面用它做友好展示。
 */
public final class PermissionCatalog {

    private static final Map<String, CapabilityVO> BY_CODE = new LinkedHashMap<>();
    private static final List<CapabilityVO> READER = List.of(
            new CapabilityVO("浏览发现", "看已发布的图文，按分类和关键词找内容"),
            new CapabilityVO("点赞收藏", "喜欢就点亮，收藏随时回看"),
            new CapabilityVO("评论回复", "参与讨论，支持一层回复"),
            new CapabilityVO("关注作者", "在关注流里追更"),
            new CapabilityVO("站内通知", "审核、评论、点赞等提醒"));

    static {
        BY_CODE.put("article:create", new CapabilityVO("发布图文", "发布文本、多图或单视频内容，先存草稿"));
        BY_CODE.put("article:update", new CapabilityVO("编辑图文", "修改自己的内容，已发布编辑后重新送审"));
        BY_CODE.put("article:delete", new CapabilityVO("删除图文", "移除自己的内容，不再出现在信息流"));
        BY_CODE.put("article:submit", new CapabilityVO("提交审核", "把草稿送审，或从待审队列撤回"));
        BY_CODE.put("article:review", new CapabilityVO("审核图文", "通过或驳回待审内容，并写明理由"));
        BY_CODE.put("article:batch", new CapabilityVO("批量审核", "一次处理多条待审内容，失败项保持原状"));
        BY_CODE.put("article:unpublish", new CapabilityVO("下架与上架", "下架不合规内容，处理后可重新上架"));
        BY_CODE.put("comment:manage", new CapabilityVO("评论治理", "隐藏或删除违规评论"));
        BY_CODE.put("category:manage", new CapabilityVO("分类管理", "维护多级分类、排序与启停"));
        BY_CODE.put("user:manage", new CapabilityVO("用户管理", "查询账号、调整角色、停用或删除"));
        BY_CODE.put("log:view", new CapabilityVO("查看日志", "按时间、操作人、动作查治理记录"));
    }

    private PermissionCatalog() {
    }

    /**
     * 没有任何管理权限码时，展示阅读者日常能做的事；否则只展示这个角色多出来的能力。
     */
    public static List<CapabilityVO> describe(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return READER;
        }
        List<CapabilityVO> result = new ArrayList<>();
        BY_CODE.forEach((code, capability) -> {
            if (codes.contains(code)) {
                result.add(capability);
            }
        });
        return result.isEmpty() ? READER : result;
    }
}
