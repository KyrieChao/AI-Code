package com.chao.aicode.mapper;

import com.mybatisflex.core.BaseMapper;
import com.chao.aicode.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话历史 映射层。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

}
