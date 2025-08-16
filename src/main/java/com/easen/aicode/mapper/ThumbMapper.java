package com.easen.aicode.mapper;


import com.easen.aicode.model.entity.Thumb;
import com.mybatisflex.core.BaseMapper;

import java.util.List;

/**
 *  映射层。
 *
 * @author <a>easen</a>
 */
public interface ThumbMapper extends BaseMapper<Thumb> {

    void deleteBatch(List<Thumb> thumbListDecr);
}
