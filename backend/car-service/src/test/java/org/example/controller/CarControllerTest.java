package org.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.application.CarApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CarController.class)
public class CarControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CarApplicationService carApplicationService;

  @Test
  @WithMockUser(roles = {"USER"})
  public void shouldReturnOk_WhenFetchingCars() throws Exception {
    // УВАГА: Перевір у CarController.java, який там насправді шлях!
    // Якщо там @RequestMapping("/car/v1"), то пиши "/car/v1"
    mockMvc.perform(get("/car/v1")).andExpect(status().isOk());
  }
}
