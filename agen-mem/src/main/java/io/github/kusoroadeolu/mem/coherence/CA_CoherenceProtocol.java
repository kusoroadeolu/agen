package io.github.kusoroadeolu.mem.coherence;

public class CA_CoherenceProtocol implements CacheCoherenceProtocol{
    public static final Object NONE = new Object();

    @Override
    public Object readRequest(MemoryLocation location) {
       boolean ack = MultiProcessorChip.chip().memory().startAck();
       if (ack){
           var object = MultiProcessorChip.chip().memory().read(location);
           AckMessage ackMessage = new AckMessage(location);
           if(InterConnectionNetwork.getInstance().offer(ackMessage)){
               try {
                   ackMessage.waitAcknowledgement();
               } catch (InterruptedException e) {
                   return NONE;
               }

               InterConnectionNetwork.getInstance().poll(); //Remove the ack message
               MultiProcessorChip.chip().memory().endAck();
               return object;
           }

           return NONE;

       }

       return NONE;
    }

    @Override
    public boolean writeRequest(MemoryLocation location, Object value) {
        boolean ack = MultiProcessorChip.chip().memory().startAck();
        if (ack){
            MultiProcessorChip.chip().memory().write(location, value);
            AckMessage ackMessage = new AckMessage(location);
            if(InterConnectionNetwork.getInstance().offer(ackMessage)){
                try {
                    ackMessage.waitAcknowledgement();
                } catch (InterruptedException e) {
                    return false;
                }

                InterConnectionNetwork.getInstance().poll(); //Remove the ack message
                MultiProcessorChip.chip().memory().endAck();
                return true;
            }

            return false;
        }

        return false;
    }
}
