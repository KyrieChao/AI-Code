# Prompt 使用指南

## 概述

本项目提供了专业的 Prompt 模板和生成工具，用于 AI 零代码应用生成平台。根据用户描述自动生成完整的网站代码（HTML + CSS + JS）。

## 文件说明

1. **prompt-template.md** - 详细的 Prompt 模板文档，包含完整的说明和最佳实践
2. **prompt-simple.md** - 简化版 Prompt 模板，可直接使用
3. **src/utils/promptGenerator.ts** - TypeScript 工具类，用于在代码中生成 Prompt

## 快速开始

### 方式一：使用简化模板（手动）

1. 打开 `prompt-simple.md`
2. 复制标准 Prompt 模板
3. 替换 `{用户描述}` 为用户实际需求
4. 发送给 AI 模型

### 方式二：使用代码生成（推荐）

```typescript
import { generatePrompt, PromptTemplates } from '@/utils/promptGenerator'

// 基础用法
const prompt = generatePrompt({
  description: '我需要一个展示我们公司服务的网站，包含首页、关于我们、服务和联系我们页面。',
  colorTheme: '蓝色和白色主题',
  pageCount: 4,
  specialFeatures: ['联系表单', '服务展示'],
  targetUsers: '企业官网',
})

// 使用预设模板
const corporatePrompt = PromptTemplates.corporate(
  '我需要一个展示我们公司服务的网站'
)

const portfolioPrompt = PromptTemplates.portfolio(
  '我需要一个个人作品集网站'
)
```

## 使用示例

### 示例 1：企业官网

```typescript
import { generatePrompt } from '@/utils/promptGenerator'

const userInput = {
  description: '我需要一个展示我们公司提供的网页设计和开发服务的网站。网站应具有现代感，主要颜色为蓝色和白色。首页应包含我们的公司简介和服务概览。关于我们页面应详细介绍我们的团队和公司历史。服务页面应列出我们提供的具体服务，如 UI/UX 设计、前端开发、后端开发等。联系我们页面应提供一个表单，供潜在客户与我们联系。',
  colorTheme: '蓝色和白色主题，简约现代风格',
  pageCount: 4,
  specialFeatures: ['联系表单', '服务展示卡片'],
  targetUsers: '企业官网',
}

const prompt = generatePrompt(userInput)
// 将 prompt 发送给 AI 模型
```

### 示例 2：个人作品集

```typescript
import { PromptTemplates } from '@/utils/promptGenerator'

const prompt = PromptTemplates.portfolio(
  '我需要一个个人作品集网站，展示我的设计作品和开发项目。网站应该有个性化、创意十足的设计，使用深色主题。'
)
```

### 示例 3：自定义需求

```typescript
import { generatePrompt, validateUserInput } from '@/utils/promptGenerator'

const userInput = {
  description: '我需要一个在线教育平台，包含课程列表、课程详情、用户登录和购买功能。',
  colorTheme: '教育风格，蓝色和绿色主题',
  pageCount: 6,
  specialFeatures: ['用户登录', '课程购买', '视频播放', '支付功能'],
  targetUsers: '在线教育平台',
  stylePreference: '专业、可信赖',
}

// 验证输入
const validation = validateUserInput(userInput)
if (!validation.valid) {
  console.error('输入验证失败:', validation.errors)
  return
}

// 生成 Prompt
const prompt = generatePrompt(userInput)
```

## 在 Vue 组件中使用

```vue
<template>
  <div>
    <a-form :model="formState" @finish="handleSubmit">
      <a-form-item label="网站描述" name="description">
        <a-textarea
          v-model:value="formState.description"
          :rows="4"
          placeholder="请描述您需要的网站..."
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">生成网站</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { generatePrompt, validateUserInput } from '@/utils/promptGenerator'
import { message } from 'ant-design-vue'

const formState = reactive({
  description: '',
})

const handleSubmit = async () => {
  // 验证输入
  const validation = validateUserInput({ description: formState.description })
  if (!validation.valid) {
    message.error(validation.errors.join(', '))
    return
  }

  // 生成 Prompt
  const prompt = generatePrompt({
    description: formState.description,
  })

  // 调用 AI API 生成网站代码
  // const result = await callAIService(prompt)
  console.log('生成的 Prompt:', prompt)
}
</script>
```

## Prompt 优化建议

### 1. 明确性
- ✅ 使用清晰、具体的指令
- ✅ 避免模糊或歧义的表达
- ✅ 明确指定输出格式

### 2. 上下文
- ✅ 提供足够的背景信息
- ✅ 说明目标用户和使用场景
- ✅ 明确技术栈和工具要求

### 3. 约束条件
- ✅ 明确代码质量标准
- ✅ 指定浏览器兼容性要求
- ✅ 说明性能要求

### 4. 迭代优化
- ✅ 根据生成结果调整 Prompt
- ✅ 添加更具体的需求描述
- ✅ 优化输出格式要求

## 常见问题

### Q: 如何提高生成代码的质量？
A: 
1. 提供更详细的用户描述
2. 明确指定设计风格和功能要求
3. 使用预设模板作为起点
4. 根据生成结果迭代优化 Prompt

### Q: 生成的代码不符合要求怎么办？
A:
1. 检查用户描述是否足够详细
2. 补充更具体的约束条件
3. 调整 Prompt 中的要求部分
4. 使用更具体的示例

### Q: 如何支持多语言？
A: 可以在 Prompt 中添加语言要求，例如：
```
- 网站语言：中文/英文
- 文本内容：使用中文
```

## 最佳实践

1. **用户输入验证**：始终验证用户输入，确保描述足够详细
2. **使用预设模板**：对于常见场景，使用预设模板可以提高效率
3. **迭代优化**：根据实际使用效果，不断优化 Prompt 模板
4. **代码审查**：生成的代码需要经过审查和测试
5. **用户反馈**：收集用户反馈，持续改进 Prompt 质量

## 参考资料

- [阿里云 Prompt 工程指南](https://help.aliyun.com/zh/model-studio/use-cases/prompt-engineering-guide)
- [OpenAI Prompt Engineering Guide](https://platform.openai.com/docs/guides/prompt-engineering)
- [Prompt Engineering Best Practices](https://www.promptingguide.ai/)

