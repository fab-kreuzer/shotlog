package dev.fkreuzer.shotlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Profile picture stored in its own table so the (potentially multi-MB) blob is
 * never loaded with the {@link UserAccount} entity — which rides along on the
 * security principal in every session. Keyed by the owning user's id.
 */
@Entity
@Table(name = "user_avatars")
@Getter
@Setter
public class UserAvatar {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    protected UserAvatar() {}

    public UserAvatar(Long userId, byte[] data, String contentType) {
        this.userId = userId;
        this.data = data;
        this.contentType = contentType;
    }
}
