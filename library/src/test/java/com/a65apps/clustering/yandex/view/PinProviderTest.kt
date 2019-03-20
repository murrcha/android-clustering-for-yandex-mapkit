package com.a65apps.clustering.yandex.view

import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.AnimatedImageProvider
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.mock
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PinProviderTest : Spek({
    describe("YandexPinProvider tests") {
        val imageProvider = mock(ImageProvider::class.java)
        val viewProvider = mock(ViewProvider::class.java)
        val animatedImageProvider = mock(AnimatedImageProvider::class.java)
        val iconStyle = mock(IconStyle::class.java)

        describe("Create YandexPinProvider from ImageProvider") {
            val pinProvider = YandexPinProvider.from(imageProvider)
            it("returns ImageProvider when call provider()") {
                val any = pinProvider.provider()
                assertThat(any).isInstanceOf(ImageProvider::class.java)
                assertThat(pinProvider.style).isNull()
            }
        }

        describe("Create YandexPinProvider from ImageProvider with IconStyle") {
            val pinProvider = YandexPinProvider.from(imageProvider, iconStyle)
            it("returns ImageProvider when call provider()") {
                val any = pinProvider.provider()
                assertThat(any).isInstanceOf(ImageProvider::class.java)
                assertThat(pinProvider.style).isEqualTo(iconStyle)
            }
        }

        describe("Create YandexPinProvider from ViewProvider") {
            val pinProvider = YandexPinProvider.from(viewProvider)
            it("returns ViewProvider when call provider()") {
                val any = pinProvider.provider()
                assertThat(any).isInstanceOf(ViewProvider::class.java)
                assertThat(pinProvider.style).isNull()
            }
        }

        describe("Create YandexPinProvider from ViewProvider with IconStyle") {
            val pinProvider = YandexPinProvider.from(viewProvider, iconStyle)
            it("returns ViewProvider when call provider()") {
                val any = pinProvider.provider()
                assertThat(any).isInstanceOf(ViewProvider::class.java)
                assertThat(pinProvider.style).isEqualTo(iconStyle)
            }
        }

        describe("Create YandexPinProvider from AnimatedImageProvider with IconStyle") {
            val pinProvider = YandexPinProvider.from(animatedImageProvider, iconStyle)
            it("returns AnimatedImageProvider when call provider()") {
                val any = pinProvider.provider()
                assertThat(any).isInstanceOf(AnimatedImageProvider::class.java)
                assertThat(pinProvider.style).isEqualTo(iconStyle)
            }
        }
    }
})
