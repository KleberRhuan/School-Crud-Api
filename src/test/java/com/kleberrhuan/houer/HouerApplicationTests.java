/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled(
  "Desabilitado por dependência de banco de dados na inicialização do contexto."
)
class HouerApplicationTests {

  @Test
  @Disabled(
    "Desabilitado para evitar dependência de banco de dados nas execuções de teste."
  )
  void contextLoads() {}
}
