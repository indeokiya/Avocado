package com.avocado.userserver.api.service

import com.avocado.userserver.api.response.TokenRefreshResp
import com.avocado.userserver.common.error.BaseException
import com.avocado.userserver.common.error.ResponseCode
import com.avocado.userserver.common.utils.ConvertIdUtil
import com.avocado.userserver.db.repository.ConsumerRepository
import com.avocado.userserver.db.repository.ProviderRepository
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service

@Service
class TokenService (
    val jwtProvider: JwtProvider,
    val providerRepository: ProviderRepository,
    val consumerRepository: ConsumerRepository,
    val convertIdUtil: ConvertIdUtil,
) {
    suspend fun refresh(request: ServerHttpRequest): TokenRefreshResp {
        val token = jwtProvider.getToken(request)
        val claims = jwtProvider.parseClaims(token)
        val id = convertIdUtil.unHex(claims["id"]?.toString()?:throw BaseException(ResponseCode.WRONG_TOKEN))

        var resp: TokenRefreshResp = when (claims["type"]?.toString()) {
            "consumer" -> getConsumerToken(id)
            "provider" -> getProviderToken(id)
            else -> throw BaseException(ResponseCode.WRONG_TOKEN)
        }

        return resp
    }

    private suspend fun getConsumerToken(id: ByteArray): TokenRefreshResp {
        val consumer = consumerRepository.findById(id)?: throw BaseException(ResponseCode.NOT_FOUND)
        return TokenRefreshResp(jwtProvider.getAccessToken(consumer), jwtProvider.getRefreshToken(consumer))
    }

    private suspend fun getProviderToken(id: ByteArray): TokenRefreshResp {
        val provider = providerRepository.findById(id)?: throw BaseException(ResponseCode.NOT_FOUND)
        return TokenRefreshResp(jwtProvider.getAccessToken(provider), jwtProvider.getRefreshToken(provider))
    }

}