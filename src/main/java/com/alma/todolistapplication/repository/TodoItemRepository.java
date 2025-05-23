package com.alma.todolistapplication.repository;

import com.alma.todolistapplication.model.TodoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // Import if used in method signatures like findByUserIdAndDueDateBetween
import java.util.List;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    // --- Finders by User ID with different ordering ---
    List<TodoItem> findByUserIdOrderByCreatedAtDesc(Long userId); // Newest create date first
    List<TodoItem> findByUserIdOrderByCreatedAtAsc(Long userId);  // Oldest create date first
    List<TodoItem> findByUserIdOrderByDueDateAsc(Long userId);    // Earliest due date first
    List<TodoItem> findByUserIdOrderByDueDateDesc(Long userId);   // Latest due date first

    // --- Finders by User ID and Completion Status (with ordering) ---
    List<TodoItem> findByUserIdAndCompleted(Long userId, boolean completed); // Default ordering (usually by ID)
    List<TodoItem> findByUserIdAndCompletedOrderByCreatedAtDesc(Long userId, boolean completed);
    List<TodoItem> findByUserIdAndCompletedOrderByCreatedAtAsc(Long userId, boolean completed);
    List<TodoItem> findByUserIdAndCompletedOrderByDueDateAsc(Long userId, boolean completed);
    List<TodoItem> findByUserIdAndCompletedOrderByDueDateDesc(Long userId, boolean completed);

    // --- Pagination Support ---
    Page<TodoItem> findByUserId(Long userId, Pageable pageable);
    Page<TodoItem> findByUserIdAndCompleted(Long userId, boolean completed, Pageable pageable);
    // Example with dueDate filtering and pagination:
    // Page<TodoItem> findByUserIdAndDueDateBefore(Long userId, LocalDateTime dateTime, Pageable pageable);

    // --- Counting ---
    long countByUserId(Long userId);
    long countByUserIdAndCompleted(Long userId, boolean completed);

    // --- Searching ---
    // Find by description containing a keyword for a specific user, ignoring case, ordered by creation date
    List<TodoItem> findByUserIdAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String keyword);
    // Find by description containing a keyword for a specific user, ignoring case, with pagination
    Page<TodoItem> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String keyword, Pageable pageable);


    // --- Date-based queries ---
    // Find tasks for a user due within a specific date range
    List<TodoItem> findByUserIdAndDueDateBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    // Find tasks for a user due before a certain date
    List<TodoItem> findByUserIdAndDueDateBefore(Long userId, LocalDateTime dateTime);
    // Find tasks for a user due after a certain date
    List<TodoItem> findByUserIdAndDueDateAfter(Long userId, LocalDateTime dateTime);
    // Find tasks for a user that are overdue (dueDate is in the past and not completed)
    List<TodoItem> findByUserIdAndDueDateBeforeAndCompletedIsFalse(Long userId, LocalDateTime currentDateTime);


    // --- Bulk operations ---
    // Mark all tasks for a user as completed
    @Modifying
    @Query("UPDATE TodoItem t SET t.completed = true, t.completedAt = CURRENT_TIMESTAMP WHERE t.user.id = :userId AND t.completed = false")
    int markAllAsCompletedForUser(Long userId);

    // Delete all completed tasks for a user
    @Modifying
    @Query("DELETE FROM TodoItem t WHERE t.user.id = :userId AND t.completed = true")
    int deleteAllCompletedForUser(Long userId);

    // Delete all tasks for a specific user (use with extreme caution - JpaRepository.deleteAllByUserId could also work if User entity is correctly mapped)
    // This is redundant if cascade delete is set up on the User entity's OneToMany relationship to TodoItem.
    // However, if you need to perform it as a separate operation from the repository:
    // @Modifying
    // @Query("DELETE FROM TodoItem t WHERE t.user.id = :userId")
    // int deleteAllByUserId(Long userId); // Or Spring Data might derive: void deleteAllByUserId(Long userId);
}