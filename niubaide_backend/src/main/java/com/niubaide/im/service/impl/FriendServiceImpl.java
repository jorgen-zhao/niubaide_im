package com.niubaide.im.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niubaide.im.mapper.FriendMapper;
import com.niubaide.im.pojo.bean.TbFriend;
import com.niubaide.im.pojo.bean.TbFriendReq;
import com.niubaide.im.pojo.bean.TbUser;
import com.niubaide.im.pojo.vo.User;
import com.niubaide.im.service.FriendService;
import com.niubaide.im.service.FriendServiceReq;
import com.niubaide.im.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fly
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, TbFriend> implements FriendService {

    @Autowired
    private FriendServiceReq reqService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Override
    public boolean sendRequest(TbFriendReq req) {
        isFriend(req);
        return reqService.saveOrUpdate(req);
    }

    @Override
    public List<User> getFriendReq(String userid) {
        List<TbFriendReq> reqs = reqService.list(Wrappers.lambdaQuery(TbFriendReq.class)
                .eq(TbFriendReq::getToUserid, userid)
                .eq(TbFriendReq::getStatus, 0));
        List<String> userIds = reqs.stream().map(req -> req.getFromUserid()).collect(Collectors.toList());
        List<TbUser> tbUsers = userService.listByIds(userIds);
        List<User> result = tbUsers.stream().map(tbUser -> {
            User user = new User();
            BeanUtils.copyProperties(tbUser, user);
            return user;
        }).collect(Collectors.toList());
        return result;
    }

    private void isFriend(TbFriendReq req) {
        // 1.用户不能添加自己为好友
        Assert.isTrue(!Objects.equals(req.getFromUserid(), req.getToUserid()), "不能添加自己为好友！");
        // 2.用户不能重复添加
        // 如果用户已经添加该好友，就不能再次添加
        int friendCount = friendService.count(Wrappers.lambdaQuery(TbFriend.class)
                .eq(TbFriend::getFriendsId, req.getToUserid())
                .eq(TbFriend::getUserid, req.getFromUserid()));
        Assert.isTrue(friendCount <= 0, "该用户已经是您的好友！请勿重复添加");
        // 3.判断是否已经提交好友申请，如果已经提交好友申请，直接抛出运行异常
        int reqCount = reqService.count(Wrappers.lambdaQuery(TbFriendReq.class)
                .eq(TbFriendReq::getFromUserid, req.getFromUserid())
                .eq(TbFriendReq::getToUserid, req.getToUserid())
                // 状态0 表示没有被处理
                .eq(TbFriendReq::getStatus, 0));
        Assert.isTrue(reqCount <= 0, "您已申请好友，请等待对方通过！");
    }

}

