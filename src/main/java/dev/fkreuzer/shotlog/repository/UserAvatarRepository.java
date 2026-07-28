package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {
}
