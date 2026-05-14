package org.example.application.controller;

import org.example.application.UserApplicationService;
import org.example.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserApplicationService userApplicationService;

  @Test
  @WithMockUser(username = "testUser", roles = {"USER"})
  public void shouldReturnOkStatus_WhenGettingAllUsers() throws Exception {
    mockMvc.perform(get("/user/v1"))
      .andExpect(status().isOk());
  }
}
