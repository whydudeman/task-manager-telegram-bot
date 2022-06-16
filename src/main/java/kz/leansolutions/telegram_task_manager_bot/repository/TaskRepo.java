package kz.leansolutions.telegram_task_manager_bot.repository;

import kz.leansolutions.telegram_task_manager_bot.model.Status;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepo extends MongoRepository<Task, Long> {
    List<Task> findAllByExecutor(User executor);

    Page<Task> findAllByExecutor(User executor, Pageable pageable);

    @Query("{'deadline' : {$lte: ?0}, 'status' : {$eq: 'IN_PROGRESS'} }")
    List<Task> findAllByDeadlineAndStatus(LocalDateTime to);

    List<Task> findByStatusNotInAndDeadlineNotNullAndDeadlineBefore(List<Status> asList, LocalDateTime now);
}
