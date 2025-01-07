package spc.cloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spc.cloud.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
