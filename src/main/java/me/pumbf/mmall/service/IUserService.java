package me.pumbf.mmall.service;

import me.pumbf.mmall.common.ServerResponse;
import me.pumbf.mmall.pojo.User;

public interface IUserService {
    ServerResponse<User>  login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String str, String type);
    ServerResponse<String> selectQuestion(String username);
    ServerResponse<String> checkAnswer(String username, String question, String answer);
    ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken);
    ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user);
    ServerResponse<User> updateInformation(User user);
}
