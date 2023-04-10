package com.ruzhkov.springDmBot.bot;


import com.ruzhkov.springDmBot.config.BotConfig;
import com.ruzhkov.springDmBot.entity.City;
import com.ruzhkov.springDmBot.entity.Weather;
import com.ruzhkov.springDmBot.service.ServiceBotImp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final ServiceBotImp serviceBot;

    @Autowired
    public TelegramBot(BotConfig config, ServiceBotImp serviceBot) {
        this.config = config;
        this.serviceBot = serviceBot;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText().toLowerCase();

            System.out.println(messageText);
            long chatId = update.getMessage().getChatId();

            if(messageText.matches("\\d{4}-\\d{2}-\\d{2}")){
                sendMessage(update.getMessage().getChatId(), serviceBot.getWeatherByDate(update.getMessage()));
            }else if(messageText.matches("\\d{4}-\\d{2}-\\d{2} - \\d{4}-\\d{2}-\\d{2}")) {
                String[] dates = messageText.split(" - ");
                serviceBot.printWeatherByInterval(dates[0], dates[1], update.getMessage()).forEach(x -> sendMessage(chatId, x));
            }else if(messageText.matches("/send .+")){
                serviceBot.sendMessageFromAdmin(update.getMessage()).forEach(x -> sendMessage(x, update.getMessage().getText().substring(6)));
            }

            else {
                switch (messageText) {
                    case "/start":
                        serviceBot.registerUser(update.getMessage());
                        sendMessage(update.getMessage().getChatId(), serviceBot.commandStart(chatId, update.getMessage().getFrom().getFirstName()));
                        break;
                    case "/info":
                        sendMessage(update.getMessage().getChatId(), serviceBot.commandInfo(chatId, update.getMessage().getFrom().getFirstName()));
                        break;
                    case "/help":
                        sendMessage(update.getMessage().getChatId(), serviceBot.commandHelp(chatId, update.getMessage().getFrom().getFirstName()));
                        break;
                    case "/my_data":
                        sendMessage(update.getMessage().getChatId(), serviceBot.getMyData(update.getMessage()));
                        break;
                    case "/delete_my_data":
                        sendMessage(update.getMessage().getChatId(), serviceBot.deleteMyData(update.getMessage()));
                        break;
                    case "/register":
                        sendMessageAllCity(update.getMessage());
                    case "/news":
                        //testing
                        break;
                    case "/photo":
                        //sendPhotoMessage(chatId);
                        break;
                    case "погода на 10 дней":
                        String[] weather = serviceBot.getWeather(update.getMessage());
                        Arrays.stream(weather).forEach(answer -> sendMessage(chatId, answer));
                        break;
                    default:
                        sendMessage(chatId, "Такой команды нет\nНапишите /info, чтобы посмотреть существующие команды");


                }
            }
        }else if(update.hasCallbackQuery()){
            String callBackData = update.getCallbackQuery().getData();
            String[] arr = callBackData.split("[|]");
            String cityName = arr[0];
            String userName = arr[1];
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            City city = City.valueOf(cityName);
            String textToChange = serviceBot.changeCityForUser(update.getCallbackQuery().getMessage(), city, userName);

            EditMessageText message = new EditMessageText();
            message.setChatId(String.valueOf(chatId));
            message.setMessageId((int) messageId);
            message.setText(textToChange);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }



    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        //setReplyMarkup1(message);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
    private void sendMessageAllCity(Message msg){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(msg.getChatId()));
        message.setText("   Выберите свой город:");
        {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
            List<InlineKeyboardButton> rowInline4 = new ArrayList<>();

            var button1 = new InlineKeyboardButton("Москва");
            button1.setCallbackData(City.MOSCOW.name() + "|" + msg.getFrom().getUserName());

            var button2 = new InlineKeyboardButton("Санкт-Петербург");
            button2.setCallbackData(City.ST_PETERSBURG.name() + "|" + msg.getFrom().getUserName());

            var button3 = new InlineKeyboardButton("Екатеринбург");
            button3.setCallbackData(City.EKATERINBURG.name() + "|" + msg.getFrom().getUserName());

            var button4 = new InlineKeyboardButton("Казань");
            button4.setCallbackData(City.KAZAN.name() + "|" + msg.getFrom().getUserName());

            var button5 = new InlineKeyboardButton("Нижний Новгород");
            button5.setCallbackData(City.NIZHNY_NOVGOROD.name() + "|" + msg.getFrom().getUserName());

            var button6 = new InlineKeyboardButton("Самара");
            button6.setCallbackData(City.SAMARA.name() + "|" + msg.getFrom().getUserName());

            var button7 = new InlineKeyboardButton("Ростов-на-Дону");
            button7.setCallbackData(City.ROSTOV.name() + "|" + msg.getFrom().getUserName());

            var button8 = new InlineKeyboardButton("Железногорск");
            button8.setCallbackData(City.ZHELEZNOGORSK.name() + "|" + msg.getFrom().getUserName());

            var button9 = new InlineKeyboardButton("Уфа");
            button9.setCallbackData(City.UFA.name() + "|" + msg.getFrom().getUserName());

            var button10 = new InlineKeyboardButton("Новосибирск");
            button10.setCallbackData(City.NOVOSIBIRSK.name() + "|" + msg.getFrom().getUserName());

            rowInline1.add(button1);
            rowInline1.add(button2);
            rowInline1.add(button3);
            rowInline2.add(button4);
            rowInline2.add(button5);

            rowInline2.add(button6);
            rowInline3.add(button7);
            rowInline3.add(button8);
            rowInline3.add(button9);
            rowInline4.add(button10);
            rowsInline.add(rowInline1);
            rowsInline.add(rowInline2);
            rowsInline.add(rowInline3);
            rowsInline.add(rowInline4);


            markup.setKeyboard(rowsInline);

            message.setReplyMarkup(markup);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }


    }

    private void sendPhotoMessage(long chatId)  {
        try {
            URL imageUrl = new URL("https://yastatic.net/weather/i/icons/funky/dark/ovc_ra.svg");
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream inputStream = connection.getInputStream();
            BufferedImage image = ImageIO.read(inputStream);

// преобразуем BufferedImage в InputFile для отправки в Telegram
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "image.jpg");
            SendPhoto msg = new SendPhoto();

            msg.setChatId(String.valueOf(chatId));

            msg.setPhoto(inputFile);

            msg.setCaption("first photo");
            execute(msg);
        }catch (Exception e){
            e.printStackTrace();
        }




    }
    private void getPhoto(long chatId, String ImgUrl) {
        SendPhoto sPhoto = new SendPhoto();
        try {
            URL url = null;
            url = new URL(ImgUrl);
            InputFile photo = new InputFile(String.valueOf(url));
            sPhoto.setPhoto(photo);
            sPhoto.setChatId(String.valueOf(chatId));
            sPhoto.setCaption("test");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            execute(sPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
