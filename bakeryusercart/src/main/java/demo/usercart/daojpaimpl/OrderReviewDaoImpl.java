package demo.usercart.daojpaimpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.OrderReviewDao;
import demo.usercart.model.OrderReview;
import demo.usercart.repository.OrderReviewRepository;

@Repository("orderReviewDaoJpa")
public class OrderReviewDaoImpl implements OrderReviewDao {

	@Autowired
	private OrderReviewRepository orderReviewRepository;

	@Override
	public OrderReview save(OrderReview review) {
		return orderReviewRepository.save(review);
	}

	@Override
	public Optional<OrderReview> findById(Long reviewId) {
		return orderReviewRepository.findById(reviewId);
	}

	@Override
	public boolean existsByOrderId(Integer orderId) {
		return orderReviewRepository.existsByOrder_Id(orderId);
	}

	@Override
	public Optional<OrderReview> findByOrderId(Integer orderId) {
		return orderReviewRepository.findByOrder_Id(orderId);
	}

	@Override
	public Page<OrderReview> findAll(Pageable pageable) {
		return orderReviewRepository.findAllByOrderByCreatedAtDescIdDesc(pageable);
	}

	@Override
	public Page<OrderReview> searchByKeyword(String pattern, Pageable pageable) {
		return orderReviewRepository.searchByKeyword(pattern, pageable);
	}

}
