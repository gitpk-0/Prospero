package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pk.wgu.capstone.data.entity.Budget;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("select b from budgets b where b.userId = :user_id")
    List<pk.wgu.capstone.data.entity.Budget> getBudgetsByUserId(@Param("user_id") Long userId);

}
