package demo.usercart.daojpaimpl;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.RefreshTokenDao;
import demo.usercart.model.RefreshToken;
import demo.usercart.repository.RefreshTokenRepository;

@Repository("refreshTokenDaoJpa")
public class RefreshTokenDaoImpl implements RefreshTokenDao{
	
	 @Autowired
	    private RefreshTokenRepository refreshTokenRepository;

	    @Override
	    public RefreshToken save(RefreshToken refreshToken) {
	        return refreshTokenRepository.save(refreshToken);
	    }

	    @Override
	    public Optional<RefreshToken> findByTokenHashForUpdate(
	            String tokenHash
	    ) {
	        return refreshTokenRepository.findByTokenHashForUpdate(
	                tokenHash
	        );
	    }

	    @Override
	    public int revokeFamily(
	            String familyId,
	            Instant revokedAt
	    ) {
	        return refreshTokenRepository.revokeFamily(
	                familyId,
	                revokedAt
	        );
	    }

}
