package com.uber.bg.uber.bg.DTOs;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class ChangeProfileDataDTO extends BaseDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String phoneNumber;
    private byte[] photo;

}
