package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.UserInfo;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserInfoService implements TableService{
    private final UserInfoRepository userInfoRepository;
    @Override
    public List<UserInfo> getList() {
        return userInfoRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        Long count = userInfoRepository.countById(id);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Không tìm thấy ID: " + id);
        }
        userInfoRepository.deleteById(id);
    }

    @Override
    public Optional get(UUID id) {
        Optional<UserInfo> result = userInfoRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }

    public UserInfo save(UserInfo userInfo, Users foundUser) {
        Optional<UserInfo> editUserInfo = userInfoRepository.findById(userInfo.getId());
        if (editUserInfo.isPresent()) {
            UserInfo user = editUserInfo.get();
            user.setUser(foundUser);
            user.setFavorites(userInfo.getFavorites());
            user.setCurrentJob(userInfo.getCurrentJob());
            user.setOtherInfo(userInfo.getOtherInfo());
            user.setLivingAt(userInfo.getLivingAt());
            user.setWorkingAt(userInfo.getWorkingAt());
            user.setDateOfBirth(userInfo.getDateOfBirth());
            user.getUser().setFullName(userInfo.getUser().getFullName());
            user.getUser().setGender(userInfo.getUser().getGender());
            user.getUser().setAvatarUrl(userInfo.getUser().getAvatarUrl());
            userInfoRepository.save(user);
        } else
            throw new ResourceNotFoundException("Không tìm thấy user với ID: " + userInfo.getId() + "!");
        return userInfo;
    }
}
