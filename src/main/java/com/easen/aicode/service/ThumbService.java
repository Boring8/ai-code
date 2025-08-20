package com.easen.aicode.service;


import com.easen.aicode.common.PageRequest;
import com.easen.aicode.model.entity.Thumb;
import com.easen.aicode.model.vo.AppThumbDetailVO;
import com.easen.aicode.model.vo.AppThumbVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Set;

/**
 * 点赞与排行榜 服务层。
 *
 * <p>提供应用点赞、取消点赞、查询点赞状态以及获取点赞排行榜等能力。</p>
 *
 * @author <a>easen</a>
 */
public interface ThumbService extends IService<Thumb> {

    /**
     * 获取应用点赞排行榜列表
     *
     * @return 应用点赞信息列表（包含应用及其点赞数等聚合信息）
     */
    List<AppThumbVO> appThumbList();


    /**
     * 用户点赞应用
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否点赞成功
     */
    boolean likeApp(Long appId, Long userId);

    /**
     * 用户取消点赞应用
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否取消点赞成功
     */
    boolean unlikeApp(Long appId, Long userId);

    /**
     * 检查用户是否已点赞应用
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isUserLikedApp(Long appId, Long userId);
}
