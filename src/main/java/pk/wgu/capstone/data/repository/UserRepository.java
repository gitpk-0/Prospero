package pk.wgu.capstone.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pk.wgu.capstone.data.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select count(u.id) from User u where u.email = :email")
    int findUserByEmail(@Param("email") String email);
}
