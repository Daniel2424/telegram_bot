package com.ruzhkov.springDmBot.repositories;

import com.ruzhkov.springDmBot.entity.City;
import com.ruzhkov.springDmBot.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Integer> {
    List<Weather> findAllByDateAndCity(String date, City city);
    List<Weather> findAllByDateBetweenAndCity(String a, String b, City city);
}
