package pl.rtprog.smtptransport.delivery;

import pl.rtprog.smtptransport.utils.JsonBodyHandler;
import pl.rtprog.smtptransport.delivery.seafile.AuthResponse;
import pl.rtprog.smtptransport.delivery.seafile.DefaultRepo;
import pl.rtprog.smtptransport.delivery.seafile.DirectoryDetail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * Service for supporting transport to Seafile.
 *
 * @author Ryszard Trojnacki
 */
class SeafileClient {
    private final static String HEADER_AUTH="Authorization";
    private final static String HEADER_ACCEPT="Accept";
    private final static String ACCEPT_VALUE="application/json";
    private final static String HEADER_CHARSET="charset";
    private final static String CHARSET_VALUE="utf-8";
    private final static String HEADER_INDENT="indent";
    private final static String INDENT_VALUE="4";

    private final static String PING="/api2/ping/";
    private final static String AUTH="/api2/auth-token/";
    private final static String AUTH_PING="/api2/auth/ping/";
    private final static String DEFAULT_REPO="/api2/default-repo/";

    private final static MessageFormat GET_DETAILS=new MessageFormat("/api/2.1/repos/{0}/dir/detail/?path={1}");
    private final static MessageFormat CREATE_DIRECTORY=new MessageFormat("/api2/repos/{0}/dir/?p={1}");

    private final static MessageFormat UPLOAD_FILE=new MessageFormat("/api2/repos/{0}/upload-link/p={1}");

    private HttpClient client;

    private String server;

    private String token;

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
            return res.statusCode()==400 && "pong".equals(res.body());
        } catch (IOException|InterruptedException e) {
            return false;
        }
    }

    public boolean authPing() throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(AUTH_PING))
                .header(HEADER_AUTH, token)
                .GET().build();
        var res=client.send(request, HttpResponse.BodyHandlers.ofString());
        return res.statusCode()==200 && "pong".equals(res.body());
    }

    private String urlEncode(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    private String auth(String username, String password) throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(AUTH))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "username="+ urlEncode(username)
                        +"&password="+urlEncode(password)
                ))
                .build();
        var res=client.send(request, JsonBodyHandler.of(AuthResponse.class));
        if(res.statusCode()!=200) return null;
        return res.body().get().token;
    }

    private String defaultRepo() throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder()
                .uri(uri(DEFAULT_REPO))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        var res=client.send(request, JsonBodyHandler.of(DefaultRepo.class));
        if(res.statusCode()!=200) return null;
        return res.body().get().repoId;
    }

    private DirectoryDetail getDir(String repoId, String path) throws IOException, InterruptedException {
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

    private boolean createDir(String repoId, String path) throws IOException, InterruptedException {
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

    private String uploadFile(String repoId, String path) {
        var request=HttpRequest.newBuilder()
                .uri(uri(UPLOAD_FILE.format(new String[] { repoId, urlEncode(path) })))
                .header(HEADER_AUTH, token)
                .header(HEADER_ACCEPT, ACCEPT_VALUE)
                .header(HEADER_CHARSET, CHARSET_VALUE)
                .header(HEADER_INDENT, INDENT_VALUE)
                .GET()
                .build();
        return null;
    }
}
