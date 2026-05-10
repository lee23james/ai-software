package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.jobplatform.dto.LoginRequestDTO;
import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.mapper.AccountUserMapper;
import com.example.jobplatform.service.AuthService;
import com.example.jobplatform.vo.AuthUserVO;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountUserMapper accountUserMapper;

    public AuthServiceImpl(AccountUserMapper accountUserMapper) {
        this.accountUserMapper = accountUserMapper;
    }

    @Override
    public AuthUserVO register(RegisterRequestDTO request) {
        String phone = trimToNull(request.getPhone());
        String email = trimToNull(request.getEmail());
        if (phone == null && email == null) {
            throw new IllegalArgumentException("手机号和邮箱不能同时为空");
        }
        if (phone != null && existsByPhone(phone)) {
            throw new IllegalArgumentException("手机号已注册");
        }
        if (email != null && existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已注册");
        }

        AccountUser user = new AccountUser();
        String username = trimToNull(request.getUsername());
        user.setUsername(username != null ? username : buildDefaultUsername(phone, email));
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(sha256(request.getPassword()));
        user.setRole("student");
        user.setStatus(1);
        accountUserMapper.insert(user);
        return toAuthUserVO(user);
    }

    @Override
    public AuthUserVO requireActiveUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        AccountUser user = accountUserMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("登录状态已失效，请重新登录。若曾清空或重建数据库，需重新注册账号。");
        }
        return toAuthUserVO(user);
    }

    @Override
    public AuthUserVO login(LoginRequestDTO request) {
        String identifier = request.getIdentifier().trim();
        AccountUser user = identifier.contains("@")
            ? accountUserMapper.selectOne(new LambdaQueryWrapper<AccountUser>().eq(AccountUser::getEmail, identifier))
            : accountUserMapper.selectOne(new LambdaQueryWrapper<AccountUser>().eq(AccountUser::getPhone, identifier));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("账号不存在或已禁用");
        }
        if (!sha256(request.getPassword()).equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        user.setLastLoginAt(LocalDateTime.now());
        accountUserMapper.updateById(user);
        return toAuthUserVO(user);
    }

    private boolean existsByPhone(String phone) {
        return accountUserMapper.selectCount(
            new LambdaQueryWrapper<AccountUser>().eq(AccountUser::getPhone, phone)
        ) > 0;
    }

    private boolean existsByEmail(String email) {
        return accountUserMapper.selectCount(
            new LambdaQueryWrapper<AccountUser>().eq(AccountUser::getEmail, email)
        ) > 0;
    }

    private AuthUserVO toAuthUserVO(AccountUser user) {
        return new AuthUserVO(
            user.getId(),
            user.getUsername(),
            user.getPhone(),
            user.getEmail(),
            user.getLastLoginAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("密码算法不可用", e);
        }
    }

    private String buildDefaultUsername(String phone, String email) {
        String base = phone != null ? "u" + phone : "u" + (email == null ? "user" : email.split("@")[0]);
        String candidate = base;
        int suffix = 1;
        while (accountUserMapper.selectCount(new QueryWrapper<AccountUser>().eq("username", candidate)) > 0) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }
}
