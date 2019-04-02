package me.pumbf.mmall.service.impl;

import me.pumbf.mmall.common.Const;
import me.pumbf.mmall.common.ServerResponse;
import me.pumbf.mmall.common.TokenCache;
import me.pumbf.mmall.dao.UserMapper;
import me.pumbf.mmall.pojo.User;
import me.pumbf.mmall.service.IUserService;
import me.pumbf.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    public static final String TOKEN_PREFIX = "token_";

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //  密码登陆MD5
        password = MD5Util.encode(password);
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    public ServerResponse<String> register(User user) {
        // 检查用户名是否已存在
        ServerResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        // 检查email是否已存在
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        // md5加密
        user.setPassword(MD5Util.encode(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if(resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNoneBlank(type)) {
            // 开始校验
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0)
                    return ServerResponse.createByErrorMessage("用户名已存在");
            } else if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0)
                    return ServerResponse.createByErrorMessage("Email已存在");
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess())
            return ServerResponse.createByErrorMessage("该用户不存在");
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            // 问题答案正确
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token不存在");
        }
        String token = TokenCache.getKey(TOKEN_PREFIX + username);
        if (token == null) {
            return ServerResponse.createByErrorMessage("token失效");
        }
        if(StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.encode(newPassword);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }

        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");

    }

    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
        // 防止横向越权
        int resultCount = userMapper.checkPassword(MD5Util.encode(oldPassword), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("原密码错误");
        }
        user.setPassword(MD5Util.encode(newPassword));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        return resultCount > 0 ? ServerResponse.createBySuccessMessage("密码修改成功")
                                : ServerResponse.createByErrorMessage("密码修改失败");
    }

    public ServerResponse<User> updateInformation(User user) {
        // username不能被更新
        // email 检验是否已经存在

        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("该邮箱已经存在");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        resultCount = userMapper.updateByPrimaryKeySelective(updateUser);

        return resultCount > 0 ? ServerResponse.createBySuccess("个人信息更新成功", updateUser)
                : ServerResponse.createByErrorMessage("个人信息更新失败");
    }
}
