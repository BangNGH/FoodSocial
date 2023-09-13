package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.UserRela;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.UserRelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRelaService implements TableService{
    private final UserRelaRepository userRelaRepository;
    @Override
    public List<UserRela> getList() {
        return userRelaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        Long count = userRelaRepository.countById(id);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Không tìm thấy ID: " + id);
        }
        userRelaRepository.deleteById(id);
    }

    @Override
    public Optional get(UUID id) {
        Optional<UserRela> result = userRelaRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }

    public void save(Users followerAccount, Users followingAccount) {
        UserRela newRela = new UserRela();
        newRela.setFollower(followerAccount);
        newRela.setUser(followingAccount);
        this.userRelaRepository.save(newRela);
    }

    public void unfollow(Users followerAccount, Users followingAccount) {
        // Tìm mối quan hệ theo dõi trong cơ sở dữ liệu
        UserRela userRela = userRelaRepository.findByFollowerAndUser(followerAccount, followingAccount);

        // Nếu tìm thấy mối quan hệ, thì xóa nó
        if (userRela != null) {
            userRelaRepository.delete(userRela);
        }
    }

}
