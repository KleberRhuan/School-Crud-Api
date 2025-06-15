/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import java.util.Optional;

public record TokenPair(String access, Optional<String> refresh, long ttlSec) {}
