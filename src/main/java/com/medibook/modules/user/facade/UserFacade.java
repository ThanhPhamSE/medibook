package com.medibook.modules.user.facade;

import com.medibook.modules.user.dto.internal.CurrentUserDto;
import com.medibook.modules.user.entity.User;

public interface UserFacade {

    User getUserById(Long id);

    void upgradeToDoctor(Long userId);

    CurrentUserDto getCurrentUser();

}
