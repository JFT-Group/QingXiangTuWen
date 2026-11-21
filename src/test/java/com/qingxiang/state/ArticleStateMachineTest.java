package com.qingxiang.state;

import com.qingxiang.enums.ArticleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleStateMachineTest {

    @Test
    void pendingCannotBeEditedDirectly() {
        assertFalse(ArticleStateMachine.canEdit(ArticleStatus.PENDING));
    }

    @Test
    void draftRejectedPublishedUnpublishedCanBeEdited() {
        assertTrue(ArticleStateMachine.canEdit(ArticleStatus.DRAFT));
        assertTrue(ArticleStateMachine.canEdit(ArticleStatus.REJECTED));
        assertTrue(ArticleStateMachine.canEdit(ArticleStatus.PUBLISHED));
        assertTrue(ArticleStateMachine.canEdit(ArticleStatus.UNPUBLISHED));
    }

    @Test
    void editingPublishedOrUnpublishedGoesBackToPending() {
        assertEquals(ArticleStatus.PENDING,
                ArticleStateMachine.statusAfterEdit(ArticleStatus.PUBLISHED));
        assertEquals(ArticleStatus.PENDING,
                ArticleStateMachine.statusAfterEdit(ArticleStatus.UNPUBLISHED));
    }

    @Test
    void editingDraftOrRejectedKeepsStatus() {
        assertEquals(ArticleStatus.DRAFT,
                ArticleStateMachine.statusAfterEdit(ArticleStatus.DRAFT));
        assertEquals(ArticleStatus.REJECTED,
                ArticleStateMachine.statusAfterEdit(ArticleStatus.REJECTED));
    }

    @Test
    void onlyDraftAndRejectedCanSubmit() {
        assertTrue(ArticleStateMachine.canSubmit(ArticleStatus.DRAFT));
        assertTrue(ArticleStateMachine.canSubmit(ArticleStatus.REJECTED));
        assertFalse(ArticleStateMachine.canSubmit(ArticleStatus.PENDING));
        assertFalse(ArticleStateMachine.canSubmit(ArticleStatus.PUBLISHED));
    }

    @Test
    void onlyPendingCanWithdraw() {
        assertTrue(ArticleStateMachine.canWithdraw(ArticleStatus.PENDING));
        assertFalse(ArticleStateMachine.canWithdraw(ArticleStatus.DRAFT));
        assertFalse(ArticleStateMachine.canWithdraw(ArticleStatus.PUBLISHED));
    }

    @Test
    void pendingCannotBeDeletedByAuthor() {
        assertFalse(ArticleStateMachine.canDeleteByAuthor(ArticleStatus.PENDING));
        assertTrue(ArticleStateMachine.canDeleteByAuthor(ArticleStatus.DRAFT));
        assertTrue(ArticleStateMachine.canDeleteByAuthor(ArticleStatus.PUBLISHED));
    }

    @Test
    void reviewActionsMatchExpectedStates() {
        assertTrue(ArticleStateMachine.canApprove(ArticleStatus.PENDING));
        assertTrue(ArticleStateMachine.canReject(ArticleStatus.PENDING));
        assertFalse(ArticleStateMachine.canApprove(ArticleStatus.DRAFT));

        assertTrue(ArticleStateMachine.canUnpublish(ArticleStatus.PUBLISHED));
        assertFalse(ArticleStateMachine.canUnpublish(ArticleStatus.PENDING));

        assertTrue(ArticleStateMachine.canRepublish(ArticleStatus.UNPUBLISHED));
        assertFalse(ArticleStateMachine.canRepublish(ArticleStatus.PUBLISHED));
    }
}
