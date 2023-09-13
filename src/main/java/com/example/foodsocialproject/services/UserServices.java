package com.example.foodsocialproject.services;

import com.example.foodsocialproject.dto.RegistrationRequest;
import com.example.foodsocialproject.entity.UserInfo;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.entity.VerificationToken;
import com.example.foodsocialproject.exception.AlreadyExistsException;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.UserInfoRepository;
import com.example.foodsocialproject.repository.UserRepository;
import com.example.foodsocialproject.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServices implements TableService{
    private final UserRepository userRepository;
    private final PostsService postsService;
    private final UserInfoRepository  userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;

    @Override
    public List<Users> getList() {
        return userRepository.findAll();
    }

    public Optional<Users> findByEmail(String email) {
        Optional<Users> result = userRepository.findByEmail(email);
        if (result.isPresent()){
            return result;
        }
        else return null;
    }

    public void save(Users user) {
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new ResourceNotFoundException("Người dùng với email " + user.getEmail() + " đã tồn tại");
            } else {
                throw e;
            }
        }
    }

    @Override
    public void delete(UUID id) {
        Long count = userRepository.countById(id);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional get(UUID id) {
        Optional<Users> result = userRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id + "!");
    }

    public Optional<Users> findbyEmail(String email) {
        Optional<Users> result = userRepository.findByEmail(email);
        if (result.isPresent()){
            return result;
        }
        else return null;

    }

    public Users registerUser(RegistrationRequest request) {
        Optional<Users> user = this.findbyEmail(request.email());
        if (user != null) {
            throw new AlreadyExistsException("Người dùng với email " + request.email() + " đã tồn tại!");
        }
        var newUser = new Users();
        newUser.setFullName(request.fullName());
        newUser.setEmail(request.email());

        // Xóa bỏ các ký tự không phải số
        String cleanedNumber = request.phone().replaceAll("[^\\d]", "");
        // Kiểm tra nếu số điện thoại bắt đầu bằng "0"
        if (cleanedNumber.startsWith("0")) {
            // Thay thế "0" đầu tiên bằng "+84"
            cleanedNumber = "+84" + cleanedNumber.substring(1);
        }
        newUser.setPhone(cleanedNumber);
        newUser.setGender("Nam");

        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(request.role());
        var newUserInfo = new UserInfo();
        newUserInfo.setUser(newUser);
        userInfoRepository.save(newUserInfo);
        return userRepository.save(newUser);
    }

    public void saveUserVerificationToken(Users user, String token) {
        var verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
    }

    public String validateToken(String theToken) {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if (token==null){
            return "Invalid verification token";
        }
        Users user = token.getUser();
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0){
            //tokenRepository.delete(token);
            return "Token already expired!";
        }
        user.setActive(true);
        userRepository.save(user);
        return "Valid";
    }
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = tokenRepository.findByToken(oldToken);
        if (verificationToken == null) {
            throw new ResourceNotFoundException("Token " + oldToken + " is not valid");
        }
        var verificationTokenTime = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpirationTime(verificationTokenTime.getTokenExpirationTime());
        return tokenRepository.save(verificationToken);
    }

    public Users findById(UUID id) {
        Optional<Users> result = userRepository.findById(id);
        if (result.isPresent()){
            return result.get();
        }
        else return null;
    }
    public Page<Users> findPaginated(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return this.userRepository.findAll(pageable);
    }

    public List<Users> search(String q) {
        if (q != null) {
            return this.userRepository.search(q);
        }
        else {
            return postsService.getTop5User();
        }
    }
}
