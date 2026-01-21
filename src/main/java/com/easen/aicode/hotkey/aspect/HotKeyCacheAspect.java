//package com.easen.aicode.hotkey.aspect;
//
//import cn.hutool.core.util.ReflectUtil;
//import cn.hutool.core.util.StrUtil;
//import com.easen.aicode.hotkey.annotation.HotKeyCache;
//import com.easen.aicode.common.BaseResponse;
//import com.easen.aicode.common.ResultUtils;
//import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//
///**
// * 热键缓存切面
// * 实现热键缓存的 AOP 环绕切面
// */
////@Aspect
////@Component
//@Slf4j
//public class HotKeyCacheAspect {
//
//    /**
//     * 环绕通知，处理热键缓存逻辑
//     */
//    @Around("@annotation(com.easen.aicode.hotkey.annotation.HotKeyCache)")
//    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
//        // 获取方法签名和注解
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Method method = signature.getMethod();
//        HotKeyCache annotation = method.getAnnotation(HotKeyCache.class);
//        
//        // 构建缓存键
//        String cacheKey = buildCacheKey(joinPoint, annotation);
//        
//        // 如果启用热键检测且是热键，尝试从缓存获取
//        if (annotation.enableHotKey() && JdHotKeyStore.isHotKey(cacheKey)) {
//            Object cachedValue = JdHotKeyStore.get(cacheKey);
//            if (cachedValue != null) {
//                log.debug("从热键缓存获取数据，key: {}", cacheKey);
//                return buildResponse(cachedValue, method);
//            }
//        }
//        
//        // 执行原方法
//        Object result = joinPoint.proceed();
//        
//        // 处理缓存逻辑
//        handleCache(result, cacheKey, annotation, method);
//        
//        return result;
//    }
//    
//    /**
//     * 处理缓存逻辑
//     */
//    private void handleCache(Object result, String cacheKey, HotKeyCache annotation, Method method) {
//        if (result == null) {
//            if (annotation.cacheNull()) {
//                JdHotKeyStore.smartSet(cacheKey, null);
//                log.debug("空值已存入热键缓存，key: {}", cacheKey);
//            }
//            return;
//        }
//        
//        Object dataToCache = result;
//        if (result instanceof BaseResponse) {
//            BaseResponse<?> response = (BaseResponse<?>) result;
//            dataToCache = response.getData();
//            
//            // 如果数据为空且不缓存空值，直接返回
//            if (dataToCache == null && !annotation.cacheNull()) {
//                return;
//            }
//        }
//        
//        // 将结果存入热键缓存
//        if (dataToCache != null || annotation.cacheNull()) {
//            JdHotKeyStore.smartSet(cacheKey, dataToCache);
//            log.debug("数据已存入热键缓存，key: {}", cacheKey);
//        }
//    }
//    
//    /**
//     * 构建响应结果
//     */
//    private Object buildResponse(Object cachedValue, Method method) {
//        // 如果方法返回类型是 BaseResponse，需要包装成 BaseResponse
//        if (method.getReturnType().equals(BaseResponse.class)) {
//            return ResultUtils.success(cachedValue);
//        }
//        return cachedValue;
//    }
//    
//    /**
//     * 构建缓存键
//     */
//    private String buildCacheKey(ProceedingJoinPoint joinPoint, HotKeyCache annotation) {
//        StringBuilder keyBuilder = new StringBuilder();
//        
//        // 添加前缀
//        if (StrUtil.isNotBlank(annotation.prefix())) {
//            keyBuilder.append(annotation.prefix());
//        }
//        
//        // 获取键参数
//        Object[] args = joinPoint.getArgs();
//        if (args.length > annotation.keyParamIndex()) {
//            Object keyParam = args[annotation.keyParamIndex()];
//            
//            // 如果指定了字段名，尝试从对象中获取字段值
//            if (StrUtil.isNotBlank(annotation.keyField()) && keyParam != null) {
//                try {
//                    Object fieldValue = ReflectUtil.getFieldValue(keyParam, annotation.keyField());
//                    if (fieldValue != null) {
//                        keyBuilder.append(fieldValue);
//                    } else {
//                        keyBuilder.append(keyParam);
//                    }
//                } catch (Exception e) {
//                    log.warn("获取字段值失败，使用原参数值，field: {}, param: {}", annotation.keyField(), keyParam);
//                    keyBuilder.append(keyParam);
//                }
//            } else {
//                keyBuilder.append(keyParam);
//            }
//        }
//        
//        return keyBuilder.toString();
//    }
//}
