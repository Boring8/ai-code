package com.easen.aicode.service.impl;

import com.easen.aicode.mapper.AppMapper;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.service.AppService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a>easen</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

}
