package com.qingxiang.util;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 会话 CSRF token：表单放隐藏域 {@code _csrf}，fetch 放 {@code X-CSRF-Token}。
 */
public final class CsrfUtil {

    public static final String SESSION_TOKEN = "csrfToken";
    public static final String HEADER = "X-CSRF-Token";
    public static final String PARAM = "_csrf";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /** 重新生成并写回 session；登录后调用，防旧 token 复用。 */
    public static String rotate(HttpSession session) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                    .append(Character.forDigit(b & 0xF, 16));
        }
        String token = sb.toString();
        session.setAttribute(SESSION_TOKEN, token);
        return token;
    }

    public static String ensure(HttpSession session) {
        Object token = session.getAttribute(SESSION_TOKEN);
        if (token instanceof String s && !s.isBlank()) {
            return s;
        }
        return rotate(session);
    }

    /** 常量时间比较，避免按前缀逐字节试探。 */
    public static boolean validate(String expected, String actual) {
        if (expected == null || expected.isBlank() || actual == null || actual.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
