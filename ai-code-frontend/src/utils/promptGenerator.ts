/**
 * AI 零代码应用生成平台 - Prompt 生成工具
 * 用于根据用户输入生成专业的 Prompt
 */

export interface UserInput {
  /** 用户描述 */
  description: string
  /** 颜色偏好 */
  colorTheme?: string
  /** 页面数量 */
  pageCount?: number
  /** 特殊功能 */
  specialFeatures?: string[]
  /** 目标用户 */
  targetUsers?: string
  /** 风格偏好 */
  stylePreference?: string
}

/**
 * 生成完整的 Prompt
 */
export function generatePrompt(userInput: UserInput): string {
  const {
    description,
    colorTheme = '现代简约风格',
    pageCount,
    specialFeatures = [],
    targetUsers,
    stylePreference,
  } = userInput

  let prompt = `你是一位资深的**全栈 Web 开发专家**，擅长使用 HTML、CSS 和 JavaScript 构建现代化、响应式的网站。

## 任务
根据以下用户描述，生成一个完整的、可直接运行的网站，包含 HTML、CSS 和 JavaScript 代码。

## 用户需求
${description}`

  // 添加补充信息
  const additionalInfo: string[] = []
  if (colorTheme) {
    additionalInfo.push(`- 颜色偏好：${colorTheme}`)
  }
  if (pageCount) {
    additionalInfo.push(`- 页面数量：${pageCount}个页面`)
  }
  if (specialFeatures.length > 0) {
    additionalInfo.push(`- 特殊功能：${specialFeatures.join('、')}`)
  }
  if (targetUsers) {
    additionalInfo.push(`- 目标用户：${targetUsers}`)
  }
  if (stylePreference) {
    additionalInfo.push(`- 风格偏好：${stylePreference}`)
  }

  if (additionalInfo.length > 0) {
    prompt += `\n\n【补充信息】\n${additionalInfo.join('\n')}`
  }

  // 添加核心要求
  prompt += `

## 核心要求

### 1. 代码质量
- 使用 HTML5 语义化标签（header, nav, main, section, footer 等）
- CSS 使用现代特性（Flexbox、Grid、CSS Variables）
- JavaScript 使用 ES6+ 语法
- 代码整洁、注释清晰、可维护

### 2. 设计标准
- 现代化、美观的界面设计
${colorTheme ? `- ${colorTheme}配色方案` : '- 合理的颜色方案和字体选择'}
- 响应式布局（适配移动端、平板、桌面端）
- 良好的用户体验和交互效果
- 适当的动画和过渡效果

### 3. 功能完整性
- 导航栏（移动端可折叠）
- 页脚（版权信息等）
- 所有链接和按钮功能正常
${specialFeatures.includes('表单') ? '- 表单验证' : ''}
- 页面间导航流畅
${specialFeatures.length > 0 ? `- ${specialFeatures.join('\n- ')}` : ''}

### 4. 技术要求
- 完全响应式设计（使用媒体查询）
- 确保可访问性（accessibility）
- 性能优化
- 代码可直接运行，不依赖外部库（除非明确要求）

## 输出格式

请按照以下格式输出：

\`\`\`markdown
# 网站生成结果

## 项目说明
[简要说明生成的网站功能和特点]

## 代码

### HTML
\`\`\`html
[完整 HTML 代码]
\`\`\`

### CSS
\`\`\`css
[完整 CSS 代码]
\`\`\`

### JavaScript（如需要）
\`\`\`javascript
[完整 JavaScript 代码]
\`\`\`

## 功能特性
- [功能1]
- [功能2]
- [功能3]
\`\`\`

请开始生成网站代码。`

  return prompt
}

/**
 * 生成简化版 Prompt（用于快速生成）
 */
export function generateSimplePrompt(description: string): string {
  return generatePrompt({ description })
}

/**
 * 预设 Prompt 模板
 */
export const PromptTemplates = {
  /** 企业官网模板 */
  corporate: (description: string) =>
    generatePrompt({
      description,
      colorTheme: '蓝色和白色主题，简约现代风格',
      pageCount: 4,
      specialFeatures: ['联系表单', '服务展示卡片'],
      targetUsers: '企业官网',
    }),

  /** 个人作品集模板 */
  portfolio: (description: string) =>
    generatePrompt({
      description,
      colorTheme: '深色主题，个性化设计',
      pageCount: 3,
      specialFeatures: ['作品网格展示', '悬停效果'],
      targetUsers: '个人作品集',
    }),

  /** 电商网站模板 */
  ecommerce: (description: string) =>
    generatePrompt({
      description,
      colorTheme: '现代电商风格',
      pageCount: 5,
      specialFeatures: ['商品展示', '购物车', '支付表单'],
      targetUsers: '电商网站',
    }),

  /** 博客网站模板 */
  blog: (description: string) =>
    generatePrompt({
      description,
      colorTheme: '阅读友好配色',
      pageCount: 4,
      specialFeatures: ['文章列表', '分类筛选', '搜索功能'],
      targetUsers: '博客网站',
    }),
}

/**
 * 验证用户输入
 */
export function validateUserInput(input: Partial<UserInput>): {
  valid: boolean
  errors: string[]
} {
  const errors: string[] = []

  if (!input.description || input.description.trim().length === 0) {
    errors.push('用户描述不能为空')
  }

  if (input.description && input.description.trim().length < 10) {
    errors.push('用户描述至少需要10个字符')
  }

  return {
    valid: errors.length === 0,
    errors,
  }
}

