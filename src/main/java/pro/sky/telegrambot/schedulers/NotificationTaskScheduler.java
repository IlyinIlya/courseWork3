package pro.sky.telegrambot.schedulers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.notifications.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskScheduler {

    private final Logger logger =
            LoggerFactory.getLogger(NotificationTaskScheduler.class);

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationTaskScheduler(
            NotificationTaskRepository repository,
            TelegramBot telegramBot
    ) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 * * * * *")
    public void findTasksForCurrentMinute() {
        LocalDateTime currentMinute =
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> tasks =
                repository.findAllByNotificationTime(currentMinute);

        logger.info(
                "Время проверки: {}, найдено напоминаний: {}",
                currentMinute,
                tasks.size()
        );

        for (NotificationTask task : tasks) {
            SendMessage message = new SendMessage(
                    task.getChatId(),
                    task.getMessage()
            );

            SendResponse response = telegramBot.execute(message);

            if (response.isOk()) {
                logger.info(
                        "Уведомление отправлено: id={}, chatId={}",
                        task.getId(),
                        task.getChatId()
                );
            } else {
                logger.error(
                        "Не удалось отправить уведомление: id={}, chatId={}, errorCode={}",
                        task.getId(),
                        task.getChatId(),
                        response.errorCode()
                );
            }
        }
    }
}
