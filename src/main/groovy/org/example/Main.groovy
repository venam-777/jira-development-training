package org.example

/**
 * https://groovy-lang.org/syntax.html#_keywords
 */
static void main(String[] args) {
    MapTutorial.iterate()
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