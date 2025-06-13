/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.model.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record Provider(@NonNull Channel channel, @NotBlank String name) {}
