package service;

import domain.User;

public class AtmService {

    public String login(User user) {
        if ("zzt".equals(user.getName()) && 123 == user.getPass()) {
            return "success";
        }
        return "defeat";
    }

}