package project.reviewing.unit.review.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import project.reviewing.common.exception.ErrorType;
import project.reviewing.review.domain.Review;
import project.reviewing.review.exception.InvalidReviewException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Review 는 ")
public class ReviewTest {

    @DisplayName("리뷰를 생성할 수 있다.")
    @CsvSource(value = {"1, 2, 2, true"})
    @ParameterizedTest
    void validCreateReview(
            final Long revieweeId, final Long reviewerId, final Long reviwerMemberId, final Boolean isReviewer
    ) {
        final Review newReview = Review.assign(
                revieweeId, reviewerId, "제목", "본문", "github.com/bboor/project/pull/1", reviwerMemberId, isReviewer
        );

        assertAll(
                () -> assertThat(newReview.getRevieweeId()).isEqualTo(revieweeId),
                () -> assertThat(newReview.getReviewerId()).isEqualTo(reviewerId)
        );
    }

    @DisplayName("리뷰이와 동일한 리뷰어에게 요청하는 리뷰는 생성할 수 없다.")
    @CsvSource(value = {"1, 1, 1, true"})
    @ParameterizedTest
    void createWithSameReviewerAsReviewee(
            final Long revieweeId, final Long reviewerId, final Long reviwerMemberId, final Boolean isReviewer
    ) {
        assertThatThrownBy(
                () -> Review.assign(
                        revieweeId, reviewerId, "제목", "본문",
                        "github.com/bboor/project/pull/1", reviwerMemberId, isReviewer
                ))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessage(ErrorType.SAME_REVIEWER_AS_REVIEWEE.getMessage());
    }

    @DisplayName("활동하지 않는 리뷰어에게 요청하는 리뷰는 생성할 수 없다.")
    @CsvSource(value = {"1, 2, 2, false"})
    @ParameterizedTest
    void createWithNotRegisteredReviewer(
            final Long revieweeId, final Long reviewerId, final Long reviwerMemberId, final Boolean isReviewer
    ) {
        assertThatThrownBy(
                () -> Review.assign(
                        revieweeId, reviewerId, "제목", "본문",
                        "github.com/bboor/project/pull/1", reviwerMemberId, isReviewer
                ))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessage(ErrorType.DO_NOT_REGISTERED.getMessage());
    }

    @DisplayName("리뷰를 수정할 수 있다.")
    @Test
    void validUpdateReview() {
        final Review review = Review.assign(1L, 1L, "제목", "본문", "github.com/bboor/project/pull/1", 2L, true);

        review.update(1L, "수정본문");

        assertThat(review.getContent()).isEqualTo("수정본문");
    }

    @DisplayName("리뷰를 생성한 리뷰이가 아니면 수정할 수 없다.")
    @Test
    void updateWithNotRevieweeOfReview() {
        final Long revieweeId = 1L;
        final Long anotherRevieweeId = 2L;
        final Review review = Review.assign(revieweeId, 1L, "제목", "본문", "github.com/bboor/project/pull/1", 2L, true);

        assertThatThrownBy(() -> review.update(anotherRevieweeId, "수정본문"))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessage(ErrorType.NOT_REVIEWEE_OF_REVIEW.getMessage());
    }
}
