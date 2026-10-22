package pro.sky.telegrambot.schedulers;

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

    public NotificationTaskScheduler(
            NotificationTaskRepository repository
    ) {
        this.repository = repository;
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
    }
}
