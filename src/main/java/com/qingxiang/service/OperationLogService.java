package com.qingxiang.service;

import com.qingxiang.dto.LoginUser;
import com.qingxiang.entity.OperationLog;
import com.qingxiang.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 审核/分类变更的操作日志适配器。
 *
 * <p>operation_log 归 A 所有；A 尚未提供写入口时，B 通过本服务追加，
 * 后续可由 A 接管同一接口而不影响 B 的调用方。</p>
 */
@Service
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void record(LoginUser operator, String action, String targetType, Long targetId,
                       String result, String reason, String detail) {
        if (operator == null) {
            return;
        }
        OperationLog log = new OperationLog();
        log.setOperatorId(operator.getId());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? 0L : targetId);
        log.setResult(result == null ? "SUCCESS" : result);
        log.setReason(reason);
        log.setDetail(detail);
        log.setIp(currentIp());
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private String currentIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }
}
