package skkuchin.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import skkuchin.service.domain.AppUser;
import skkuchin.service.domain.Role;
import skkuchin.service.exception.BlankException;
import skkuchin.service.exception.DiscordException;
import skkuchin.service.exception.DuplicateException;
import skkuchin.service.service.UserService;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@Transactional
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    UserController userController;
    //@Autowired
    private MockMvc mock;

    @Mock
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mock = MockMvcBuilders.standaloneSetup(userController)
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

        mock.perform(get("/api/users"))
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DiscordException))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("re_password_error"));
    }

    @Test
    @DisplayName("[POST] /api/user/save 실패")
    void nickname_중복_확인() throws Exception {
        Collection<Role> roles = new ArrayList<>();
        roles.add(new Role(1L, "ROLE_USER"));
        //AppUser user1 = new AppUser(1L, "skku", "skku123", "1234", new ArrayList<>());
        //Mockito.when(userService.saveUser(user1)).thenReturn(user1);
        //userService.saveUser(user1);
        //-> mock객체여서 바로 사라지는건가..? -> 아마 db랑 연결된게 아니고 입력 반환만 하니까.

        String user1 = objectMapper.writeValueAsString(
                new SignUpForm("", "skku", "1234", "1234"));

        //form 성공적으로 저장
        mock.perform(post("/api/user/save")
                .content(user1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
/*
        String user2 = objectMapper.writeValueAsString(
                new SignUpForm("skku", "skku123", "1234", "1234"));

        mock.perform(post("/api/user/save")
                        .content(user2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //.andExpect(status().is2xxSuccessful());
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("duplicate_error"));
*/
    }

    @Test
    @DisplayName("[POST] /api/user/save 실패")
    void 빈_값_확인() throws Exception { // 빈 값: ""
        String user = objectMapper.writeValueAsString(
                new SignUpForm("", "skku", "1234", "1234"));
/*
        mock.perform(post("/api/user/save")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //.andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(BlankException.class)))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("blank_error"));
   */
        MvcResult result = mock
                .perform(
                        post("/api/user/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(user))
                .andExpect(
                        (rslt) -> assertTrue(rslt.getResolvedException().getClass().isAssignableFrom(BlankException.class))
                )
                .andReturn();
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