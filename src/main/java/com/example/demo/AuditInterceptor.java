package com.example.demo;

import com.example.demo.model.AuditLog;
import com.example.demo.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Method;

@Aspect
@Component
public class AuditInterceptor {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ Intercept all @Transactional service methods
    @Around("@annotation(org.springframework.transaction" +
            ".annotation.Transactional)")
    public Object auditTransactional(
            ProceedingJoinPoint pjp) throws Throwable {

        // Get method info
        MethodSignature sig =
            (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        String methodName = method.getName();
        String className =
            pjp.getTarget().getClass().getSimpleName();

        // Skip non-domain service methods
        if (!isDomainService(className)) {
            return pjp.proceed();
        }

        // ✅ Get actor from SecurityContext
        String actorId = extractActorId();

        // ✅ Get IP from request
        String ipAddress = extractIpAddress();

        // ✅ Determine action from method name
        String action = determineAction(methodName);
        if (action == null) {
            return pjp.proceed();
        }

        // ✅ Determine entity type from class name
        String entityType = className
            .replace("Service", "");

        // Capture before state (first arg if exists)
        String beforeSnapshot = null;
        Object[] args = pjp.getArgs();
        if (args != null && args.length > 0) {
            try {
                beforeSnapshot =
                    objectMapper.writeValueAsString(
                        args[0]);
            } catch (Exception ignored) {}
        }

        // ✅ Execute the actual method
        Object result = pjp.proceed();

        // Capture after state
        String afterSnapshot = null;
        if (result != null &&
            !(result instanceof String)) {
            try {
                afterSnapshot =
                    objectMapper.writeValueAsString(
                        result);
            } catch (Exception ignored) {}
        }

        // ✅ Extract entity ID from result
        String entityId = extractEntityId(result);

        // ✅ Save audit log asynchronously
        try {
            auditLogService.logAction(
                actorId,
                action,
                entityType,
                entityId,
                beforeSnapshot,
                afterSnapshot,
                ipAddress
            );
        } catch (Exception e) {
            System.err.println(
                "⚠️ Audit log failed (non-critical): "
                + e.getMessage());
        }

        return result;
    }

    // ✅ Extract current user ID from SecurityContext
    private String extractActorId() {
        try {
            Authentication auth =
                SecurityContextHolder.getContext()
                    .getAuthentication();
            if (auth != null &&
                auth.getPrincipal() != null) {
                return auth.getPrincipal().toString();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    // ✅ Extract IP from current HTTP request
    private String extractIpAddress() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes)
                RequestContextHolder
                    .getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req =
                    attrs.getRequest();
                String ip = req.getHeader(
                    "X-Forwarded-For");
                if (ip == null || ip.isEmpty())
                    ip = req.getRemoteAddr();
                return ip;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ✅ Map method name to action
    private String determineAction(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.contains("create") ||
            lower.contains("register") ||
            lower.contains("save") ||
            lower.contains("add"))
            return AuditLog.ACTION_CREATE;
        if (lower.contains("update") ||
            lower.contains("edit") ||
            lower.contains("change"))
            return AuditLog.ACTION_UPDATE;
        if (lower.contains("delete") ||
            lower.contains("remove"))
            return AuditLog.ACTION_DELETE;
        if (lower.contains("approve"))
            return AuditLog.ACTION_APPROVE;
        if (lower.contains("reject"))
            return AuditLog.ACTION_REJECT;
        if (lower.contains("login") ||
            lower.contains("authenticate"))
            return AuditLog.ACTION_LOGIN;
        if (lower.contains("logout"))
            return AuditLog.ACTION_LOGOUT;
        if (lower.contains("confirm"))
            return AuditLog.ACTION_APPROVE;
        return null; // don't log read operations
    }

    // ✅ Only intercept domain services
    private boolean isDomainService(String className) {
        return className.equals("DonationService") ||
               className.equals("NgoService") ||
               className.equals("NeedService") ||
               className.equals("FulfillmentService") ||
               className.equals("UserService") ||
               className.equals("NgoMemberService");
    }

    // ✅ Extract entity ID from result object
    private String extractEntityId(Object result) {
        if (result == null) return null;
        try {
            // Try to call getId() or getDonationId() etc
            java.lang.reflect.Method[] methods =
                result.getClass().getMethods();
            for (java.lang.reflect.Method m : methods) {
                String name = m.getName().toLowerCase();
                if ((name.endsWith("id") ||
                     name.equals("getid")) &&
                    m.getParameterCount() == 0) {
                    Object id = m.invoke(result);
                    if (id != null)
                        return id.toString();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}