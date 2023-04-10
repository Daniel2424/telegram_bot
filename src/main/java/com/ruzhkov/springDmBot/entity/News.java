package com.ruzhkov.springDmBot.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "news")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String url;
    private String newsText;
    private String moreNewsText;
}
