package com.ruzhkov.springDmBot.service;

import com.ruzhkov.springDmBot.entity.City;
import com.ruzhkov.springDmBot.entity.User;
import com.ruzhkov.springDmBot.entity.UserId;
import com.ruzhkov.springDmBot.entity.Weather;
import com.ruzhkov.springDmBot.model.InsultResponse;
import com.ruzhkov.springDmBot.repositories.NewsRepository;
import com.ruzhkov.springDmBot.repositories.UserRepository;
import com.ruzhkov.springDmBot.repositories.WeatherRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.LoggerNameAwareMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ServiceBotImp implements ServiceBot{

    private final UserRepository userRepository;



    private final WeatherRepository weatherRepository;

    private final NewsRepository newsRepository;
    @Autowired
    public ServiceBotImp(UserRepository userRepository, WeatherRepository weatherRepository, NewsRepository newsRepository) {
        this.userRepository = userRepository;
        this.weatherRepository = weatherRepository;
        this.newsRepository = newsRepository;
    }

    @Override
    public String[] getWeather(Message message) {
        String[] strings = new String[10];
        List<Weather> all = weatherRepository.findAll();
        int count = 0;
        StringBuilder answer = new StringBuilder();
        String date = "";
        int i = 0;
        for(Weather w: all){
            count++;
            date = w.getDate();
            answer.append(w).append("\n\n");

            if(count % 4 == 0) {
                strings[i++] = date + "\n" + answer;
                answer = new StringBuilder();
            }
        }

        return strings;
    }

    @Override
    public String getWeatherByDate(Message message) {
        String date = message.getText();
        User user = userRepository.findById(new UserId(message.getChatId(), message.getFrom().getUserName())).get();

        if(!weatherRepository.findAllByDateAndCity(date, user.getCity()).isEmpty()){
            List<Weather> allByDate = weatherRepository.findAllByDateAndCity(date, user.getCity());
            StringBuilder answer = new StringBuilder();
            for(Weather w: allByDate){
                answer.append(w).append("\n\n");
            }
            return answer.toString();
        }else{
            return "Дата введена некорректно или у нас нет информации об этой дате";
        }
    }


    public String deleteMyData(Message message) {
        String answer;
        UserId userId = new UserId(message.getChatId(), message.getFrom().getUserName());
        if(userRepository.findById(userId).isPresent()){
            userRepository.deleteById(userId);
            log.info(message.getFrom().getUserName() + " removed from bd");
            return message.getFrom().getFirstName() + ", ваши данные были успешно удалены из базы данных";
        }else{
            return message.getFrom().getFirstName() + ", ваши данные не хранятся в базе данных";
        }
    }

    public String getMyData(Message msg){
        UserId userId = new UserId(msg.getChatId(), msg.getFrom().getUserName());
        Optional<User> userOptional = userRepository.findById(userId);

        if(userOptional.isPresent()){
            User user = userOptional.get();
            String answer = "Имя: " + user.getFirstName() +
                    "\nФамилия: " + user.getLastName() +
                    "\nUserName: @" + user.getUserId().getUserName() +
                    "\nДата регистрации в данном телеграм боте:\n" + user.getRegisteredAt().toString() +
                    "\nГород: " + user.getCity().name() +
                    "\nДля смены города введите /registerCity" +
                    "\nДля удаления информации о себе введите /delete_my_data ";

            log.info(msg.getFrom().getFirstName() + " ( @"+ msg.getFrom().getUserName() +
                    " )" + " - details have been removed from the database" +
                    msg.getFrom().getFirstName());
            return answer;
        }else{
            return msg.getFrom().getFirstName() + ", вы не зарегестрировались в данном телеграм боте\n" +
                    "Напишите команду /start, чтобы ваши данные были занесены в базу данных!";
        }
    }


    @Override
    public List<String> printWeatherByInterval(String date1, String date2, Message message) {
        User user = userRepository.findById(new UserId(message.getChatId(), message.getFrom().getUserName())).get();

        List<Weather> allByDateBetween = weatherRepository.findAllByDateBetweenAndCity(date1, date2, user.getCity());
        List<String> strings = new ArrayList<>();
        int count = 0;
        StringBuilder answer = new StringBuilder();
        String date = "";
        for(Weather w: allByDateBetween){
            count++;
            date = w.getDate();
            answer.append(w).append("\n\n");

            if(count % 4 == 0) {
                strings.add(date + "\n" + answer) ;
                answer = new StringBuilder();
            }
        }

        return strings;

    }

    public void registerUser(Message msg) {
        UserId userId = new UserId(msg.getChatId(), msg.getFrom().getUserName());
        if(userRepository.findById(userId).isEmpty()){

            User user = new User(userId,
                    msg.getFrom().getFirstName(),
                    msg.getFrom().getLastName(),
                    new Timestamp(System.currentTimeMillis()),
                    City.MOSCOW
            );
            userRepository.save(user);

            log.info("User saved: " + "user");
        }
    }

    public String startCommandInsult(long chatId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://evilinsult.com/generate_insult.php?lang=ru&type=json";
        InsultResponse response = restTemplate.getForObject(url, InsultResponse.class);
        log.info("Replied to chat: " + chatId);
        if(response!=null) {
            return response.getInsult();
        }else{
            return "Что-то пошло не так, извините\nПопробуйте еще раз";
        }


    }



    public String commandHello(long chatId, String firstName) {
        String answer = "Привет, " + firstName;
        log.info("Replied to user: " + firstName);
        return answer;
    }

    public String commandStart(long chatId, String firstName){

        String answer = EmojiParser.parseToUnicode("Привет, " + firstName + ", я телеграм бот для погоды.\n" +
                "Мой создатель: Ружков Даниил:purple_heart:\n" +
                "Для начала выберите свой город из списка" +
                "Все команды:\n\"" +
                "/info\n" +
                "/help\n" +
                "/Погода сегодня\n");
        log.info("Replied to user: " + firstName);
        return answer;
    }
    public String commandInfo(long chatId, String firstName){
        String answerInfo = firstName + ", нет инфы";
        log.info("Replied to user: " + firstName);
        return answerInfo;
    }
    public String commandHelp(long chatId, String firstName){
        String answer = firstName + ", если у вас возникли какие-то проблемы, напишет на автору бота:" +
                "\nemail - d.ruzhkov7@yandex.ru" +
                "\ntelegram: @Daniil_Ruzhkov";

        log.info("Replied to user: " + firstName);
        return answer;
    }
    public void setReplyMarkup1(SendMessage msg){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> list = new ArrayList<>();
        {
            KeyboardRow row = new KeyboardRow();
            row.add("/my_data");
            row.add("/delete_my_data");
            list.add(row);
            row = new KeyboardRow();
            row.add("/insult");
            row.add("/start");
            row.add("/hello");
            list.add(row);
        }
        keyboardMarkup.setKeyboard(list);
        msg.setReplyMarkup(keyboardMarkup);
    }
    @Override
    public List<Long> sendMessageFromAdmin(Message message) {
        if(message.getChatId() == 1390638319 && message.getFrom().getUserName().equals("Daniil_Ruzhkov")){
            List<User> list = userRepository.findAll();
            List<Long> listOfId = new ArrayList<>();

            list.forEach(x -> listOfId.add(x.getUserId().getChatId()));
            return listOfId;
        }

        return new ArrayList<>();


    }


    @Override
    public String changeCityForUser(Message message, City city, String userName) {


        UserId userId = new UserId(message.getChatId(), userName);


        Optional<User> userOptional = userRepository.findById(userId);

        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setCity(city);
            userRepository.save(user);
            return "Вы успешно изменили свой город\nТеперь все запросы с погодой будут выдавать температуру в городе "+ city.name();
        }else{
            return "Вы не зарегестрированы в телеграм боте\nВведите /start или /register";
        }
    }
}
