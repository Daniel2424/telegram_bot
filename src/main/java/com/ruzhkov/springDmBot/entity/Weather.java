package com.ruzhkov.springDmBot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "weather")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Weather {
    //  утром, от 3 до 8° | //yastatic.net/weather/i/icons/funky/dark/bkn_d.svg | Облачно с прояснениями | 752 | 84% | 3,5 | +2
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private City city;
    private String date;
    private String temperature;
    private String urlPicture;
    private String description;
    private String pressure;
    private String humidity;
    private String wind;
    private String feelsLike;

    @Override
    public String toString() {
        return
                temperature.split(",")[0].toUpperCase() + ":\n" +
                        temperature.split(",")[1] +", " + description +
                        ", Ощущается как = " + feelsLike + '°' +
                        "\nДавление = " + pressure + " мм рт. ст." +
                        ", Влажность = " + humidity +
                        ", Ветер = " + wind + " м/с";
    }
}
