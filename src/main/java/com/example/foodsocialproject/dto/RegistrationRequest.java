package com.example.foodsocialproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(String role,
                                  @NotBlank(message = "Mật khẩu không được bỏ trống!") @Size(min = 6, max = 20, message = "Mật khẩu phải nằm trong khoảng 6 -> 20 ký tự") String password,
                                  @NotBlank(message = "Họ tên không được bỏ trống!") String fullName,
                                  @NotBlank(message = "Số điện thoại không được để trống!") String phone,
                                  @NotBlank(message = "Email không được bỏ trống!") @Email(message = "Email không hợp lệ!") String email
){

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return phone;
    }

    @Override
    public String role() {
        return role;
    }
}
