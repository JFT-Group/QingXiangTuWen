package com.qingxiang.service;

import com.qingxiang.entity.Notification;
import com.qingxiang.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A 模块约定的通知入口。B/C 只调用它，不直接写 notification 表。
 * 自己操作自己不发通知。
 */
@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void notify(Long receiverId, Long actorId, String type,
                       String targetType, Long targetId, String content) {
        if (receiverId == null || receiverId.equals(actorId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId == null ? 0L : targetId);
        notification.setContent(content == null ? "" : content);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
