package com.medibook.security.context;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.medibook.modules.user.entity.User;

@Component
@RequestScope
public class CurrentUserRequestCache {

    private User user;

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    public void setUser(User user) {
        this.user = user;
    }
}
