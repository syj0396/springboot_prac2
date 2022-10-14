package skkuchin.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import skkuchin.service.common.BaseIntegrationTest;
import skkuchin.service.config.UserSetUp;
import skkuchin.service.domain.AppUser;
import skkuchin.service.domain.Role;
import skkuchin.service.exception.DiscordException;
import skkuchin.service.exception.DuplicateException;

import java.util.ArrayList;
import java.util.Collection;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private UserSetUp userSetUp;

    @Test
    @WithMockUser
    @DisplayName("[GET] /api/users 성공")
    public void 모든_회원_불러오기() throws Exception {
        //given
        Collection<Role> roles = new ArrayList<>();
        roles.add(new Role(1L, "ROLE_USER"));
        userSetUp.saveUser("user1", "user111", "1234", roles);
        userSetUp.saveUser("user2", "user222", "1234", roles);

        //when
        ResultActions resultActions = mvc.perform(get("/api/users")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", equalTo(2)));
    }

    @Test
    @DisplayName("[POST] /api/user/save 성공")
    void 회원가입_성공() throws Exception {
        //given
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); //SignUpForm을 읽어들이기 위함.
        String form = objectMapper.writeValueAsString(
                new SignUpForm("user", "user111", "1234", "1234"));

        //when
        ResultActions resultActions = mvc.perform(post("/api/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(form)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("nickname", equalTo("user")));
    }

    @Test
    @DisplayName("[POST] /api/user/save 실패")
    void 비밀번호_불일치() throws Exception {
        //given
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String form = objectMapper.writeValueAsString(
                new SignUpForm("user", "user111", "124", "1234"));

        //when
        ResultActions resultActions = mvc.perform(post("/api/user/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DiscordException))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("re_password_error"));
    }

    @Test
    @Disabled
    @DisplayName("[POST] /api/user/save 실패")
    void nickname_중복_확인() throws Exception {
        //given
        Collection<Role> roles = new ArrayList<>();
        roles.add(new Role(1L, "ROLE_USER"));
        userSetUp.saveUser("user1", "user111", "1234", roles);
        //userSetUp.saveUser("user1", "user111", "1234", roles);
        //ConstraintViolationException DataIntegrityViolationException

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String form = objectMapper.writeValueAsString(
                new SignUpForm("user1", "user222", "1234", "1234"));

        //when
        ResultActions resultActions = mvc.perform(post("/api/user/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo("duplicate_error"));
    }
}

@Data
@Builder
class SignUpForm {

    private String nickname;
    private String username;
    private String password;
    private String re_password;

    public SignUpForm(String nickname, String username, String password, String re_password) {
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.re_password = re_password;
    }

    public boolean checkPassword() {
        if (this.password.equals(this.re_password)) return true;
        else return false;
    }

    public AppUser toEntity() {
        return AppUser
                .builder().nickname(this.nickname).username(this.username).password(this.password)
                .roles(new ArrayList<>())
                .build();
    }
}