package com.medibook.modules.user.dto.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentUserResponse {

    private Long id;

    private String fullName;

}
