package spc.cloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spc.cloud.entity.UserFile;

import java.util.UUID;

public interface FileRepository extends JpaRepository<UserFile, UUID> {
}
