package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wgu.capstone.data.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
