package pl.rtprog.smtptransport.logic;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rtprog.smtptransport.delivery.DeliveryService;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Application logic service.
 *
 * @author Ryszard Trojnacki
 */
public class LogicService {
    private final static Logger log= LoggerFactory.getLogger(LogicService.class);

    private final Path data=Path.of("data");
    private final AtomicInteger id=new AtomicInteger(0);

    @Inject
    private DeliveryService delivery;

    private static class QueueItem {
        private final long created;
        private final Address to;
        private final String name;
        private final ArrayList<Path> parts=new ArrayList<>();
        private long updated;

        public QueueItem(Address to, String name) {
            this.to=to;
            this.created = System.currentTimeMillis();
            this.name=name;
        }

        @Override
        public String toString() {
            return "QueueItem{" +
                    "created=" + created +
                    ", to=" + to +
                    ", name='" + name + '\'' +
                    ", parts=" + parts +
                    ", updated=" + updated +
                    '}';
        }
    }

    private final ReentrantLock cs=new ReentrantLock();
    private final HashMap<Address, QueueItem> queue=new HashMap<>();

    private QueueItem registerContent(Address to, BodyPart content) throws IOException, MessagingException {
        var file=data.resolve(id.incrementAndGet()+"_"+content.getFileName());
        Files.createDirectories(data);
        try(var out= Files.newOutputStream(file)) {
            content.getInputStream().transferTo(out);
        }
        cs.lock();
        try {
            var item=queue.get(to);
            if(item==null) {
                item=new QueueItem(to, content.getFileName());
                queue.put(to, item);
            }
            item.parts.add(file);
            item.updated=System.nanoTime();
            return item;
        }finally {
            cs.unlock();
        }
    }

    /**
     * Method that registers incoming message from SMTP server/printer.
     * @param m received message
     */
    public void registerInput(MimeMessage m) {
        try {
            var to=m.getRecipients(Message.RecipientType.TO)[0];
            log.debug("Registering new message to: {}", Arrays.asList(m.getAllRecipients()));
            log.debug("Content: {}", m.getContent().getClass());
            if(m.getContent() instanceof Multipart) {
                var content=(Multipart)m.getContent();
                for(var i=0;i<content.getCount();++i) {
                    var part=content.getBodyPart(i);
                    log.debug("  Part {}: ({} {}) {}", i, part.getContentType(), part.getSize(), part.getFileName());
                    if(part.getContentType().contains("application/pdf")) {
                        var item=registerContent(to, part);
                        log.debug("  Registered to queue item: {}", item);
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processItem(QueueItem item) throws IOException {
        if(item.parts.size()>1) {
            var fn=data.resolve(id.incrementAndGet()+"f_"+item.name);
            log.debug("Merging items: {}", item);
            PDFMergerUtility merger=new PDFMergerUtility();
            merger.setDestinationFileName(fn.toString());
            for(var part: item.parts) merger.addSource(part.toFile());
            merger.mergeDocuments(null);
            for(var part: item.parts) Files.delete(part);
            delivery.deliver(item.to, item.name, fn);
        } else if(item.parts.size()==1) {
            log.debug("Processing single page item: {}", item);
            delivery.deliver(item.to, item.name, item.parts.get(0));
//            Files.move(item.parts.get(0), Path.of(item.name));
        }
    }

    public void processPending() {
        var completed=new ArrayList<QueueItem>();
        var limit=System.nanoTime()- TimeUnit.SECONDS.toNanos(45);
        cs.lock();
        try {
            var it=queue.values().iterator();
            while (it.hasNext()) {
                var item=it.next();
                if(item.updated<limit) {
                    it.remove();
                    completed.add(item);
                }
            }
        }finally {
            cs.unlock();
        }
        if(completed.isEmpty()) return;
        log.debug("Processing completed: {}", completed);
        for(var item: completed) {
            try {
                processItem(item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
