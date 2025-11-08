-- 创建库
create database if not exists ai_code;

-- 切换库
use ai_code;

-- 用户表
-- 以下是建表语句

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;
INSERT INTO app (appName, cover, initPrompt, codeGenType, deployKey, deployedTime, priority, userId, editTime, createTime, updateTime, isDelete)
VALUES
    ('智能问答助手', 'https://example.com/cover1.jpg', '你是一个智能问答助手，请回答用户的问题。', 'chat', 'deploy_key_001', NOW(), 1, 1001, NOW(), NOW(), NOW(), 0),
    ('图像识别工具', 'https://example.com/cover2.jpg', '识别上传图像中的物体并返回标签。', 'vision', 'deploy_key_002', NOW(), 2, 1002, NOW(), NOW(), NOW(), 0),
    ('代码生成器', 'https://example.com/cover3.jpg', '根据用户描述生成对应的代码片段。', 'code', 'deploy_key_003', NOW(), 3, 1003, NOW(), NOW(), NOW(), 0),
    ('文本摘要器', 'https://example.com/cover4.jpg', '将长文本自动摘要为简洁版本。', 'summary', 'deploy_key_004', NOW(), 4, 1004, NOW(), NOW(), NOW(), 0),
    ('语音转文字', 'https://example.com/cover5.jpg', '将语音文件转换为文字内容。', 'audio', 'deploy_key_005', NOW(), 5, 1005, NOW(), NOW(), NOW(), 0),
    ('智能客服', 'https://example.com/cover6.jpg', '模拟客服对话，解答用户常见问题。', 'chat', 'deploy_key_006', NOW(), 6, 1006, NOW(), NOW(), NOW(), 0),
    ('数据可视化助手', 'https://example.com/cover7.jpg', '根据上传的数据生成可视化图表。', 'data', 'deploy_key_007', NOW(), 7, 1007, NOW(), NOW(), NOW(), 0),
    ('翻译助手', 'https://example.com/cover8.jpg', '将输入文本翻译成目标语言。', 'translation', 'deploy_key_008', NOW(), 8, 1008, NOW(), NOW(), NOW(), 0);
-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;
