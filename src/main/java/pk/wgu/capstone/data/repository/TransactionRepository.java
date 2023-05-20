package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from Transaction t " +
            "where lower(t.description) like lower(concat('%', :searchTerm, '%'))")
    List<Transaction> searchByDescription(@Param("searchTerm") String searchTerm);

    @Query("select count(t.id) from Transaction t where t.type = :type")
    Long countByTransactionType(@Param("type") Type type);
}
