package com.ruzhkov.springDmBot.entity;

public enum City {
    MOSCOW("https://yandex.ru/weather/ru-RU/details/10-day-weather?lat=55.75581741&lon=37.61764526&via=ms"),
    ST_PETERSBURG("https://yandex.ru/weather/ru-RU/details/10-day-weather?lat=59.938951&lon=30.315635&via=ms"),
    NOVOSIBIRSK("https://yandex.ru/pogoda/details/10-day-weather?lat=55.03020096&lon=82.92043304&via=ms"),
    EKATERINBURG("https://yandex.ru/pogoda/details/10-day-weather?lat=56.8380127&lon=60.59747314&via=ms"),
    KAZAN("https://yandex.ru/pogoda/details/10-day-weather?lat=55.79612732&lon=49.10641479&via=ms"),
    NIZHNY_NOVGOROD("https://yandex.ru/pogoda/details/10-day-weather?lat=56.32679749&lon=44.00651932&via=ms"),
    SAMARA("https://yandex.ru/pogoda/details/10-day-weather?lat=53.19587708&lon=50.10020065&via=ms"),
    ROSTOV("https://yandex.ru/pogoda/details/10-day-weather?lat=47.22208023&lon=39.72035599&via=ms"),
    UFA("https://yandex.ru/pogoda/details/10-day-weather?lat=54.73514938&lon=55.9587326&via=ms"),
    ZHELEZNOGORSK("https://yandex.ru/pogoda/details/10-day-weather?lat=52.33920288&lon=35.35087204&via=ms");

    private final String url;
    City(String url){
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
