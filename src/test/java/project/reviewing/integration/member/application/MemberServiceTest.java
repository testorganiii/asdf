package project.reviewing.integration.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import project.reviewing.common.exception.ErrorType;
import project.reviewing.member.command.application.MemberService;
import project.reviewing.member.command.application.request.ReviewerRegistrationRequest;
import project.reviewing.member.command.application.request.UpdatingMemberRequest;
import project.reviewing.member.command.domain.Member;
import project.reviewing.member.command.domain.MemberRepository;
import project.reviewing.member.command.domain.Reviewer;
import project.reviewing.member.exception.MemberNotFoundException;

@DisplayName("MemberService 는 ")
@DataJpaTest
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("내 정보 수정 시")
    @Nested
    class MemberUpdateTest {

        @DisplayName("정상적인 경우 내 정보를 수정한다.")
        @Test
        void updateMember() {
            final MemberService sut = new MemberService(memberRepository);
            final Member member = createMember(new Member(1L, "username", "email@gmail.com", "image.png", "github.com/profile"));
            final UpdatingMemberRequest updatingMemberRequest = new UpdatingMemberRequest("newUsername",
                    "newEmail@gmail.com");

            sut.update(member.getId(), updatingMemberRequest);

            final Member actual = getMember(member.getId());
            assertAll(
                    () -> assertThat(actual.getUsername()).isEqualTo("newUsername"),
                    () -> assertThat(actual.getEmail()).isEqualTo("newEmail@gmail.com")
            );
        }

        @DisplayName("회원이 존재하지 않는 경우 예외를 반환한다.")
        @Test
        void updateNotExistMember() {
            final MemberService sut = new MemberService(memberRepository);
            final Long notExistMemberId = 1L;
            final UpdatingMemberRequest updatingMemberRequest = new UpdatingMemberRequest("newUsername",
                    "newEmail@gmail.com");

            assertThatThrownBy(() -> sut.update(notExistMemberId, updatingMemberRequest))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage(ErrorType.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @DisplayName("리뷰어 등록 시 ")
    @Nested
    class ReviewerRegistrationTest {

        @DisplayName("정상적인 경우 리뷰어를 등록한다.")
        @Test
        void registerReviewer() {
            final MemberService sut = new MemberService(memberRepository);
            final Member member = createMember(new Member(1L, "username", "email@gmail.com", "image.png", "github.com/profile"));
            final ReviewerRegistrationRequest reviewerRegistrationRequest = new ReviewerRegistrationRequest(
                    "백엔드", "신입", List.of(1L, 2L), "자기 소개입니다."
            );

            sut.registerReviewer(member.getId(), reviewerRegistrationRequest);
            entityManager.flush();

            final Reviewer actual = getMember(member.getId()).getReviewer();
            assertAll(
                    () -> assertThat(actual.getId()).isNotNull(),
                    () -> assertThat(actual).usingRecursiveComparison()
                            .ignoringFields("id", "member")
                            .isEqualTo(reviewerRegistrationRequest.toEntity()),
                    () -> assertThat(actual.getMember().getId()).isEqualTo(member.getId())
            );
        }

        @DisplayName("회원이 존재하지 않는 경우 예외를 반환한다.")
        @Test
        void registerReviewerByNotExistMember() {
            final MemberService sut = new MemberService(memberRepository);
            final Long wrongMemberId = 1L;
            final ReviewerRegistrationRequest reviewerRegistrationRequest = new ReviewerRegistrationRequest(
                    "백엔드", "신입(1년 이하)", List.of(1L, 2L), "자기 소개입니다."
            );

            assertThatThrownBy(() -> sut.registerReviewer(wrongMemberId, reviewerRegistrationRequest))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage(ErrorType.MEMBER_NOT_FOUND.getMessage());
        }
    }

    private Member createMember(final Member member) {
        return memberRepository.save(member);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }
}
