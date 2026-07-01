package com.medibook.modules.user.service;

import org.springframework.data.domain.Pageable;

import com.medibook.common.response.PageResponse;
import com.medibook.modules.user.dto.request.UserSearchRequest;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.entity.User;

public interface UserService {
    User getCurrentUser();

    PageResponse<UserResponse> getAllUsers(UserSearchRequest request, Pageable pageable);

    UserResponse getUserById(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);
}
