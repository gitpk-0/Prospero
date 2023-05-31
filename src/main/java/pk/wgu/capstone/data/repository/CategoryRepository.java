package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pk.wgu.capstone.data.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c from categories c where c.name = :c_name")
    Category findCategoryByName(@Param("c_name") String categoryName);

    @Modifying
    @Query("update categories c set c.userIdsCsv = c.userIdsCsv || :user_id " + // || = concat
            "where c.id = :c_id")
    void updateCustomCategoryUserIds(@Param("c_id") Long categoryId, @Param("user_id") String userId);
}
