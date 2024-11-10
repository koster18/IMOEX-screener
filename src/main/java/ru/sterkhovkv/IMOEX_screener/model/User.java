package ru.sterkhovkv.IMOEX_screener.model;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Min;

@Entity
@Table(name = "usernames")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer Id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "money")
    @Min(value = 0)
    private int money;
}
