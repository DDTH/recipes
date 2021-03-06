package com.github.ddth.recipes.apiservice.thrift;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.commons.utils.MapUtils;
import com.github.ddth.recipes.apiservice.ApiAuth;
import com.github.ddth.recipes.apiservice.ApiContext;
import com.github.ddth.recipes.apiservice.ApiParams;
import com.github.ddth.recipes.apiservice.ApiResult;
import com.github.ddth.recipes.apiservice.ApiRouter;
import com.github.ddth.recipes.apiservice.thrift.def.TApiAuth;
import com.github.ddth.recipes.apiservice.thrift.def.TApiParams;
import com.github.ddth.recipes.apiservice.thrift.def.TApiResult;
import com.github.ddth.recipes.apiservice.thrift.def.TApiService;
import com.github.ddth.recipes.apiservice.thrift.def.TDataEncoding;

/**
 * Handle API calls via Thrift.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since v0.2.0
 */
public class ApiServiceHandler implements TApiService.Iface {

    private final Logger LOGGER = LoggerFactory.getLogger(ApiServiceHandler.class);

    private ApiRouter apiRouter;

    public ApiServiceHandler() {
    }

    public ApiServiceHandler(ApiRouter apiRouter) {
        setApiRouter(apiRouter);
    }

    public ApiRouter getApiRouter() {
        return apiRouter;
    }

    public ApiServiceHandler setApiRouter(ApiRouter apiRouter) {
        this.apiRouter = apiRouter;
        return this;
    }

    /*------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void ping() throws TException {
        //EMPTY
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TApiResult check(TApiAuth apiAuth) throws TException {
        long t = System.currentTimeMillis();
        long d = System.currentTimeMillis() - t;
        long c = apiRouter.getConcurency();
        return ThriftUtils.buildResponse(
                ApiResult.DEFAULT_RESULT_OK.clone(MapUtils.createMap("t", t, "d", d, "c", c)),
                TDataEncoding.JSON_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TApiResult call(String _apiName, TApiAuth _apiAuth, TApiParams _apiParams)
            throws TException {
        long t = System.currentTimeMillis();
        try {
            ApiParams apiParams = ThriftUtils.parseParams(_apiParams);
            ApiContext apiContext = ApiContext.newContext("THRIFT", _apiName);
            ApiAuth apiAuth = ThriftUtils.parseAuth(_apiAuth);

            ApiResult apiResult = apiRouter.callApi(apiContext, apiAuth, apiParams);
            TDataEncoding returnedEncoding = _apiParams.expectedReturnEncoding == null
                    || _apiParams.expectedReturnEncoding == TDataEncoding.JSON_DEFAULT
                    ? _apiParams.encoding
                    : _apiParams.expectedReturnEncoding;
            return ThriftUtils.buildResponse(
                    apiResult != null ? apiResult : ApiResult.DEFAULT_RESULT_UNKNOWN_ERROR,
                    returnedEncoding);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            long d = System.currentTimeMillis() - t;
            long c = apiRouter.getConcurency();
            return ThriftUtils.buildResponse(new ApiResult(ApiResult.STATUS_ERROR_SERVER,
                            e.getClass().getName() + " - " + e.getMessage())
                            .setDebugData(MapUtils.createMap("t", t, "d", d, "c", c)),
                    TDataEncoding.JSON_STRING);
        }
    }

}
