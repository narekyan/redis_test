import redis.clients.jedis.JedisCluster

class RedisClusterIntList(
    private val jedisCluster: JedisCluster,
    private val redisKey: String
) : MutableList<Int> {

    override val size: Int
        get() = jedisCluster.hlen(redisKey).toInt()

    override fun isEmpty(): Boolean = size == 0

    override fun contains(element: Int): Boolean {
        return getAll().contains(element)
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        return getAll().containsAll(elements)
    }

    override fun get(index: Int): Int {
        return jedisCluster.hget(redisKey, index.toString())?.toIntOrNull()
            ?: throw IndexOutOfBoundsException("Index $index out of bounds")
    }

    override fun indexOf(element: Int): Int {
        return getAll().indexOf(element)
    }

    override fun lastIndexOf(element: Int): Int {
        return getAll().lastIndexOf(element)
    }

    override fun iterator(): MutableIterator<Int> {
        return getAll().toMutableList().iterator()
    }

    override fun listIterator(): MutableListIterator<Int> {
        return getAll().toMutableList().listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<Int> {
        return getAll().toMutableList().listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Int> {
        return getAll().subList(fromIndex, toIndex).toMutableList()
    }

    override fun add(element: Int): Boolean {
        val currentSize = size
        jedisCluster.hset(redisKey, currentSize.toString(), element.toString())
        return true
    }

    override fun add(index: Int, element: Int) {
        val list = getAll()
        if (index < 0 || index > list.size) {
            throw IndexOutOfBoundsException("Index $index out of bounds")
        }

        val updatedList = list.toMutableList()
        updatedList.add(index, element)

        // Rebuild the hash
        clear()
        updatedList.forEachIndexed { i, value ->
            jedisCluster.hset(redisKey, i.toString(), value.toString())
        }
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        val currentSize = size
        elements.forEachIndexed { index, element ->
            jedisCluster.hset(redisKey, (currentSize + index).toString(), element.toString())
        }
        return true
    }

    override fun addAll(index: Int, elements: Collection<Int>): Boolean {
        val list = getAll()
        if (index < 0 || index > list.size) {
            throw IndexOutOfBoundsException("Index $index out of bounds")
        }

        val updatedList = list.toMutableList()
        updatedList.addAll(index, elements)

        // Rebuild the hash
        clear()
        updatedList.forEachIndexed { i, value ->
            jedisCluster.hset(redisKey, i.toString(), value.toString())
        }
        return true
    }

    override fun remove(element: Int): Boolean {
        val index = indexOf(element)
        return if (index >= 0) {
            removeAt(index)
            true
        } else {
            false
        }
    }

    override fun removeAt(index: Int): Int {
        val value = get(index)
        val list = getAll().toMutableList()

        list.removeAt(index)

        // Rebuild the hash
        clear()
        list.forEachIndexed { i, updatedValue ->
            jedisCluster.hset(redisKey, i.toString(), updatedValue.toString())
        }

        return value
    }

    override fun retainAll(elements: Collection<Int>): Boolean {
        val toRemove = getAll().filter { it !in elements }
        toRemove.forEach { remove(it) }
        return toRemove.isNotEmpty()
    }

    override fun removeAll(elements: Collection<Int>): Boolean {
        elements.forEach { remove(it) }
        return true
    }

    override fun clear() {
        jedisCluster.del(redisKey)
    }

    override fun set(index: Int, element: Int): Int {
        val oldValue = get(index)
        jedisCluster.hset(redisKey, index.toString(), element.toString())
        return oldValue
    }

    public fun getAll(): List<Int> {
        return jedisCluster.hgetAll(redisKey)
            .toSortedMap(compareBy { it.toInt() }) 
            .values
            .mapNotNull { it.toIntOrNull() }
    }
}