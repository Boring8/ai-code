package com.easen.aicode.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 设备工具类
 */
public class DeviceUtils {

    /**
     * 根据请求获取设备信息
     * 优化：使用更稳定的设备识别逻辑，避免同一浏览器返回不同标识
     **/
    public static String getRequestDevice(HttpServletRequest request) {
        String userAgentStr = request.getHeader(Header.USER_AGENT.toString());
        // 使用 Hutool 解析 UserAgent
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        ThrowUtils.throwIf(userAgent == null, ErrorCode.OPERATION_ERROR, "非法请求");
        
        // 优化设备识别逻辑，使用更稳定的判断
        String device = getStableDeviceType(userAgentStr, userAgent);
        
        return device;
    }

    /**
     * 获取稳定的设备类型
     * 优化：使用更精确的设备识别，避免误判
     */
    private static String getStableDeviceType(String userAgentStr, UserAgent userAgent) {
        // 优先判断小程序
        if (isMiniProgram(userAgentStr)) {
            return "miniProgram";
        }
        
        // 判断平板设备
        if (isPad(userAgentStr)) {
            return "pad";
        }
        
        // 判断移动设备（更精确的判断）
        if (isMobile(userAgentStr, userAgent)) {
            return "mobile";
        }
        
        // 默认为PC
        return "pc";
    }

    /**
     * 判断是否是小程序
     * 一般通过 User-Agent 字符串中的 "MicroMessenger" 来判断是否是微信小程序
     **/
    private static boolean isMiniProgram(String userAgentStr) {
        // 判断 User-Agent 是否包含 "MicroMessenger" 表示是微信环境
        return StrUtil.containsIgnoreCase(userAgentStr, "MicroMessenger")
                && StrUtil.containsIgnoreCase(userAgentStr, "MiniProgram");
    }

    /**
     * 判断是否为平板设备
     * 支持 iOS（如 iPad）和 Android 平板的检测
     **/
    private static boolean isPad(String userAgentStr) {
        // 检查 iPad 的 User-Agent 标志
        boolean isIpad = StrUtil.containsIgnoreCase(userAgentStr, "iPad");

        // 检查 Android 平板（包含 "Android" 且不包含 "Mobile"）
        boolean isAndroidTablet = StrUtil.containsIgnoreCase(userAgentStr, "Android")
                && !StrUtil.containsIgnoreCase(userAgentStr, "Mobile");

        // 如果是 iPad 或 Android 平板，则返回 true
        return isIpad || isAndroidTablet;
    }

    /**
     * 判断是否为移动设备
     * 优化：使用更精确的移动设备判断逻辑
     */
    private static boolean isMobile(String userAgentStr, UserAgent userAgent) {
        // 使用 Hutool 的 UserAgent 判断
        if (userAgent.isMobile()) {
            return true;
        }
        
        // 额外的移动设备关键词检查
        String[] mobileKeywords = {
            "Mobile", "Android", "iPhone", "iPod", "BlackBerry", 
            "Windows Phone", "Opera Mini", "IEMobile"
        };
        
        for (String keyword : mobileKeywords) {
            if (StrUtil.containsIgnoreCase(userAgentStr, keyword)) {
                return true;
            }
        }
        
        return false;
    }
}
