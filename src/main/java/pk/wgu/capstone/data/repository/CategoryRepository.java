package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wgu.capstone.data.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
