package com.chao.aicode.converter;

import com.chao.aicode.model.dto.app.AppAddRequest;
import com.chao.aicode.model.dto.app.AppUpdateRequest;
import com.chao.aicode.model.dto.app.AppAdminUpdateRequest;
import com.chao.aicode.model.entity.App;
import com.chao.aicode.model.vo.AppVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AppConverter {
    AppConverter INSTANCE = Mappers.getMapper(AppConverter.class);

    AppVO VoTo(App app);

    App ToAddRequest(AppAddRequest request);

    App ToUpdateRequest(AppUpdateRequest request);

    App ToAdminUpdateRequest(AppAdminUpdateRequest request);
}

