package okky.team.chawchaw.user.language;

import lombok.Getter;
import okky.team.chawchaw.user.UserEntity;

import javax.persistence.*;

@Entity
@Table(name = "user_hope_language")
@Getter
public class UserHopeLanguageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_hope_language_id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    LanguageEntity hopeLanguage;

}
