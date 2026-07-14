package com.medibook.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleInfoResponse {
    private Long id;
    private String name;
    private String description;
    private long usersCount;
    private int permissions;
}
