package com.neoga.platform.memberstore.review.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.neoga.platform.event.ReviewEventDispatcher;
import com.neoga.platform.exception.custom.CReviewNotExistException;
import com.neoga.platform.memberstore.member.domain.Members;
import com.neoga.platform.memberstore.member.repository.MemberRepository;
import com.neoga.platform.memberstore.review.dto.RegisterDto;
import com.neoga.platform.memberstore.review.domain.Review;
import com.neoga.platform.memberstore.review.dto.ReviewDto;
import com.neoga.platform.memberstore.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final ReviewEventDispatcher reviewEventDispatcher;

    @Override
    public ReviewDto addReview(Long memberId, Long registerId, String content) throws JsonProcessingException {
        Members refMembers = memberRepository.getOne(memberId);
        Members refRegister = memberRepository.getOne(registerId);

        Review savedReview = reviewRepository.save(Review.builder()
                .store(refMembers)
                .register(refRegister)
                .content(content).build());

        reviewEventDispatcher.send(
                memberId,
                registerId,
                content,
                savedReview.getCreateDt()
        );

        log.info("[review event] content: {}",content);

        return mapReviewReviewDto(savedReview);
    }

    @Override
    public List<ReviewDto> getReviews(Long storeId) {
        List<Review> reviewList = reviewRepository.findAllByStore_IdOrderByCreateDtDesc(storeId);

        return reviewList.stream().map(this::mapReviewReviewDto).collect(Collectors.toList());
    }

    @Override
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(() -> new CReviewNotExistException("해당 리뷰가 존재하지 않습니다."));
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private ReviewDto mapReviewReviewDto(Review review) {
        ReviewDto reviewDto = modelMapper.map(review, ReviewDto.class);

        RegisterDto registerDto = new RegisterDto();
        registerDto.setRegisterId(review.getRegister().getId());
        registerDto.setRegisterName(review.getRegister().getName());

        String imagePath = review.getRegister().getImagePath();
        if (imagePath != null) {
            registerDto.setImagePath(imagePath);
        }

        reviewDto.setRegister(registerDto);

        return reviewDto;
    }
}
