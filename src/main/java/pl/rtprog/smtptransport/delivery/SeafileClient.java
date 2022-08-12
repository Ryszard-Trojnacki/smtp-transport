package pl.rtprog.smtptransport.delivery;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rtprog.smtptransport.delivery.seafile.AuthResponse;
import pl.rtprog.smtptransport.delivery.seafile.DefaultRepo;
import pl.rtprog.smtptransport.delivery.seafile.DirectoryDetail;
import pl.rtprog.smtptransport.delivery.seafile.LibraryInfo;
import pl.rtprog.smtptransport.utils.JsonBodyHandler;
import pl.rtprog.smtptransport.utils.MultipartContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Service for supporting transport to Seafile.
 *
 * @author Ryszard Trojnacki
 */
class SeafileClient {
    private final static Logger log= LoggerFactory.getLogger(SeafileClient.class);

    private final static String HEADER_AUTH="Authorization";
    private final static String HEADER_ACCEPT="Accept";
    private final static String ACCEPT_VALUE="application/json";
    private final static String HEADER_CHARSET="charset";
    private final static String CHARSET_VALUE="utf-8";
    private final static String HEADER_INDENT="indent";
    private final static String INDENT_VALUE="4";
    private final static String HEADER_CONTENT_TYPE="Content-Type";
    private final static String CONTENT_TYPE_FORM="application/x-www-form-urlencoded";
    private final static String CONTENT_TYPE_MULTIPART="multipart/form-data";

    private final static String PING="/api2/ping/";
    private final static String AUTH="/api2/auth-token/";
    private final static String AUTH_PING="/api2/auth/ping/";
    private final static String DEFAULT_REPO="/api2/default-repo/";
    private final static String REPOS="/api2/repos/";

    private final static MessageFormat GET_DETAILS=new MessageFormat("/api/2.1/repos/{0}/dir/detail/?path={1}");
    private final static MessageFormat CREATE_DIRECTORY=new MessageFormat("/api2/repos/{0}/dir/?p={1}");

    private final static MessageFormat UPLOAD_FILE=new MessageFormat("/api2/repos/{0}/upload-link/?p={1}");

    private final HttpClient client;

    private final String server;

    private String token;

    public SeafileClient(String server) {
        this.server=server;
        this.client=HttpClient.newHttpClient();
    }

    private URI uri(String path) {
        try {
            return new URI(server+path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean ping() {
        var request=HttpRequest.newBuilder()
                .uri(uri(PING))
                .GET()
                .build();
        try {
            var res=client.send(request, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode()!=200) log.debug("Ping {} result {}: {}", request.uri(), res.statusCode(), res.body());
            return res.statusCode()==200 && "\"pong\"".equals(res.body());
        } catch (IOException|InterruptedException e) {
            return false;
        }
    }

    public boolean authPing() throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(AUTH_PING))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .GET().build();
        var res=client.send(request, HttpResponse.BodyHandlers.ofString());
        if(res.statusCode()!=200) log.debug("AuthPing {} result {}: {}", request.uri(), res.statusCode(), res.body());
        return res.statusCode()==200 && "\"pong\"".equals(res.body());
    }

    private String urlEncode(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    public boolean auth(String username, String password) throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(AUTH))
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM)
                .POST(HttpRequest.BodyPublishers.ofString(
                        "username="+ urlEncode(username)
                        +"&password="+urlEncode(password)
                ))
                .build();
        var res=client.send(request, JsonBodyHandler.of(AuthResponse.class));
        if(res.statusCode()!=200) {
            log.debug("Auth {}: {}", res.uri(), res.statusCode());
            return false;
        }
        var token=res.body().get().token;
        if(token==null) return false;
        this.token="Token "+token;
        return true;
    }

    public String defaultRepo() throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(DEFAULT_REPO))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        var res=client.send(request, JsonBodyHandler.of(DefaultRepo.class));
        if(res.statusCode()!=200) {
            log.debug("DefaultRepo {}: {}", request.uri(), res.statusCode());
            return null;
        }
        var body=res.body().get();
//        log.debug("Response: {}", body);
        return body.repoId;
    }

    private final static TypeReference<List<LibraryInfo>> LIST_REPOS_TYPE=new TypeReference<List<LibraryInfo>>() {};

    public List<LibraryInfo> listRepos() throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(REPOS))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        var res=client.send(request, JsonBodyHandler.of(LIST_REPOS_TYPE));
        if(res.statusCode()!=200) {
            log.debug("DefaultRepo {}: {}", request.uri(), res.statusCode());
            return null;
        }
        var body=res.body().get();
        log.debug("Response: {}", body);
        return body;
    }

    public DirectoryDetail getDir(String repoId, String path) throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(GET_DETAILS.format(new String[] { repoId, urlEncode(path) })))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        var res=client.send(request, JsonBodyHandler.of(DirectoryDetail.class));
        if(res.statusCode()!=200) return null;
        return res.body().get();
    }

    public boolean createDir(String repoId, String path) throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(CREATE_DIRECTORY.format(new String[] { repoId, urlEncode(path) })))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        var res=client.send(request, JsonBodyHandler.of(String.class));
        return res.statusCode()==201;
    }

    public String uploadFile(String repoId, String path) throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(UPLOAD_FILE.format(new String[] { repoId, urlEncode(path) })))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        var res=client.send(request, JsonBodyHandler.of(String.class));
        if(res.statusCode()!=200) {
            log.debug("Upload file {}: {}", request.uri(), res.statusCode());
            return null;
        }
        var url=res.body().get();

        return url;
    }

    public boolean sendFile(String url, String path, Path file, String filename) throws URISyntaxException, IOException, InterruptedException {
        var info=new HashMap<String, Object>();
        info.put("file", new MultipartContent.FileInfo(file, filename));
        info.put("parent_dir", path);
//        info.put("relative_path", path);
        info.put("replace", "0");

        var data=new MultipartContent(info);

//        for(var el: data) {
//            log.info("Part: {}", new String(el));
//        }

        var request=HttpRequest.newBuilder()
                .uri(new URI(url))
                .header(HEADER_AUTH, token)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_MULTIPART+";boundary="+data.getBoundary())
                .POST(HttpRequest.BodyPublishers.ofByteArrays(data))
                .build();
        var res=client.send(request, HttpResponse.BodyHandlers.ofString());
        if(res.statusCode()!=200) {
            log.warn("Error uploading file {}: {} {}", res.uri(), res.statusCode(), res.body());
            return false;
        }
        return true;
    }
}
