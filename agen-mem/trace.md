write -> random core chosen -> write request to a mem location sent to core controller(synchronized)
-> write request to a mem location made by core through cache protocol interface -> locked object for mem location gotten -> try hold write lock -> if success -> write to mem location -> cache protocol then ensures write visibility, in this case (force all cores to read from main memory sequentially before returning(no lock is held here)) -> release mem location write lock -> return true
if failed to hold write lock -> return false


read -> random core chosen -> read request to a mem location sent to core controller(synchronized) -> read request to a mem location made by core through cache protocol interface try hold read lock -> if success ->  cache protocol then ensures write visibility, in this case (force all cores to read from main memory sequentially before returning(no lock is held here)) -> release mem location read lock -> return true
if failed to hold read lock -> return false