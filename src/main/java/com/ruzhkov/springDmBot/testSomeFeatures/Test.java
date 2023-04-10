package com.ruzhkov.springDmBot.testSomeFeatures;

import com.ruzhkov.springDmBot.entity.Weather;
import com.ruzhkov.springDmBot.repositories.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class Test {
    private final WeatherRepository weatherRepository;


    public Test(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }


}
