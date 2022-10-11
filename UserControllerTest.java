package skkuchin.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ExceptionCollector;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import skkuchin.service.service.UserService;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
//@RunWith(SpringJUnit4ClassRunner.class) //spring-test에서 제공하는 단위 테스트를 위한 클래스 러너
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    UserController userController;
    //@Autowired
    private MockMvc mock;

    //@MockBean
    //private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mock = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    //perform: 가상의 request 처리
    //expect: 가상의 response 검증
    //do: 테스트 과정에서 콘솔 출력 등 직접 처리할 일 작성
    @Test
    @DisplayName("[GET] /api/users 성공")
    void 모든_회원_불러오기() throws Exception {
        MockHttpServletRequestBuilder builder =
                get("/api/users");

        mock.perform(builder)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[POST] /api/user/save 성공")
    void 회원가입_성공() throws Exception {
        String form = objectMapper.writeValueAsString(
                new SignUpForm("skku", "skku123", "1234", "1234"));

        mock.perform(post("/api/user/save")
                        .content(form)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("[POST] /api/user/save 실패")
    void 비밀번호_불일치() throws Exception {
        String form = objectMapper.writeValueAsString(
                new SignUpForm("skku", "skku123", "1234", "123"));

        mock.perform(post("/api/user/save")
                        .content(form)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertThat(result.getResolvedException().getMessage())
                        .isEqualTo("re_password_error"));
    }

    @Test
    void addRoleToUser() {
    }

    @Test
    void verifyToken() {
    }

    @Test
    void refreshToken() {
    }

    @Test
    void getApiUser() {
    }
}

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> SampleException(RuntimeException e) {

    }
}