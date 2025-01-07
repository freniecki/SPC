package spc.cloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spc.cloud.entity.UserFile;

import java.util.UUID;

public interface FileRepository extends JpaRepository<UserFile, UUID> {

    @Query("select uf from UserFile uf where uf.s3Key = :s3Key")
    UserFile findUserFileByS3Key(@Param("s3Key") String s3Key);
}
