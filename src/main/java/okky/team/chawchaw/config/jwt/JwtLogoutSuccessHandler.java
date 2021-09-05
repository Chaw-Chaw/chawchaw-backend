package okky.team.chawchaw.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import okky.team.chawchaw.user.UserService;
import okky.team.chawchaw.utils.dto.DefaultResponseVo;
import okky.team.chawchaw.utils.message.ResponseUserMessage;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    private UserService userService;
    private Environment env;

    public JwtLogoutSuccessHandler(UserService userService, Environment env) {
        this.userService = userService;
        this.env = env;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        PrintWriter writer = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        String jwtHeader = request.getHeader("Authorization");

        if (jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
            writer.print(mapper.writeValueAsString(DefaultResponseVo.res(ResponseUserMessage.LOGOUT_FAIL, false)));
            return;
        }

        try {
            String token = jwtHeader.replace("Bearer ", "");
            String email = JWT.require(Algorithm.HMAC512(env.getProperty("token.secret")))
                    .build()
                    .verify(token)
                    .getClaim("email")
                    .asString();

            userService.updateLastLogout(email);

        } catch (Exception e) {
            writer.print(mapper.writeValueAsString(DefaultResponseVo.res(ResponseUserMessage.LOGOUT_FAIL, false)));
            return;
        }

        writer.print(mapper.writeValueAsString(DefaultResponseVo.res(ResponseUserMessage.LOGOUT_SUCCESS, true)));

    }

}
