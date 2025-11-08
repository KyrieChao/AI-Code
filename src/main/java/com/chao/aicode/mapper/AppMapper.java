package com.chao.aicode.mapper;

import com.chao.aicode.model.entity.App;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用 映射层。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@Mapper
public interface AppMapper extends BaseMapper<App> {
}
