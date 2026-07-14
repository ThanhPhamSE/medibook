package com.medibook.modules.user.service;

import java.util.List;
import com.medibook.modules.user.dto.response.RoleInfoResponse;
import com.medibook.modules.user.entity.Role;

public interface RoleService {

    Role getDoctorRole();

    List<RoleInfoResponse> getAllRoles();
}

