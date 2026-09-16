package demo.usercart.daojpaimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.OrderReviewImageDao;
import demo.usercart.model.OrderReviewImage;
import demo.usercart.repository.OrderReviewImageRepository;

@Repository("orderReviewImageDaoJpa")
public class OrderReviewImageDaoImpl implements OrderReviewImageDao {

	@Autowired
	private OrderReviewImageRepository orderReviewImageRepository;

	@Override
	public OrderReviewImage save(OrderReviewImage image) {
		return orderReviewImageRepository.save(image);
	}

	@Override
	public List<OrderReviewImage> findByReviewId(Long reviewId) {
		return orderReviewImageRepository.findByReview_IdOrderBySortOrderAsc(reviewId);
	}

	@Override
	public Optional<OrderReviewImage> findByIdAndReviewId(Long imageId, Long reviewId) {
		return orderReviewImageRepository.findByIdAndReview_Id(imageId, reviewId);
	}

	@Override
	public long countByReviewId(Long reviewId) {
		return orderReviewImageRepository.countByReview_Id(reviewId);
	}

	@Override
	public void delete(OrderReviewImage image) {
		orderReviewImageRepository.delete(image);
	}

	@Override
	public void flush() {
		orderReviewImageRepository.flush();
	}

}
