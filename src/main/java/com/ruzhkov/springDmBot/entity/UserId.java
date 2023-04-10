package com.ruzhkov.springDmBot.entity;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserId implements Serializable {

    private long chatId;

    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return chatId == userId.chatId && userName.equals(userId.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, userName);
    }
}
