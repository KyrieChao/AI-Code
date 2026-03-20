package com.chao.aicode.controller;

import com.chao.aicode.annotation.AuthCheck;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.response.ApiResponse;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.chao.aicode.model.entity.ChatHistory;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.service.ChatHistoryService;
import com.chao.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public ApiResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ApiResponse.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, HTTPResponseCode.PARAM_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ApiResponse.success(result);
    }
}
