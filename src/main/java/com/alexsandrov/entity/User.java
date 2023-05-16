package com.alexsandrov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;




@NamedEntityGraph(
        name = "withCompany",
        attributeNodes = {
                @NamedAttributeNode("userChats")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "username")
@ToString(exclude = {"company", "profile", "userChats"})
@Builder
@Entity
@Table(name = "users", schema = "public")
@Access(AccessType.FIELD)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @AttributeOverride(name = "birthDate", column = @Column(name = "birth_date"))
    private PersonalInfo personalInfo;
    @Column(unique = true)
    private String username;
    @Enumerated(EnumType.STRING)
    private Roles roles;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    // Не обязательно, так как для формирования названия используется название таблицы company и имя столбца
    // на который ссылаемся, в нашем случае id. Получается company_id
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private Profile profile;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<UserChat> userChats = new HashSet<>();

}
