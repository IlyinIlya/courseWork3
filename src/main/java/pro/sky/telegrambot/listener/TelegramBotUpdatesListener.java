package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import pro.sky.telegrambot.notifications.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    private final Pattern pattern = Pattern.compile(
            "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)"
    );

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null
                    && "/start".equals(update.message().text())) {
                telegramBot.execute(new SendMessage(
                        update.message().chat().id(),
                        "Добро пожаловать в Хогвартс! Я Ваша Сова (бот-напоминатель)!"
                ));
                return;
            }

            if (update.message() == null || update.message().text() == null) {
                return;
            }

            String text = update.message().text();
            Long chatId = update.message().chat().id();

            Matcher matcher = pattern.matcher(text);

            if (matcher.matches()) {
                NotificationTask task = new NotificationTask();
                task.setChatId(chatId);
                task.setNotificationTime(
                        LocalDateTime.parse(matcher.group(1), formatter)
                );
                task.setMessage(matcher.group(3));

                notificationTaskRepository.save(task);

                telegramBot.execute(new SendMessage(
                        chatId,
                        "Напоминание - сохранено!"
                ));
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
