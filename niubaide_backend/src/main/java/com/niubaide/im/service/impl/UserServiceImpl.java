package com.niubaide.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niubaide.im.mapper.UserMapper;
import com.niubaide.im.pojo.po.TbUser;
import com.niubaide.im.pojo.vo.User;
import com.niubaide.im.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<TbUser> getAllUser() {
        return userMapper.selectList(null);
    }

    @Override
    public User login(String userName, String password) {
            String md5Pwd = DigestUtils.md5DigestAsHex(password.getBytes());
            QueryWrapper<TbUser> tb = new QueryWrapper<>();
            // 用加密后的密码和用户名查询
            tb.eq("username", userName).eq("password", md5Pwd);
            TbUser tbUser = userMapper.selectOne(tb);
            if (tbUser != null){
                User user = new User();
                BeanUtils.copyProperties(tbUser,user);
                return user;
            }
            throw new NullPointerException("没有该用户，请检查");
    }

    @Override
    public int register(TbUser user) {
        QueryWrapper<TbUser> tb = new QueryWrapper<>();
        tb.eq("username",user.getUsername());
        TbUser register = userMapper.selectOne(tb);
        if (register != null){
            new RuntimeException("该用户已存在！");
        }
        return userMapper.insert(user);
    }
}
