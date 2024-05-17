//package com.cqt.ivr.utils;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpStatus;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.utils.URIBuilder;
//import org.apache.http.config.Registry;
//import org.apache.http.config.RegistryBuilder;
//import org.apache.http.conn.socket.ConnectionSocketFactory;
//import org.apache.http.conn.socket.PlainConnectionSocketFactory;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URI;
//import java.net.URL;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class HttpClientUtil {
//    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
//
//
//    private static PoolingHttpClientConnectionManager cm = null;
//
//    private static CloseableHttpClient httpClient = null;
//
//    private static String SSLV3 = "SSLv3";
//
//    static {
//        //采用绕过验证的方式处理https请求
//        SSLContext sslcontext = null;
//        //主要是通过该方法createIgnoreVerifySSL
//        try {
//            sslcontext = createIgnoreVerifySSL();
//        } catch (NoSuchAlgorithmException e1) {
//            logger.error("创建SSL连接失败:", e1);
//        } catch (KeyManagementException e1) {
//            logger.error("KeyManagementException失败:", e1);
//        }
//
//        //设置协议http和https对应的处理socket链接工厂的对象
//        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
//                .register("http", PlainConnectionSocketFactory.INSTANCE)
//                .register("https", new SSLConnectionSocketFactory(sslcontext))
//                .build();
//        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//        HttpClients.custom().setConnectionManager(connManager);
//        httpClient = HttpClients.custom().setConnectionManager(connManager).build();    }
//
//    /**
//     * 绕过验证
//     *
//     * @return
//     * @throws NoSuchAlgorithmException
//     * @throws KeyManagementException
//     */
//    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
//        SSLContext sc = SSLContext.getInstance(SSLV3);
//
//        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
//        X509TrustManager trustManager = new X509TrustManager() {
//            @Override
//            public void checkClientTrusted(
//                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
//                    String paramString) throws CertificateException { }
//
//            @Override
//            public void checkServerTrusted(
//                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
//                    String paramString) throws CertificateException {
//            }
//
//            @Override
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//        };
//
//        sc.init(null, new TrustManager[] { trustManager }, null);
//        return sc;
//    }
//
//    public static String doPostJson(String url, String json, String LOG_TAG) {
//
//        // 创建Httpclient对象
//        //CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setSocketTimeout(5000)
//                    .setConnectTimeout(5000)
//                    .setConnectionRequestTimeout(5000)
//                    .build();// 设置请求和传输超时时间
//            // 创建Http Post请求
//            URL uri = new URL(url);
//            HttpPost httpPost = new HttpPost(uri.toURI());
//            httpPost.setConfig(requestConfig);
//            // 创建请求内容
//            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
//            httpPost.setEntity(entity);
//            // 执行http请求
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null){
//                    response.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//    public static CloseableHttpClient getHttpClient() {
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setConnectionManager(cm)
//                .build();
//        return httpClient;
//    }
//
//    public static String doGet(String url, Map<String, String> param) {
//        // 创建Httpclient对象
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        String resultString = "";
//        CloseableHttpResponse response = null;
//        try {
//            // 创建uri
//            URIBuilder builder = new URIBuilder(url);
//            if (param != null) {
//                for (String key : param.keySet()) {
//                    builder.addParameter(key, param.get(key));
//                }
//            }
//            URI uri = builder.build();
//            // 创建http GET请求
//            HttpGet httpGet = new HttpGet(uri);
//            // 执行请求
//            response = httpclient.execute(httpGet);
//            // 判断返回状态是否为200
//            if (response.getStatusLine().getStatusCode() == 200) {
//                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//                httpclient.close();
//            } catch (IOException e) {
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//    public static String doGet(String url) {
//        return doGet(url, null);
//    }
//
//    public static String doGets(String url) {
//        //byte[] bb = null;
//        String resultString = "";
//        // 创建Httpclient对象
//        CloseableHttpClient client = HttpClientUtil.getHttpClient();
//        // 发送get请求
//        HttpGet request = new HttpGet(url);
//        CloseableHttpResponse response = null;
//        // 浏览器表示
//        //request.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.6)");
//        // 传输的类型
//        //request.addHeader("Content-Type", "application/x-www-form-urlencoded");
//
//        // 设置请求和传输超时时间
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(5000)//数据传输过程中数据包之间间隔的最大时间
//                .setConnectTimeout(5000)//连接建立时间，三次握手完成时间
//                .setExpectContinueEnabled(true)//重点参数
//                .setConnectionRequestTimeout(5000)
//                .build();
//        request.setConfig(requestConfig);
//        try {
//            response = client.execute(request);
//            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//                System.out.println("请求失败");
//                return null;
//            }
//            HttpEntity resEntity = response.getEntity();
//
//            // 判断返回状态是否为200
//            if (response.getStatusLine().getStatusCode() == 200) {
//                resultString = EntityUtils.toString(resEntity, "UTF-8");
//            }
//            //InputStream  is = resEntity.getContent();
//            //bb= StreamTransUtil.input2byte(is);
//            //is.close();
//        } catch (UnsupportedEncodingException e) {
//            System.out.println(e.getMessage());
//            return null;
//        } catch (ClientProtocolException e) {
//            System.out.println(e.getMessage());
//            return null;
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        } finally {
//            if (response != null) {
//                try {//此处调优重点，多线程模式下可提高性能。
//                    EntityUtils.consume(response.getEntity());//此处高能，通过源码分析，由EntityUtils是否回收HttpEntity
//                    if (response != null) {
//                        response.close();
//                    }
//                } catch (IOException e) {
//                    System.out.println("关闭response失败:" + e);
//                }
//            }
//        }
//        return resultString;
//    }
//
//    public static String doPost(String url, Map<String, String> param) {
//        // 创建Httpclient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setSocketTimeout(3000)
//                    .setConnectTimeout(3000)
//                    .setConnectionRequestTimeout(3000)
//                    .build();// 设置请求和传输超时时间
//            // 创建Http Post请求
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.setConfig(requestConfig);
//            // 创建参数列表
//            if (param != null) {
//                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
//                for (String key : param.keySet()) {
//                    paramList.add(new BasicNameValuePair(key, param.get(key)));
//                }
//                // 模拟表单
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
//                httpPost.setEntity(entity);
//            }
//            // 执行http请求
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//    public static String doPost(String url) {
//        return doPost(url, null);
//    }
//
//    public static String doPostJson1(String url, String json) {
//        // 创建Httpclient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            URL uri = new URL(url);
//            // 创建Http Post请求
//            HttpPost httpPost = new HttpPost(uri.toURI());
//            // 创建请求内容
//            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
//            httpPost.setEntity(entity);
//            // 执行http请求
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//    public static String doPostStr(String url, String str) {
//        // 创建Httpclient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            URL uri = new URL(url);
//            // 创建Http Post请求
//            HttpPost httpPost = new HttpPost(uri.toString());
//            // 创建请求内容
//            StringEntity entity = new StringEntity(str);
//            httpPost.setEntity(entity);
//            // 执行http请求
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//    public static String doPostXml(String url, String xml) {
//        // 创建Httpclient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            URL uri = new URL(url);
//            // 创建Http Post请求
//            HttpPost httpPost = new HttpPost(uri.toString());
//            // 创建请求内容
//            StringEntity entity = new StringEntity(xml, ContentType.APPLICATION_XML);
//            httpPost.setEntity(entity);
//            // 执行http请求
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//    public static String doPostXml(String url, String xml, String endcoding) {
//        // 创建Httpclient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            URL uri = new URL(url);
//            // 创建Http Post请求
//            HttpPost httpPost = new HttpPost(uri.toString());
//            // 创建请求内容
//            StringEntity entity = new StringEntity(xml, endcoding);
//            httpPost.setEntity(entity);
//            // 执行http请求
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error(e.getMessage());
//            }
//        }
//        return resultString;
//    }
//
//}
