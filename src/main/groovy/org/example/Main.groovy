package org.example

import org.cryptacular.StreamException

/**
 * https://groovy-lang.org/syntax.html#_keywords
 */
static void main(String[] args) {
    CarClass car = new CarClass();
    Integer engine = car.getEngine()
    System.out.println(engine * 2);

    Integer a = 4;
    Integer b = 5;
    Integer c = a > b ? a * b : a + b
    String s = null
    String d = s ?: "123"
    String p = "Hello, \"wonderful\" world!"
    System.out.println(p)
    File f
    try {
        f = readFile("fdfsd")
    }
    catch (IOException e) {
        System.out.println("Не удалось прочитать файл по причине: " + f.name)
    }
    catch (StreamException e) {
        System.out.println("Не удалось прочитать файл, скорее всего, битый диск")
    } finally {
       // ioStream.close();
    }
    List<String> list = getStrings()
    list.clear()

}

/**
 * Ищет в массиве чисел максимальное число
 * @param arr массив чисел
 * @return максимальное число
 */
static int findMax(int[] arr) {
    if (arr) {
        int max = Integer.MIN_VALUE
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max
    } else {
        return 0
    }
}

static File readFile(String path) throws IOException, StreamException {
    //.....
    return new File()
}

/**
 * ArrayList [1, 4, 6, 7]
 * LinkedList -> i1 -> i2 -> i3 -> i4
 * Set<String>
 * @return
 */
static List<String> getStrings() {
    List<String> s = ["a", "b"]
    //{"key" : "value"}
    Map<String, Integer> m = ["somekey": 5, "anotherkey": 6]
}