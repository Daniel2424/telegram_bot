package com.ruzhkov.springDmBot.repositories;

import com.ruzhkov.springDmBot.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Integer> {
    boolean existsById(Integer id);
    //boolean existsByNewsText(String text);
}
