package com.qingxiang.state;

import com.qingxiang.enums.ArticleStatus;

/**
 * 文章状态机：纯函数，所有合法/非法流转都从这里判断。
 *
 * <p>真正的并发安全由 SQL 的 {@code WHERE status = 旧状态} 保证；
 * 这里只负责业务语义，便于单元测试和答辩讲解。</p>
 */
public final class ArticleStateMachine {

    private ArticleStateMachine() {
    }

    /** 作者可编辑：草稿、驳回、已发布、已下架；待审核必须先撤回。 */
    public static boolean canEdit(ArticleStatus current) {
        return current == ArticleStatus.DRAFT
                || current == ArticleStatus.REJECTED
                || current == ArticleStatus.PUBLISHED
                || current == ArticleStatus.UNPUBLISHED;
    }

    /** 编辑后的状态：已发布/已下架的内容修改后重新进入待审核，其余保持原状态。 */
    public static ArticleStatus statusAfterEdit(ArticleStatus current) {
        if (current == ArticleStatus.PUBLISHED || current == ArticleStatus.UNPUBLISHED) {
            return ArticleStatus.PENDING;
        }
        return current;
    }

    public static boolean canSubmit(ArticleStatus current) {
        return current == ArticleStatus.DRAFT || current == ArticleStatus.REJECTED;
    }

    public static boolean canWithdraw(ArticleStatus current) {
        return current == ArticleStatus.PENDING;
    }

    public static boolean canDeleteByAuthor(ArticleStatus current) {
        return current != ArticleStatus.PENDING;
    }

    public static boolean canApprove(ArticleStatus current) {
        return current == ArticleStatus.PENDING;
    }

    public static boolean canReject(ArticleStatus current) {
        return current == ArticleStatus.PENDING;
    }

    public static boolean canUnpublish(ArticleStatus current) {
        return current == ArticleStatus.PUBLISHED;
    }

    public static boolean canRepublish(ArticleStatus current) {
        return current == ArticleStatus.UNPUBLISHED;
    }
}
