package com.ruzhkov.springDmBot.bot_shedule;

import com.ruzhkov.springDmBot.entity.City;
import com.ruzhkov.springDmBot.entity.News;
import com.ruzhkov.springDmBot.entity.Weather;
import com.ruzhkov.springDmBot.repositories.NewsRepository;
import com.ruzhkov.springDmBot.repositories.WeatherRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
public class Task_Parse {
    private final WeatherRepository weatherRepository;
    private final NewsRepository newsRepository;

    @Autowired
    public Task_Parse(WeatherRepository weatherRepository, NewsRepository newsRepository) {
        this.weatherRepository = weatherRepository;
        this.newsRepository = newsRepository;
    }

    //@Scheduled(cron = "* * * * * *")
    @Scheduled(fixedDelay = 600000)
    public void parseNewWeather() {
        System.out.println("starting parse weather");
        City[] values = City.values();


        try {
            weatherRepository.deleteAll();
            log.info("deleted all rows from the weather table");
            for (int i = 0; i < values.length; i++) {


                String url2 = values[i].getUrl();
                Document page = Jsoup.connect(url2).timeout(5000).get();
                Elements weatherElements = page.getElementsByClass("weather-table__row");
                System.out.println(weatherElements.size() + " - " + values[i].name());

                if (weatherElements.size() != 40) {
                    log.info("Не получилось спарсить погоду в городе: " + values[i].name() + ". Еще раз пробуем.");
                    dopParse(values[i], 1);
                }


                // Получаем текущую дату и время
                Timestamp curDate = new Timestamp(System.currentTimeMillis());
                // Получаем сегодняшнюю дату в формате "2022-05-24"
                LocalDate today = curDate.toLocalDateTime().toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


                int countDay = 0;

                int dop = 0;
                Weather weather;
                for (Element e : weatherElements) {
                    dop++;
                    weather = new Weather();
                    weather.setCity(values[i]);
                    weather.setDate(today.plusDays(countDay).format(formatter));
                    weather.setTemperature(e.getElementsByClass("a11y-hidden").get(0).text());
                    weather.setUrlPicture(e.getElementsByClass("icon_color_dark").attr("src"));
                    weather.setDescription(e.getElementsByClass("weather-table__body-cell_type_condition").text());
                    weather.setPressure(e.getElementsByClass("weather-table__body-cell_type_air-pressure").text());
                    weather.setHumidity(e.getElementsByClass("weather-table__body-cell_type_humidity").text());
                    weather.setWind(e.getElementsByClass("wind-speed").text());
                    weather.setFeelsLike(Objects.requireNonNull(e.getElementsByClass("temp__value_with-unit").last()).text());
                    weatherRepository.save(weather);

                    if (dop % 4 == 0) countDay++;
                }
                log.info("Данные о погоде в городе: "+values[i].name() + " успешно записаны в бд");

            }
        } catch (IOException e) {
            log.error("ParseWeather error: " + e.getMessage());
        }

    }


    //@Scheduled(cron = "0 0 0 * * *")
    public void parseAllNews() {
        System.out.println("starting parse news");

        int pageNumber = 1;
        for (int i = 1; i <= 557; i++) {


            String pageN = "page";

            String url = "https://habr.com/ru/flows/develop/news/" + pageN + pageNumber;
            pageNumber++;


            try {
                Document page = Jsoup.connect(url).get();
                Elements news = page.getElementsByClass("tm-title__link");
                System.out.println(news.size() + " - size");
                int count = 0;

                for (Element element : news) {

                    News cur_news = new News();
                    cur_news.setNewsText(element.text());
                    cur_news.setUrl("https://habr.com" + element.attr("href"));
                    newsRepository.save(cur_news);
                    //sendMessage(message.getChatId(), cur_news.getNewsText() + "\n" + cur_news.getUrl());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private void dopParse(City city, int count){
        if(count > 2) return;
        System.out.println("starting parse dop weather");

        try {
                String url2 = city.getUrl();
                Document page = Jsoup.connect(url2).get();
                Elements weatherElements = page.getElementsByClass("weather-table__row");
                if (weatherElements.size() != 40) {
                    log.info("Не получилось спарсить погоду в городе 2 или более раз: " + city.name());
                    dopParse(city, count + 1);
                    return;
                }


                // Получаем текущую дату и время
                Timestamp curDate = new Timestamp(System.currentTimeMillis());
                // Получаем сегодняшнюю дату в формате "2022-05-24"
                LocalDate today = curDate.toLocalDateTime().toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


                int countDay = 0;

                int dop = 0;
                Weather weather;
                System.out.println(weatherElements.size() + " - " + city.name());

                for (Element e : weatherElements) {
                    dop++;
                    weather = new Weather();
                    weather.setCity(city);
                    weather.setDate(today.plusDays(countDay).format(formatter));
                    weather.setTemperature(e.getElementsByClass("a11y-hidden").get(0).text());
                    weather.setUrlPicture(e.getElementsByClass("icon_color_dark").attr("src"));
                    weather.setDescription(e.getElementsByClass("weather-table__body-cell_type_condition").text());
                    weather.setPressure(e.getElementsByClass("weather-table__body-cell_type_air-pressure").text());
                    weather.setHumidity(e.getElementsByClass("weather-table__body-cell_type_humidity").text());
                    weather.setWind(e.getElementsByClass("wind-speed").text());
                    weather.setFeelsLike(Objects.requireNonNull(e.getElementsByClass("temp__value_with-unit").last()).text());
                    weatherRepository.save(weather);

                    if (dop % 4 == 0) countDay++;
                }
                log.info("Данные о погоде в городе: "+city.name() + " успешно записаны в бд");


        } catch (IOException e) {
            log.error("ParseWeather error: " + e.getMessage());
        }
    }
}
