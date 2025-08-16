package com.easen.aicode.service;


import com.easen.aicode.common.PageRequest;
import com.easen.aicode.model.entity.Thumb;
import com.easen.aicode.model.vo.AppThumbDetailVO;
import com.easen.aicode.model.vo.AppThumbVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 *  服务层。
 *
 * @author <a>easen</a>
 */
public interface ThumbService extends IService<Thumb> {

    /**
     * 分页查询应用，按点赞数从高到低排序
     *
     * @param pageRequest 分页请求对象
     * @return 分页结果
     */
    Page<AppThumbVO> getAppThumbPage(PageRequest pageRequest);


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
