package com.medibook.modules.user.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionInfoResponse {
    private String id;
    private String module;
    private String action;
    private List<String> roles;
}
