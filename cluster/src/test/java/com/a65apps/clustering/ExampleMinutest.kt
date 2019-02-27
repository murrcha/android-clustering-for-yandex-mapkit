package com.a65apps.clustering

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.assertj.core.api.Assertions.assertThat

class ExampleMinutest : JUnit5Minutests {
    fun testsNoFixture() = rootContext<Unit> {
        test("1 + 1 = 2") {
            assertThat(1 + 1).isEqualTo(2)
        }

        test("6 * 6 = 36") {
            assertThat(6 * 6).isEqualTo(36)
        }
    }

    fun testsWithFixture() = rootContext<ArrayList<Int>> {
        fixture { ArrayList() }

        test("is empty") {
            assertThat(this.isEmpty()).isTrue()
        }

        context("after add 1") {
            modifyFixture {
                parentFixture.add(1)
            }

            test("is not empty") {
                assertThat(this.isEmpty()).isFalse()
            }

            test("size = 1") {
                assertThat(this.size).isEqualTo(1)
            }
        }
    }

    fun testsCalculator() = rootContext<Calculator> {
        fixture { Calculator() }

        test("fun add: 2 + 2 = 4") {
            assertThat(this.add(2, 2)).isEqualTo(4)
        }
    }

    class Calculator {
        fun add(first: Int, second: Int) : Int = first + second
    }
}
