package com.smartpark.backend1.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
  private String ancienPassword;
  private String nouveauPassword;
}
