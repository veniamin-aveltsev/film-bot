package com.home.filmbot.botapi.handlers.callbackquery;

import com.home.filmbot.botapi.FilmTelegramBot;
import com.home.filmbot.service.FilmsDataService;
import com.home.filmbot.service.ReplyMessagesService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Component
public class FilmInfoQueryHandler implements ICallbackQueryHandler{
    private final FilmsDataService filmsDataService;
    private final ReplyMessagesService messagesService;
    private final FilmTelegramBot telegramBot;

    public FilmInfoQueryHandler(FilmsDataService filmsDataService, ReplyMessagesService messagesService,@Lazy FilmTelegramBot telegramBot) {
        this.filmsDataService = filmsDataService;
        this.messagesService = messagesService;
        this.telegramBot = telegramBot;
    }

    @Override
    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) {
        //Set variables
        String callbackId = callbackQuery.getId();
        long message_id = callbackQuery.getMessage().getMessageId();
        String call_data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        final int index; /*Integer.parseInt(callbackQuery.getData().split("\\|")[1]);*/
        String answer = messagesService.getReplyText("reply.movieResponse");

        EditMessageText callBackAnswer = new EditMessageText()
                .setChatId(chatId)
                .enableHtml(true)
                .setMessageId(Math.toIntExact(message_id))
                /*.enableWebPagePreview()*/;


        if(call_data.split("\\|")[1].equals("next")){
            index = Integer.parseInt(callbackQuery.getData().split("\\|")[2])+5;
            answer += filmsDataService.getFilmList(index);
            callBackAnswer.setText( answer);
            callBackAnswer.setReplyMarkup(filmsDataService.getInlineMessageButtonsForList(index));
            telegramBot.sendAnswerCallbackQuery(sendAnswerCallbackQuery("Updated",false, callbackId));
        }
        else if(call_data.split("\\|")[1].equals("back")){
            index = Integer.parseInt(callbackQuery.getData().split("\\|")[2])-5;
            answer += filmsDataService.getFilmList(index);
            callBackAnswer.setText(answer);
            callBackAnswer.setReplyMarkup(filmsDataService.getInlineMessageButtonsForList(index));
            telegramBot.sendAnswerCallbackQuery(sendAnswerCallbackQuery("Updated",false, callbackId));
        }
        else {
            index = Integer.parseInt(callbackQuery.getData().split("\\|")[1]);
            return  new  SendMessage()
                    .setChatId(chatId)
                    /*.enableWebPagePreview()*/
                    .enableHtml(true)
                    .setText( filmsDataService.getFilmShortInfo(index))
                    .setReplyMarkup(getInlineMessageButtonsForShortFilm(index));
        }
        return callBackAnswer;
    }

    public InlineKeyboardMarkup getInlineMessageButtonsForShortFilm(int index){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(new InlineKeyboardButton().setText("Подробнее").setCallbackData("INFO|detail|"+index));

        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(new InlineKeyboardButton().setText("Добавить в избранное").setCallbackData("SUBSCRIBE|" + index));




        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }



        private AnswerCallbackQuery sendAnswerCallbackQuery(String text, boolean alert, String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        return answerCallbackQuery;
    }

    @Override
    public CallbackQueryType getHandlerQueryType() {
        return CallbackQueryType.FILMS;
    }
}
