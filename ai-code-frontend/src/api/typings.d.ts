declare namespace API {
  type ApiResponseAppVO = {
    code?: number
    message?: string
    data?: AppVO
    timestamp?: string
    description?: string
  }

  type ApiResponseBoolean = {
    code?: number
    message?: string
    data?: boolean
    timestamp?: string
    description?: string
  }

  type ApiResponseLoginUserVO = {
    code?: number
    message?: string
    data?: LoginUserVO
    timestamp?: string
    description?: string
  }

  type ApiResponseLong = {
    code?: number
    message?: string
    data?: number
    timestamp?: string
    description?: string
  }

  type ApiResponsePageAppVO = {
    code?: number
    message?: string
    data?: PageAppVO
    timestamp?: string
    description?: string
  }

  type ApiResponsePageUserVO = {
    code?: number
    message?: string
    data?: PageUserVO
    timestamp?: string
    description?: string
  }

  type ApiResponseString = {
    code?: number
    message?: string
    data?: string
    timestamp?: string
    description?: string
  }

  type ApiResponseUser = {
    code?: number
    message?: string
    data?: User
    timestamp?: string
    description?: string
  }

  type ApiResponseUserVO = {
    code?: number
    message?: string
    data?: UserVO
    timestamp?: string
    description?: string
  }

  type AppAddRequest = {
    appName?: string
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: string | number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: string | number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: string | number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: string | number
    appName?: string
  }

  type AppVO = {
    id?: string | number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type chatGenCodeParams = {
    appId: string | number
    message: string
  }

  type DeleteRequest = {
    id?: string
  }

  type getAppByIdParams = {
    id: string | number
  }

  type getAppVOByIdParams = {
    id: string | number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
