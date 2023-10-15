package com.ruzhkov.springDmBot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InsultResponse {
    private int number;
    private String language;
    private String insult;
    private String created;
    private int shown;
    private String createdby;
    private int active;
    private String comment;



}
