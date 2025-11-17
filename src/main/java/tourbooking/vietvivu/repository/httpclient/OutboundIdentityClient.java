package tourbooking.vietvivu.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import feign.QueryMap;
import tourbooking.vietvivu.dto.request.ExchangeTokenRequest;
import tourbooking.vietvivu.dto.response.ExchangeTokenResponse;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
