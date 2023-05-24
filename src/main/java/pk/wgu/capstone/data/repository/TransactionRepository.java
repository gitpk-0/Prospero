package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from transactions t " +
            "where lower(t.description) like lower(concat('%', :searchTerm, '%')) and t.userId = :user_id")
    List<Transaction> searchByUserIdAndDescription(@Param("user_id") Long userId, @Param("searchTerm") String searchTerm);

    @Query("select count(t.id) from transactions t where t.type = :type and t.userId = :user_id")
    Long countByTransactionType(@Param("user_id") Long userId, @Param("type") Type type);

    @Query("select t from transactions t " +
            "where t.userId = :user_id")
    List<Transaction> findAllByUserId(@Param("user_id") Long userId);
}
