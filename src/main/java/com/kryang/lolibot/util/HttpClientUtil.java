package com.kryang.lolibot.util;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http请求封装
 *
 * @author hebiao
 * @version $Id:HttpClientUtil.java, v0.1 2019/5/37 13:22 hebiao Exp $$
 */
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static PoolingHttpClientConnectionManager cm;
    private static String UTF_8 = "UTF-8";
    private static final CloseableHttpClient httpClient;

    public static final int CONN_TIMEOUT = 1000 * 30;

    private static HttpClient fbaHttpClient;

    private static AtomicBoolean haveFbaHttpClient = new AtomicBoolean(true);

    static {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(2 * 60000).build();
        httpClient = HttpClientBuilder.create().setRetryHandler(new HttpRetryHandler())
                .setDefaultRequestConfig(config).build();
    }

    /**
     * 单例模式HttpClient实例,一个JVM只允许一个请求
     * @return
     */
    public static synchronized HttpClient getInstance() {
        if (fbaHttpClient == null) {
            fbaHttpClient = HttpClients.custom().build();
        }
        if (haveFbaHttpClient.get()) {
            haveFbaHttpClient.set(false);
            return fbaHttpClient;
        } else {
            logger.warn("fbaHttpClient is in used {}" ,Thread.currentThread());
            return null;
        }
    }

    /**
     * 释放haveFbaHttpClient互斥锁,单例模式下暂不必考虑并发
     */
    public static void releaseHttpClient () {
        if (haveFbaHttpClient != null) haveFbaHttpClient.set(true);
    }


    private static synchronized void init() throws Exception {
        if (cm == null) {
            //Secure Protocol implementation.
            SSLContext ctx = SSLContext.getInstance("SSL");
            //Implementation of a trust manager for X509 certificates
            X509TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] xcs,
                                               String string) {

                }

                public void checkServerTrusted(X509Certificate[] xcs,
                                               String string) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(ctx))
                    .build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setMaxTotal(50);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
        }
    }

    /**
     * 通过连接池获取HttpClient
     */
    private static CloseableHttpClient getHttpClient() {
        try {
            init();
            return HttpClients.custom().setConnectionManager(cm).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 处理Http请求
     */
    private static String getResult(HttpRequestBase request) {
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("接口返回状态为失败,statusCode:{}", statusCode);
            }
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                String result = EntityUtils.toString(entity);
                response.close();
                // httpClient.close();
                if (statusCode != HttpStatus.SC_OK) {
                    logger.error("接口返回失败,{}", result);
                    throw new Exception("接口返回错误");
                }
                return result;
            }
            logger.error("接口返回失败,entity 为空");
            throw new Exception("接口返回错误");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }
        return pairs;
    }

    /**
     *
     */
    public static String httpGetRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params)
            throws URISyntaxException {
        URIBuilder ub = new URIBuilder(url);
//        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet);
    }

    public static String httpPostRequest(String url) {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }


    public static String httpPostRequest(String url, Map<String, Object> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getResult(httpPost);
    }

    public static String httpPostRequestStr(String url, Map<String, Object> headers, Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        String textMsg = JSONObject.toJSONString(params);
        StringEntity se = new StringEntity(textMsg, "utf-8");
        httpPost.setEntity(se);

        return getResult(httpPost);
    }

    public static String httpPostRequestFile(String url, Map<String, Object> headers, Map<String, Object> params, File file)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        MultipartEntityBuilder EntityBuilder = MultipartEntityBuilder.create();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            EntityBuilder.addPart(entry.getKey(), new StringBody(entry.getValue().toString()));
        }
        EntityBuilder.addBinaryBody("file", file);
        httpPost.setEntity(EntityBuilder.build());
        return getResult(httpPost);
    }

    public static String httpPutRequest(String url, Map<String, Object> headers, Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPut httpPut = new HttpPut(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPut.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPut.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getResult(httpPut);
    }

    public static HttpResponse doPost(String url, UrlEncodedFormEntity urlEncodedFormEntity) {
        CloseableHttpResponse response = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(urlEncodedFormEntity);
        try {
            response = httpClient.execute(httpPost);
            return response;
        } catch (Exception e) {
            logger.error("post: {}请求错误", url, e);
        }
        return response;
    }

    /**
     * 发起post请求
     *
     * @param jsonParam json参数字符串
     */
    public static HttpResponse doPost(String url, String jsonParam, Header header) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        if (StringUtils.isBlank(jsonParam)) {
            throw new IllegalArgumentException("参数错误");
        }
        HttpPost httpPost = new HttpPost(url);
        if (header != null) {
            httpPost.setHeader(header);
        }
        StringEntity encodedFormEntity = new StringEntity(jsonParam, Charset.forName("UTF-8"));
        httpPost.addHeader("Content-type", "application/json; charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(encodedFormEntity);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            return response;
        } catch (Exception e) {
            logger.error("{}请求错误", url, e);
        }
        return response;
    }

    /**
     * 发起post请求
     *
     * @param jsonParam json参数字符串
     */
    public static HttpResponse doPost(String url, String jsonParam, Map<String,String> headers) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        if (StringUtils.isBlank(jsonParam)) {
            throw new IllegalArgumentException("参数错误");
        }
        HttpPost httpPost = new HttpPost(url);
        for (Map.Entry<String, String> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        StringEntity encodedFormEntity = new StringEntity(jsonParam, CharsetUtils.get("UTF-8"));
        httpPost.setEntity(encodedFormEntity);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            return response;
        } catch (Exception e) {
            logger.error("{}请求错误", url, e);
        }
        return response;
    }

    /**
     * Post请求
     */
    public static String doPost(String url, Map<String, String> params, String charset)
            throws IOException {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        List<NameValuePair> pairs = getNameValuePair(params);
        HttpPost httpPost = new HttpPost(url);
        if (pairs != null && pairs.size() > 0) {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    /**
     * Get请求
     */
    public static String doGet(String url, Map<String, Object> headers, String params) {
        try {
            URIBuilder ub = new URIBuilder();
            ub.setPath(url);
            HashMap<String, Object> map = JacksonUtils.getObjectMapper()
                    .readValue(params, new TypeReference<HashMap<String, Object>>() {
                    });
            ArrayList<NameValuePair> pairs = covertParams2NVPS(map);
            ub.setParameters(pairs);

            HttpGet httpGet = new HttpGet(ub.build());
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
            return getResult(httpGet);
        } catch (Exception e) {
            logger.error("get url:{} error", url, e);
        }
        return null;
    }

    private static List<NameValuePair> getNameValuePair(Map<String, String> params) {
        List<NameValuePair> pairs = null;
        if (params != null && !params.isEmpty()) {
            pairs = new ArrayList<>(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value.toString()));
                }
            }
        }
        return pairs;
    }

    static class HttpRetryHandler implements HttpRequestRetryHandler {

        private static final int times = 5;//最多重试次数

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount > times) {
                return false;
            }
            if (exception instanceof ConnectTimeoutException
                    || !(exception instanceof SSLException) || exception instanceof NoHttpResponseException) {
                return true;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // 如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
                return true;
            }
            return false;
        }
    }


    /**
     * 发起一个Http请求,以字符串的方式返回结果
     *
     * @param url    请求地址
     * @param params Map类型的请求参数
     * @return
     */
    public static String doGetReq(String url, Map<String, String> params, HttpClient httpClient) {
        HttpResponse response;
        String content = "";
        try {
            //创建uri
            HttpGet request = new HttpGet(buildUriByParams(url, params));
            //设置超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONN_TIMEOUT)
                    .setConnectionRequestTimeout(CONN_TIMEOUT)
                    .setSocketTimeout(CONN_TIMEOUT).build();
            request.setConfig(requestConfig);
            addHeader(request);
            //执行请求
            response = httpClient.execute(request);
            //返回请求结果信息处理
            if (handleResponseStats(response) && response.getEntity() != null) {
                content = EntityUtils.toString(response.getEntity(), UTF_8);
            } else {
                logger.warn("GET返回失败,responseStatus={},request uri={}",
                        response == null ? null : response.getStatusLine(), request.getURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("GET请求接口异常,{}", e.getMessage());
            content = "";
        }
        return content;
    }

    /**
     * 发起一个Http请求,以字符串的方式返回结果
     *
     * @param url        请求地址
     * @param dataString 字符串类型的Post请求对象,可以是json等
     * @return
     */
    public static String doPostReq(String url, Map<String, String> params, String dataString, HttpClient httpClient) {
        String content = "";
        HttpResponse response;
        try {
            //创建uri
            URI uri = buildUriByParams(url, params);
            HttpPost request = new HttpPost(uri);
            //http请求参数设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONN_TIMEOUT)
                    .setConnectionRequestTimeout(CONN_TIMEOUT)
                    .setSocketTimeout(CONN_TIMEOUT)
                    .build();
            request.setConfig(requestConfig);
            request.setConfig(requestConfig);
            addHeader(request);
            //设置请求体
            if (StringUtils.isNotBlank(dataString)) {
                StringEntity stringEntity = new StringEntity(dataString, UTF_8);
                request.setEntity(stringEntity);
            }
            response = httpClient.execute(request);
            //返回结果数据处理
            if (handleResponseStats(response) && response.getEntity() != null) {
                content = EntityUtils.toString(response.getEntity(), UTF_8);
            } else {
                logger.warn("POST返回失败,uri={},responseStatus={},request content={}", uri, request == null ? null : response.getStatusLine(), dataString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("POST请求接口异常,{}", e.getMessage());
            content = "";
        }
        return content;
    }

    /**
     * 处理http请求返回状态码
     *
     * @param response
     * @return
     */
    private static boolean handleResponseStats(HttpResponse response) {
        if (response == null || response.getStatusLine() == null) {
            logger.error("response is invalid.");
            return false;
        } else {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    return true;
                case HttpStatus.SC_CREATED:
                    return true;
                case HttpStatus.SC_BAD_REQUEST:
                    return false;
                case HttpStatus.SC_UNAUTHORIZED:
                    return false;
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    return false;
                case HttpStatus.SC_BAD_GATEWAY:
                    return false;
                default:
                    return false;
            }
        }
    }

    private static void addHeader(HttpRequestBase request){
        request.addHeader("Accept", "application/json, text/plain, */*");
        request.addHeader("Content-Type", "application/json;charset=UTF-8");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");
        request.addHeader("cookie","i18n-prefs=USD; ubid-main=134-0812075-2342144; skin=noskin; x-wl-uid=1myTAbilfzvoyCTnEiBrSby38M9M83zDKVc1v30tDtBN8zU+xWuzHv8TyMCHntNhaH520TF7aofIy5bZ6xulz2Nm5lgSriDx1a5q60y1K/qFqelYH59CAqztB1iWQAGo7wj8kmCqTw7U=; lc-main=en_US; x-main=\"V5V7vV2Zrg77nMjipic9CCn9qRsGK?6B@2ago4WkBTZybcAjhR7TnMNu5HFJuZzn\"; at-main=Atza|IwEBII2TE2JXzTD2Tb8ea27zr3eg0ieiJVaXOnRraS4cHIeaNUGdYfv-TI6N-6QptHxNHldt0kHuJE6qQb1-Kdm53w0ULvDZ_-syXG2Pr1I4fWjzlxUk08lE6t7xVNyIx_2tnRPjqYe6V6-GHemCj00AqZovGUKCH0jGcl2OsF2cpjVaavApbCV6Yxl32GsTXjBx3wJ6EWv8LVbJcyRnuSpU7Yvpw1qz55mvBBHcomMh5otDgskpyHYkiZZqs9saXNFqxFAIf_xC321dzhLmAtI__Zzn3kbTIYd76NpFhSGFBboz4iQSW3MTG2EePqojnHx0gfJV3CAxolzJ3mY41V6pJ-DogX8ZjjfWhUd82n9I3zUBi8_lcu7bdJ33igZLU0haDP1E_BqHief7dKPEf_pNtqjk; sess-at-main=\"iJfNEmpziCw/+YSQost6vEdgXoIVWFNS/nlWavHjJ/k=\"; sst-main=Sst1|PQHUCkLyDFK0LgSrQyakUD1lC-xo-7VCB2MahRPZBI5ahtrTcs2O-2bfWXTBEynDg0A3XXWKJ-CpH9fRcjuKNVZohQzffIpAGMrZLbZx_WdyzvGyzmQtI-esSGa-OJA8TNX3iIc8epwlESonGEcjYhnkITQ1Mjw_BgB0LSIs-AFMVZcoJtFqjGkhUQUV8Om1ICFXNeY6X7PxaP_3N1qbJfb_8_axgS79L8aga2oXvYWfIXjYSDK82pSAUMYLbjDnk6Vgl7I6Doa4dhiWY1ezofoyU-WHV5rbORhH1CqiOGWp-gLoL0p3kHVkepkA-UhMM95NrWprZ2nD_Lt6wVEzwY9wbw; session-id-time=2082787201l; ubid-acbus=130-5339074-8083658; session-id=146-9111266-3287651; csm-hit=tb:Q2MQ5CZGWVXCCFM8K4W6+s-MFDXPH7SB88YE36WP38E|1589964439870&t:1589964439870&adb:adblk_no; s_sess=%20c_m%3DTyped%252FBookmarkedTyped%252FBookmarkedundefined%3B%20s_sq%3D%3B%20s_cc%3Dtrue%3B%20s_ppvl%3DUS%25253ASD%25253AFBA-main%252C7%252C7%252C722%252C1536%252C722%252C1536%252C864%252C1.25%252CL%3B%20s_ppv%3DUS%25253ASD%25253ASOA-pricing%252C70%252C70%252C10891%252C1536%252C722%252C1536%252C864%252C1.25%252CL%3B; s_pers=%20s_fid%3D3A69CBDFD80043DE-3A832D882E2C81E9%7C1747731962516%3B%20s_dl%3D1%7C1589967362517%3B%20gpv_page%3DUS%253ASD%253ASOA-pricing%7C1589967362520%3B%20s_ev15%3D%255B%255B%2527SDUSSOADirect%2527%252C%25271589965562523%2527%255D%255D%7C1747731962523%3B; session-token=\"sGxkGHyVEadmfJgpClCx8HXy4Yg7RCK2e3gKmZiabe1pwh17WCUUX+N1Bw7mOfaTFmLHJkuOMoMPCd7rm1XdozO1j2EhXOXoEoUZmMUh26fRiWELLu1XcKtlOmO/dzoASLRW6DzvvUch+aEVKcVHwa3tacZ+m8UaYRUOcbqn0UAX6PEB0WeFCDE9fs6ZuR+YFcArJ07oaYVYrPg/J70mqZyuZ/K/ZmYxoN9eJ7FKpTmA66+k82QSG8LNiXqtYke4y+Ie6Y6uyGWOCiixDYeD2w==\"; __Host-mons-selections=\"H4sIAAAAAAAAAKtWSi4tLsnPTS3yTFGyUkrMrcoz1EtMTs4vzSvRy0/OTNFzNDaw8A7wMQg0C/cK8VDSUQKqTc5IzCtB0gAT0svHojyxKDu1pCAnMTkVWUcBSK1ZuIWpZ2SgabiTobNSLQDiVn3BjAAAAA==\"");
    }

    /**
     * 根据输入的参数,对请求URI资源进行创建
     *
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     */
    private static URI buildUriByParams(String url, Map<String, String> params) throws URISyntaxException {
        //创建uri
        URIBuilder builder = new URIBuilder(url);
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addParameter(key, params.get(key));
            }
        }
        URI uri = builder.build();
        return uri;
    }

    public static void main(String[] args) {
        System.out.println(3 << 1);
    }
}