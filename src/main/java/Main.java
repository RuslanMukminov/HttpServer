import web.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(9999);

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/classic.html", (r, o) -> {
                    try {
                        var filePath = Path.of(".", "public", r.getPath());
                        var mimeType = Files.probeContentType(filePath);
                        var template = Files.readString(filePath);
                        var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        o.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        o.write(content);
                        o.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
        });

        server.start();
    }
}
