package com.ruzhkov.springDmBot.service;

import com.ruzhkov.springDmBot.entity.City;
import com.ruzhkov.springDmBot.entity.Weather;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface ServiceBot {
    String deleteMyData(Message message);
    String getMyData(Message msg);
    void registerUser(Message msg);
    String startCommandInsult(long chatId);
    String commandHello(long chatId, String firstName);
    String commandStart(long chatId, String firstName);
    String commandInfo(long chatId, String firstName);
    String commandHelp(long chatId, String firstName);
    void setReplyMarkup1(SendMessage msg);

    String[] getWeather(Message message);
    String getWeatherByDate(Message message);

    List<String> printWeatherByInterval(String date1, String date2, Message message);

    List<Long> sendMessageFromAdmin(Message message);




    String changeCityForUser(Message message, City city, String userName);
}
