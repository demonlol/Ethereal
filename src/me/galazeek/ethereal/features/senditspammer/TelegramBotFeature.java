package me.galazeek.ethereal.features.senditspammer;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;

public class TelegramBotFeature {

    public static void main(String[] args) { new TelegramBotFeature(); }

    public TelegramBotFeature() {
        Runnable bot_thread = () -> {

            // Create your bot passing the token received from @BotFather
            TelegramBot bot = new TelegramBot("6304161088:AAHCgvDQI0Sujpgm2b1IW5T-wmwFb0PzQsc");

// Register for updates
            bot.setUpdatesListener(updates -> {
                // ... process updates
                // return id of last processed update or confirm them all
                for (Update update : updates) {
                    // Send messages
                    long chatId = update.message().chat().id();

                    //process stuff

                    SendResponse response = bot.execute(new SendMessage(chatId, "Hello!"));
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
// Create Exception Handler
            }, e -> {
                if (e.response() != null) {
                    // got bad response from telegram
                    e.response().errorCode();
                    e.response().description();
                } else {
                    // probably network error
                    e.printStackTrace();
                }
            });

        };

        new Thread(bot_thread).start();

        System.out.println("Started");

        while(true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
