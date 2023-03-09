package web;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");
    private final int POOLSIZE = 64;
    private int port;
    private ExecutorService executorService = Executors.newFixedThreadPool(POOLSIZE);
    public static Map<String, Map<String, Handler>> handlerMap = new HashMap<>();
    private final Handler handlerDefault = (r, o) -> {
        try {
            var filePath = Path.of(".", "public", r.getPath());
            var mimeType = Files.probeContentType(filePath);//"application/octet-stream" "text/plain"
            var length = Files.size(filePath);
            o.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, o);
            o.flush();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    };

    public Server(int port) {
        this.port = port;
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlerMap.containsKey(method)) {
            handlerMap.get(method).put(path, handler);
        } else {
            Map<String, Handler> localMap = new HashMap<>();
            localMap.put(path, handler);
            handlerMap.put(method, localMap);
        }
    }


    public void start() {
        System.out.println("Starting server at " + port + " port.");
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> connect(socket));
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }

    private void connect(Socket socket) {
        try (socket;
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                socket.close();
                return;
            }

            URIBuilder uriBuilder = new URIBuilder(parts[1]);

            Request request = new Request(parts[0], uriBuilder);

            if (handlerMap.get(request.getMethod()).containsKey(request.getPath())) {
                Handler handler = handlerMap.get(request.getMethod()).get(request.getPath());
                handler.handle(request, out);
            }
            if (validPaths.contains(request.getPath())) {
                handlerDefault.handle(request, out);
            } else {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
