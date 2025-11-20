package org.example

class ListTutorial {

    static List init() {
        List emptyList = []
        assert emptyList instanceof ArrayList
        List<Integer> intList = [1, 2, 3]
        List<Float> floatList = [1.1, 2.2, 3.3]
        //по умолчанию создается ArrayList
        List<Integer> linkedList = [] as LinkedList
        //создание списка с копированием другого
        List<Integer> copyList = new ArrayList<>(intList)
        //часто при вызове api-методов возвращаются неизменяемые списки. в них нельзя добавлять или удалять элементы
        List immutableIntList = Collections.unmodifiableList(intList)
        //для того, чтобы этот список можно было менять - нужно создать новый список на основе первого
        List mutableList = new ArrayList(immutableIntList)
        mutableList.clear()
        //в функциях, которые возвращают список, вместо null всегда лучше возвращать пустую коллекцию. например:
        List emptyImmutableList = Collections.emptyList()
        return emptyImmutableList;
    }

    static void addItems() {
        List list = [1/*0*/, 2/*1*/, 3/*2*/]
        list << 9
        assert list.contains(9)
        assert list.size() == 4
        list.add(5)
        list[5] = 6
        assert list.size() == 6
        list += [7, 8]
        assert list.size() == 8
    }

    static void updateItems() {
        List list = [1, 2, 3]
        list[1] = 4
        assert list == [1, 4, 3]
        list.set(2, 5)
        assert list == [1, 4, 5]
    }

    static void removeItems() {
        List list = [1, 2, 3, 4, 5, 5, 6, 6, 7]
        //удаление по индексу
        list.remove(3)
        assert list == [1, 2, 3, 5, 5, 6, 6, 7]
        //удаление первого вхождения элемента в список
        list.removeElement(6)
        assert list == [1, 2, 3, 5, 5, 6, 7]
        //удаление всех таких элементов из списка
        list = list - 5
        assert list == [1, 2, 3, 6, 7]
    }

    static void iterate() {
        List list = [1, 2, 3, 4, 5, 5, 6, 6, 7]
        list.each {
            print(it)
        }
        list.eachWithIndex { int value, int index ->
            println("${index + ':' + value}")
        }
    }

    static void each(List<Integer> list, Closure closure) {
        for (int i = 0; i < list.size(); i++) {
            int element = list.get(i);
            closure(element)
        }
    }

    static void filtering() {
        List list = [2, 1, 3, 4, 5, 6, 76]
        //поиск первого элемента, удовлетворяющего условию
        assert list.find { it > 3 } == 4
        //поиск всех  элементов, подходящих под условие
        assert list.findAll { it > 3 } == [4, 5, 6, 76]
        list = [1, 2, 2, 3]
        assert list.unique() == [1, 2, 3]
        assert list.every { it > 0 }
        assert list.any { it > 1 }
        if (list.every { it > 2 }) {
            println("1111")
        } else {
            println("2222")
        }
    }

    static void sorting() {
        List list = [1, 4, 3, 2]
        assert list.sort() == [1, 2, 3, 4]

        Comparator mc = { a, b -> a == b ? 0 : a < b ? 1 : -1 }
        /*if (a == b) {
            return 0
        } else if (a < b) {
            return 1
        } else {
            return -1
        }*/
        list = [1, 2, 1, 0]
        list.sort(mc)
        assert list == [2, 1, 1, 0]
        assert list.max() == 2
        assert list.min() == 0
    }

    static void collect() {
        List list = ["Kay", "Henry", "Justin", "Tom"]
        list.collect { "Hi, " + it }
                .findAll { it.size() > 7 }
                .each { println(it) }
        println(list.join(" | "))
    }

}
