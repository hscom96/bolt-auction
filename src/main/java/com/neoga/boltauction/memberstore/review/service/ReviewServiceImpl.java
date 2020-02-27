package com.neoga.boltauction.memberstore.review.service;

import com.neoga.boltauction.exception.custom.CReviewNotExistException;
import com.neoga.boltauction.memberstore.member.domain.Members;
import com.neoga.boltauction.memberstore.review.dto.RegisterDto;
import com.neoga.boltauction.memberstore.review.domain.Review;
import com.neoga.boltauction.memberstore.review.dto.ReviewDto;
import com.neoga.boltauction.memberstore.review.repository.ReviewRepository;
import com.neoga.boltauction.memberstore.store.domain.Store;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private static JSONParser jsonParser = new JSONParser();

    @Override
    public ReviewDto addReview(Long storeId, Long memberId, String content) {
        Store refStore = entityManager.getReference(Store.class, storeId);
        Members refMembers = entityManager.getReference(Members.class, memberId);

        Review review = new Review();

        review.setStore(refStore);
        review.setRegister(refMembers);
        review.setContent(content);
        reviewRepository.save(review);

        return mapReviewReviewDto(review);
    }

    @Override
    public List<ReviewDto> getReviews(Long storeId) {
        List<Review> reviewList = reviewRepository.findAllByStore_IdOrderByCreateDtDesc(storeId);

        return reviewList.stream().map(review -> mapReviewReviewDto(review)).collect(Collectors.toList());
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
        registerDto.setId(review.getRegister().getId());
        registerDto.setName(review.getRegister().getName());
        try {
            registerDto.setImagePath((JSONObject) jsonParser.parse(review.getRegister().getStore().getImagePath()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        reviewDto.setRegister(registerDto);

        return reviewDto;
    }
}