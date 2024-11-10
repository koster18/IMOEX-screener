package ru.sterkhovkv.IMOEX_screener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sterkhovkv.IMOEX_screener.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByName(String username);
}
