package com.ruzhkov.springDmBot.repositories;

import com.ruzhkov.springDmBot.entity.User;
import com.ruzhkov.springDmBot.entity.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UserId> {

}
