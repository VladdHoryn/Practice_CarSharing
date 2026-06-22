package org.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CarTest {

    private Car car;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setBrand("Audi");
        car.setModel("A6");
        car.setYear(2022);
        car.setStatus(CarStatus.AVAILABLE);
        car.setCarClass(CarClass.BUSINESS);
        car.setPricePerDay(100.0f);
        car.setUserId(10L);
    }

    @Test
    void shouldSetAndGetId() {
        car.setId(5L);
        assertEquals(5L, car.getId());
    }

    @Test
    void shouldSetAndGetBrand() {
        car.setBrand("BMW");
        assertEquals("BMW", car.getBrand());
    }

    @Test
    void shouldSetAndGetModel() {
        car.setModel("M5");
        assertEquals("M5", car.getModel());
    }

    @Test
    void shouldSetAndGetYear() {
        car.setYear(2020);
        assertEquals(2020, car.getYear());
    }

    @Test
    void shouldSetAndGetPricePerDay() {
        car.setPricePerDay(200.0f);
        assertThat(car.getPricePerDay()).isEqualTo(200.0f);
    }

    @Test
    void shouldSetAndGetUserId() {
        car.setUserId(42L);
        assertEquals(42L, car.getUserId());
    }

    @Test
    void shouldSetAndGetCarClass() {
        car.setCarClass(CarClass.ECONOMY);
        assertEquals(CarClass.ECONOMY, car.getCarClass());
    }

    @Test
    void shouldSetAndGetStatus() {
        car.setStatus(CarStatus.RENTED);
        assertEquals(CarStatus.RENTED, car.getStatus());
    }

    @ParameterizedTest
    @EnumSource(CarStatus.class)
    void shouldSupportAllPossibleStatuses(CarStatus status) {
        car.setStatus(status);
        assertEquals(status, car.getStatus());
    }

    @ParameterizedTest
    @EnumSource(CarClass.class)
    void shouldSupportAllPossibleCarClasses(CarClass carClass) {
        car.setCarClass(carClass);
        assertEquals(carClass, car.getCarClass());
    }

    @Test
    void entityShouldBeEqualToItself() {
        assertEquals(car, car);
    }

    @Test
    void entityShouldNotBeEqualToNull() {
        assertNotEquals(null, car);
    }

    @Test
    void toStringShouldExecuteWithoutExceptions() {
        String result = car.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Nested
    @DisplayName("rent()")
    class RentTests {

        @Test
        @DisplayName("успішна аренда доступного авто")
        void shouldRentAvailableCar() {
            car.setStatus(CarStatus.AVAILABLE);
            car.rent(99L);
            assertThat(car.getStatus()).isEqualTo(CarStatus.RENTED);
            assertThat(car.getUserId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо авто не AVAILABLE")
        void shouldThrowWhenCarNotAvailable() {
            car.setStatus(CarStatus.RENTED);
            assertThatThrownBy(() -> car.rent(99L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not available for rent");
        }

        @Test
        @DisplayName("кидає IllegalStateException для авто в MAINTENANCE")
        void shouldThrowWhenCarInMaintenance() {
            car.setStatus(CarStatus.MAINTENANCE);
            assertThatThrownBy(() -> car.rent(99L)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("кидає IllegalStateException для UNCONFIRMED авто")
        void shouldThrowWhenCarUnconfirmed() {
            car.setStatus(CarStatus.UNCONFIRMED);
            assertThatThrownBy(() -> car.rent(99L)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("кидає IllegalArgumentException, якщо renterId null")
        void shouldThrowWhenRenterIdNull() {
            car.setStatus(CarStatus.AVAILABLE);
            assertThatThrownBy(() -> car.rent(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Renter id cannot be null");
        }
    }

    @Nested
    @DisplayName("returnFromRent()")
    class ReturnFromRentTests {

        @Test
        @DisplayName("успішне повернення орендованого авто")
        void shouldReturnRentedCar() {
            car.setStatus(CarStatus.RENTED);
            car.returnFromRent();
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
            assertThat(car.getUserId()).isNull();
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо авто не RENTED")
        void shouldThrowWhenCarNotRented() {
            car.setStatus(CarStatus.AVAILABLE);
            assertThatThrownBy(() -> car.returnFromRent())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only rented cars can be returned");
        }

        @Test
        @DisplayName("кидає IllegalStateException для MAINTENANCE авто")
        void shouldThrowWhenCarInMaintenance() {
            car.setStatus(CarStatus.MAINTENANCE);
            assertThatThrownBy(() -> car.returnFromRent())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("sendToMaintenance()")
    class SendToMaintenanceTests {

        @Test
        @DisplayName("успішно відправляє AVAILABLE авто на ТО")
        void shouldSendAvailableCarToMaintenance() {
            car.setStatus(CarStatus.AVAILABLE);
            car.sendToMaintenance();
            assertThat(car.getStatus()).isEqualTo(CarStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("успішно відправляє UNCONFIRMED авто на ТО")
        void shouldSendUnconfirmedCarToMaintenance() {
            car.setStatus(CarStatus.UNCONFIRMED);
            car.sendToMaintenance();
            assertThat(car.getStatus()).isEqualTo(CarStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо авто RENTED")
        void shouldThrowWhenCarIsRented() {
            car.setStatus(CarStatus.RENTED);
            assertThatThrownBy(() -> car.sendToMaintenance())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot send rented car to maintenance");
        }
    }

    @Nested
    @DisplayName("completeMaintenance()")
    class CompleteMaintenanceTests {

        @Test
        @DisplayName("успішно завершує ТО")
        void shouldCompleteMaintenanceSuccessfully() {
            car.setStatus(CarStatus.MAINTENANCE);
            car.completeMaintenance();
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо авто не в MAINTENANCE")
        void shouldThrowWhenCarNotInMaintenance() {
            car.setStatus(CarStatus.AVAILABLE);
            assertThatThrownBy(() -> car.completeMaintenance())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Car is not in maintenance");
        }

        @Test
        @DisplayName("кидає IllegalStateException для RENTED авто")
        void shouldThrowWhenCarIsRented() {
            car.setStatus(CarStatus.RENTED);
            assertThatThrownBy(() -> car.completeMaintenance())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("confirmCar()")
    class ConfirmCarTests {

        @Test
        @DisplayName("успішно підтверджує UNCONFIRMED авто")
        void shouldConfirmUnconfirmedCar() {
            car.setStatus(CarStatus.UNCONFIRMED);
            car.confirmCar();
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо авто не UNCONFIRMED")
        void shouldThrowWhenCarNotUnconfirmed() {
            car.setStatus(CarStatus.AVAILABLE);
            assertThatThrownBy(() -> car.confirmCar())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Car is not unconfirmed");
        }

        @Test
        @DisplayName("кидає IllegalStateException для RENTED авто")
        void shouldThrowWhenCarIsRented() {
            car.setStatus(CarStatus.RENTED);
            assertThatThrownBy(() -> car.confirmCar()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("cancelCar()")
    class CancelCarTests {

        @Test
        @DisplayName("успішно скасовує UNCONFIRMED авто")
        void shouldCancelUnconfirmedCar() {
            car.setStatus(CarStatus.UNCONFIRMED);
            car.cancelCar();
            assertThat(car.getStatus()).isEqualTo(CarStatus.CANCELED);
        }

        @Test
        @DisplayName("кидає IllegalStateException, якщо авто не UNCONFIRMED")
        void shouldThrowWhenCarNotUnconfirmed() {
            car.setStatus(CarStatus.AVAILABLE);
            assertThatThrownBy(() -> car.cancelCar())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Car is not unconfirmed");
        }

        @Test
        @DisplayName("кидає IllegalStateException для RENTED авто")
        void shouldThrowWhenCarIsRented() {
            car.setStatus(CarStatus.RENTED);
            assertThatThrownBy(() -> car.cancelCar()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("isAvailableForRent()")
    class IsAvailableForRentTests {

        @Test
        @DisplayName("повертає true для AVAILABLE авто")
        void shouldReturnTrueWhenAvailable() {
            car.setStatus(CarStatus.AVAILABLE);
            assertThat(car.isAvailableForRent()).isTrue();
        }

        @Test
        @DisplayName("повертає false для RENTED авто")
        void shouldReturnFalseWhenRented() {
            car.setStatus(CarStatus.RENTED);
            assertThat(car.isAvailableForRent()).isFalse();
        }

        @Test
        @DisplayName("повертає false для MAINTENANCE авто")
        void shouldReturnFalseWhenInMaintenance() {
            car.setStatus(CarStatus.MAINTENANCE);
            assertThat(car.isAvailableForRent()).isFalse();
        }

        @Test
        @DisplayName("повертає false для UNCONFIRMED авто")
        void shouldReturnFalseWhenUnconfirmed() {
            car.setStatus(CarStatus.UNCONFIRMED);
            assertThat(car.isAvailableForRent()).isFalse();
        }

        @Test
        @DisplayName("повертає false для CANCELED авто")
        void shouldReturnFalseWhenCanceled() {
            car.setStatus(CarStatus.CANCELED);
            assertThat(car.isAvailableForRent()).isFalse();
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTests {

        @Test
        @DisplayName("змінює статус на будь-який переданий")
        void shouldChangeStatus() {
            car.setStatus(CarStatus.AVAILABLE);
            car.changeStatus(CarStatus.RENTED);
            assertThat(car.getStatus()).isEqualTo(CarStatus.RENTED);
        }

        @Test
        @DisplayName("дозволяє встановити той самий статус")
        void shouldAllowSameStatus() {
            car.setStatus(CarStatus.AVAILABLE);
            car.changeStatus(CarStatus.AVAILABLE);
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @ParameterizedTest
        @EnumSource(CarStatus.class)
        @DisplayName("змінює статус на будь-який з CarStatus")
        void shouldChangeToAnyStatus(CarStatus status) {
            car.changeStatus(status);
            assertThat(car.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("Управління зображеннями")
    class ImageTests {

        private CarImage buildImage(Long id) {
            CarImage img = new CarImage();
            img.setId(id);
            img.setFileName("img" + id + ".jpg");
            img.setContentType("image/jpeg");
            img.setImageData(new byte[] {1, 2, 3});
            img.setFileSize(3L);
            img.setMain(false);
            return img;
        }

        @Test
        @DisplayName("перше додане зображення стає головним")
        void firstAddedImageBecomesMain() {
            CarImage img = buildImage(1L);
            car.addImage(img);
            assertThat(img.isMain()).isTrue();
            assertThat(car.getImages()).hasSize(1);
        }

        @Test
        @DisplayName("друге зображення не стає головним автоматично")
        void secondImageIsNotMain() {
            CarImage img1 = buildImage(1L);
            CarImage img2 = buildImage(2L);
            car.addImage(img1);
            car.addImage(img2);
            assertThat(img1.isMain()).isTrue();
            assertThat(img2.isMain()).isFalse();
        }

        @Test
        @DisplayName("getMainImage повертає головне зображення")
        void getMainImageReturnsMain() {
            CarImage img1 = buildImage(1L);
            CarImage img2 = buildImage(2L);
            car.addImage(img1);
            car.addImage(img2);
            assertThat(car.getMainImage()).isEqualTo(img1);
        }

        @Test
        @DisplayName("getMainImage повертає null, якщо зображень немає")
        void getMainImageReturnsNullWhenNoImages() {
            assertThat(car.getMainImage()).isNull();
        }

        @Test
        @DisplayName("setMainImage призначає головним вказане зображення")
        void setMainImageChangesMain() {
            CarImage img1 = buildImage(1L);
            CarImage img2 = buildImage(2L);
            car.addImage(img1);
            car.addImage(img2);
            car.setMainImage(2L);
            assertThat(img1.isMain()).isFalse();
            assertThat(img2.isMain()).isTrue();
        }

        @Test
        @DisplayName("removeImage видаляє зображення за ID")
        void removeImageRemovesById() {
            CarImage img1 = buildImage(1L);
            CarImage img2 = buildImage(2L);
            car.addImage(img1);
            car.addImage(img2);
            boolean removed = car.removeImage(1L);
            assertThat(removed).isTrue();
            assertThat(car.getImages()).hasSize(1);
        }

        @Test
        @DisplayName("removeImage повертає false для неіснуючого ID")
        void removeImageReturnsFalseForUnknownId() {
            CarImage img = buildImage(1L);
            car.addImage(img);
            boolean removed = car.removeImage(999L);
            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("видалення головного зображення передає статус головного наступному")
        void removeMainImageTransfersMainToAnother() {
            CarImage img1 = buildImage(1L);
            CarImage img2 = buildImage(2L);
            car.addImage(img1);
            car.addImage(img2);
            car.removeImage(1L);
            assertThat(img2.isMain()).isTrue();
        }

        @Test
        @DisplayName("addImage встановлює зворотній зв'язок з car")
        void addImageSetsCar() {
            CarImage img = buildImage(1L);
            car.addImage(img);
            assertThat(img.getCar()).isEqualTo(car);
        }

        @Test
        @DisplayName("removeImage повертає false якщо список порожній")
        void removeImageReturnsFalseWhenEmpty() {
            boolean removed = car.removeImage(1L);
            assertThat(removed).isFalse();
        }
    }
}
