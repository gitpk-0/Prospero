package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pk.wgu.capstone.data.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from Transaction t " +
            "where lower(t.description) like lower(concat('%', :searchTerm, '%'))")
    List<Transaction> search(@Param("searchTerm") String searchTerm);
}
