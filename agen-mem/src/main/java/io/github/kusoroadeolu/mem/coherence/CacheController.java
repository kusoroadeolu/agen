package io.github.kusoroadeolu.mem.coherence;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import static io.github.kusoroadeolu.mem.coherence.CA_CoherenceProtocol.NONE;

public class CacheController {
    private final BlockingQueue<CoreMessage> inBoundRequests;
    private final PrivateDataCache cache;
    private final Thread thread;

    public CacheController() {
        this.inBoundRequests = new LinkedBlockingQueue<>();
        this.cache = new PrivateDataCache();
        this.thread = startThread();
    }

    private Thread startThread() {
        return Thread.ofPlatform().start(() -> {
           while (true){
               AckMessage ackMessage;
               if ((ackMessage = InterConnectionNetwork.getInstance().peek()) != null){
                    var object = InterConnectionNetwork.getInstance().readFromMainMemory(ackMessage.location());
                    cache.write(ackMessage.location(), object);
                    ackMessage.acknowledge();
               }

               CoreMessage coreMessage;
               if ((coreMessage = inBoundRequests.poll()) != null){
                   switch (coreMessage){
                       case CoreMessage.ReadRequest rq -> {
                           var object = MultiProcessorChip.chip().protocol().readRequest(rq.location);
                           if(object != NONE){
                               cache.write(rq.location, object);
                           }

                           rq.o().complete(object);
                       }
                       case CoreMessage.WriteRequest wq -> {
                           var ack = MultiProcessorChip.chip().protocol().writeRequest(wq.location, wq.o);
                           if (ack){
                               cache.write(wq.location, wq.o);
                           }
                           wq.b().complete(ack);
                       }
                   }
               }
           }
        });
    }

    public void write(MemoryLocation location, Object o, CompletableFuture<Boolean> b){
        inBoundRequests.add(new CoreMessage.WriteRequest(location, o, b));
    }

    public void read(MemoryLocation location, CompletableFuture<Object> o){
        inBoundRequests.add(new CoreMessage.ReadRequest(location, o));
    }


    sealed interface CoreMessage{
        record ReadRequest(MemoryLocation location, CompletableFuture<Object> o) implements CoreMessage{

        }

        record WriteRequest(MemoryLocation location, Object o, CompletableFuture<Boolean> b) implements  CoreMessage{

        }
    }
}
