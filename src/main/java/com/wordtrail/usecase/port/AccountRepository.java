package com.wordtrail.usecase.port;

import com.wordtrail.entity.User;
import java.util.Optional;

public interface AccountRepository {
    Optional<User> findUser(String username);
    boolean hasAccounts();
    void createUser(User user);
}
