package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from transactions t " +
            "where lower(t.description) like lower(concat('%', :searchTerm, '%')) and t.userId = :user_id")
    List<Transaction> searchByUserIdAndDescription(@Param("user_id") Long userId, @Param("searchTerm") String searchTerm);

    @Query("select count(t.id) from transactions t where t.type = :type and t.userId = :user_id")
    Long countByTransactionType(@Param("user_id") Long userId, @Param("type") Type type);

    @Query("select t from transactions t where t.userId = :user_id")
    List<Transaction> findAllByUserId(@Param("user_id") Long userId);

    @Query("select sum(t.amount) from transactions t where t.userId = :user_id and t.type = :type")
    BigDecimal sumAllTransactionsByType(@Param("user_id") Long userId, @Param("type") Type type);

    @Query("select c.name, SUM(t.amount) AS total_amount FROM transactions t " +
            "JOIN categories c ON t.category.id = c.id " +
            "where t.userId = :user_id and c.type = :type " +
            "GROUP BY c.name")
    List<Object[]> sumTransactionsByCategory(@Param("user_id") Long userId, @Param("type") Type type);

    @Query("select coalesce(SUM(t.amount), 0.0) from transactions t " +
            "where t.userId = :user_id and t.date >= :start and t.date <= :end " +
            "and t.type = :type")
    BigDecimal getSumExpensesInDateRange(
            @Param("start") Date start,
            @Param("end") Date end,
            @Param("type") Type type,
            @Param("user_id") Long userId);

    @Query("select c.name, SUM(t.amount) AS total_amount FROM transactions t " +
            "JOIN categories c ON t.category.id = c.id " +
            "where t.userId = :user_id and c.type = :type and t.date >= :start and t.date <= :end " +
            "GROUP BY c.name")
    List<Object[]> sumTransactionsInDateRangeByCategory(
            @Param("user_id") Long userId,
            @Param("type") Type type,
            @Param("start") Date start,
            @Param("end") Date end);

    @Query("select sum(t.amount) as income from transactions t where t.userId = :user_id and t.type = :type")
    BigDecimal getSumTransactionsByType(@Param("user_id") Long userId, @Param("type") Type type);

    @Query("select count(t.id) from transactions t where t.userId = :user_id")
    Integer getTransactionCount(@Param("user_id") Long userId);
}
