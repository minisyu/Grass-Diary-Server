package chzzk.grassdiary.auth.service;

import chzzk.grassdiary.auth.client.GoogleOAuthClient;
import chzzk.grassdiary.auth.jwt.JwtTokenProvider;
import chzzk.grassdiary.auth.service.dto.AuthMemberPayload;
import chzzk.grassdiary.auth.service.dto.GoogleOAuthToken;
import chzzk.grassdiary.auth.service.dto.GoogleUserInfo;
import chzzk.grassdiary.auth.service.dto.JWTTokenResponse;
import chzzk.grassdiary.auth.util.GoogleOAuthUriGenerator;
import chzzk.grassdiary.domain.member.Member;
import chzzk.grassdiary.domain.member.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OAuthService {
    private final GoogleOAuthUriGenerator googleOAuthUriGenerator;
    private final GoogleOAuthClient googleOAuthClient;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String findRedirectUri() {
        return googleOAuthUriGenerator.generateUrl();
    }

    public JWTTokenResponse loginGoogle(String code) {
        GoogleOAuthToken googleAccessToken = googleOAuthClient.getGoogleAccessToken(code);

        GoogleUserInfo googleUserInfo = googleOAuthClient.getGoogleUserInfo(googleAccessToken.accessToken());

        Member member = createMemberIfNotExist(googleUserInfo);

        String accessToken = jwtTokenProvider.generateAccessToken(AuthMemberPayload.from(member));
        log.info("[토큰 생성 완료] - 구글로부터 받은 사용자 email: {}", googleUserInfo.email());
        log.info("[토큰 생성 완료] - repository에 저장된 사용자 email: {}", member.getEmail());

        return new JWTTokenResponse(accessToken);
    }

    private Member createMemberIfNotExist(GoogleUserInfo googleUserInfo) {
        Optional<Member> foundMember = memberRepository.findByEmail(googleUserInfo.email());

        if (foundMember.isPresent()) {
            return foundMember.get();
        }

        Member member = Member.builder()
                .nickname(googleUserInfo.nickname())
                .email(googleUserInfo.email())
                .picture(googleUserInfo.picture())
                .build();

        return memberRepository.save(member);
    }
}