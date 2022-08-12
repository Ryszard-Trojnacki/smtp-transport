package pl.rtprog.smtptransport.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Helper class for MimeMultipart for encoding.
 *
 * @author Ryszard Trojnacki
 */
public class MultipartContent implements Iterable<byte[]> {
    private final static String boundary=new BigInteger(320, new Random()).toString();
    private final static byte[] boundarySep=("--"+boundary+"\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);
    private final static byte[] line="\r\n".getBytes(StandardCharsets.UTF_8);

    private final List<Pair<String, Object>> data;

    public MultipartContent(Map<String, Object> data) {
        this.data = data.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public String getBoundary() {
        return boundary;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new Iterator<>() {
            int item=0;

            @Override
            public boolean hasNext() {
                return item<data.size()*4;
            }

            @Override
            public byte[] next() {
                var elem=data.get(item/4);
                var part=item%4;
                ++item;
                switch (part) {
                    case 0: return boundarySep;
                    case 1:
                        if(elem.getValue() instanceof Path) {
                            return ('"'+elem.getKey()+"\"; filename=\""+((Path)elem.getValue()).getFileName()
                                    +"\"\r\nContent-Type: application/pdf\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                        } else {
                            return ('"' + elem.getKey() + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                        }
                    case 2:
                        if(elem.getValue() instanceof Path) {
                            try {
                                return Files.readAllBytes((Path) elem.getValue());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return elem.getValue().toString().getBytes(StandardCharsets.UTF_8);
                        }
                    case 3:
                        return line;
                    default:
                        throw new IllegalStateException();
                }

            }
        };
    }
}
