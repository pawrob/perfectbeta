package pl.ftims.ias.your_climbing_gym.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "access_level", discriminatorType = DiscriminatorType.STRING, length = 16)
@Table(name = "access_level_table", schema = "public")
public  abstract class AccessLevelEntity extends AbstractEntity {

    private String accessLevel;
    private Boolean isActive;
    private UserEntity user;


    @Basic
    @Column(name = "access_level")
    public String getAccessLevel() {
        return accessLevel;
    }

    @Basic
    @Column(name = "is_active")
    public Boolean getActive() {
        return isActive;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    public UserEntity getUser() {
        return user;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

}