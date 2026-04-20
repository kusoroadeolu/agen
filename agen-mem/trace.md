MultiProcessor Chip 
Multiple Sequential Cores (A Private Data Cache), An LLC located near the main memory(this could be a field in the main memory class)


Caches communicate over an inter connection network. What do we mean by communicate here? 
From what I understand, an interconnection network links caches together. Basically each cache is an actor independent of others. So all communication is facilitated by the cache controller right?

Basically a core should never interact with a cache directly


```java
class Core {
    final CacheController cc = new PdcCacheController(); //The cache shouldn't be exposed to the core itself, all communication between the core and the cache happens through the cache controller
}
```


For our intercommunication network, we want to use a simple shared bus
Messages will be structured simply as is
```java
class Message {
    enum MessageType{
        GET_S //AckMessage to main memory, asking for a copy of an object, block till we get it
        GET_M //AckMessage to main memory asking for exclusive write access to an object
        INV //Invalidate ackMessage by a coherence protocol to other caches
    }
    
    MessageType mt;
    Data data; //Will be null for some ackMessage types
    CacheId sourceId;
    CacheId destinationId;
}
```

OK, so our final architecture should look like this. For now, we're skipping LLC since it really doesnt matter here
We have a Cache, its sequentiality is enforced by the cache controller. The cache controller is a dual machine. It can send or receive messages.

A cache controller will basically go in this order
```java
while(running){
    if(inboundQueue.poll() != null) //Handle ackMessage and block
        
}


//oubound queue path
while(running){
    if(in)    
}
```

For writes to a cache from the MP chip, we'll have something as so. MP_CHIP by random picks a random core's id. The Core hands it off to its cache controller, and blocks until it receives an ack from the cache controller.
The cache controller then requests a write request through the cache coherence protocol interface


To handle the case in which two caches can wait forever to ack each other's response, when we send a ackMessage to main memory asking for exclusive write access to an object or 

Core -> sends a load/store request to cache controller. How do we handle this.
Cache controller polls for messages from the inter conn bus, relays those messages to the pd cache


/*

Hey, great work overall it fits in pretty well with the rest of the library. Just a few things I want to point out before we merge

**DividerConfiguration**

1. The `dividerColor(String)` overload is only grabbing `[0]` from `ParserUtils.getAnsiCodes()`, so anything after the first code gets silently dropped. Something like `"red, bold"` would lose the bold entirely. Other configuration classes handle this by using a full `AnsiCode[]` rather than just getting the first elem
2. `MarkupParser` is missing from the builder. Per the spec it should be a required non-null field, and right now `Divider.get()` has `MarkupParser.DEFAULT` hardcoded so there’s no way to pass a custom parser through.
3. No Javadoc on the record or builder. Every other config class has docs so this should match that standard before merging.

**Divider**

1. The Javadoc mentions the rendered string is cached after the first `get()` call and invalidated on `title()`, but there’s no actual caching implementation.
2. `title.length()` doesn’t account for markup, something like `[red]Section Title` has a longer raw length than its visible length, so truncation kicks in too early. I usually use `MarkupParser.DEFAULT.originalString(String)` in other classes which strips both markup tags and  ansi to calculate the original string before any markup or ansi was applied.

None of this is major, mostly just consistency and a couple of correctness gaps. Let me know if I need to explain anything better

*/