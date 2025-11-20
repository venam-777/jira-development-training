package org.example

class MapTutorial {

    static void init() {
        //по умолчанию создается HashMap
        Map emptyMap = [:]
        assert emptyMap instanceof HashMap
        Map linkedMap = [:] as LinkedHashMap
        Map map = [name: "Jerry", age: 42, city: "New York"]
        Map copyMap = new HashMap(map)
        emptyMap = Collections.emptyMap()
        Map immutable = Collections.unmodifiableMap(map)
        assert map.get("name") == "Jerry"
        assert map.get("city") == "New York"
        String city = map.get("city")
        assert city == "New York"
        assert map.name == "Jerry"
        map.clear()
    }

    static void addItems() {
        def map = [name: "Jerry"]
        map.age = 35
        map["age"] = 36
        map.put("age", 37)
        assert map.age == 37
    }

    static void removeItems() {
        Map map = [name: "Jerry", age: 42, city: "New York"]
        map.remove("name")
        map.minus(["age": 43])
        assert map.age == 42
        map.removeAll { it.key == "age" }
        map.removeAll { Map.Entry it -> it.key == "age" }
        map.removeAll { key, val -> key == "age" }
        assert !map.containsKey("age")
        map.name = "Tom"
        map.retainAll { it.value == "Tom" }
        assert map.size() == 1 && map.name
    }

    static void iterate() {
        Map map = [name: "Jerry", age: 42, city: "New York"]
        map.each { key, val -> println("${key}:${val}") }
        map.each { println("${it.key}:${it.value}") }
        map.eachWithIndex { Map.Entry<String, Serializable> entry, int index -> println("index: ${index}, key: ${entry.key}, value: ${entry.value}") }
    }

    static void filtering() {
        Map map = [name: "Jerry", age: 42, city: "New York"]
        assert map.find { it.key == "name" }?.value == "Jerry"
        assert map.findAll { it.value != null }.size() == 3
        assert map.findAll { it.value == 42 } == [age: 42]
        assert map.every { it.value != null }
        assert map.any { it.key == "name" }
    }

    static void collect() {
        Map map = [
                1: [name: "Jerry", age: 42, city: "New York"],
                2: [name: "Long", age: 25, city: "New York"],
                3: [name: "Dustin", age: 29, city: "New York"],
                4: [name: "Dustin", age: 34, city: "New York"]]
        def names = map.collect { entry -> entry.value.name }
        assert names == ["Jerry", "Long", "Dustin", "Dustin"]
        def uniqueNames = map.collect([] as HashSet) { entry -> entry.value.name }
        assert uniqueNames == ["Jerry", "Long", "Dustin"] as Set
        def below30Names = map.findAll { it.value.age < 30 }.collect { key, value -> value.name }
        assert below30Names == ["Long", "Dustin"]
    }

    static void sorting() {
        def map = [ab: 20, a: 40, cb: 11, ba: 93]
        def naturalOrder = map.sort()
        assert naturalOrder.collect { it.key } == ["a", "ab", "ba", "cb"]
        def reverseOrder = map.sort({ k1, k2 -> k2.compareTo(k1) } as Comparator)
        assert reverseOrder.collect { it.key } == ["cb", "ba", "ab", "a"]
    }

}
