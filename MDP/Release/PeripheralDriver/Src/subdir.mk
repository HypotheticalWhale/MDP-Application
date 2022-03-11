################################################################################
# Automatically-generated file. Do not edit!
# Toolchain: GNU Tools for STM32 (9-2020-q2-update)
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../PeripheralDriver/Src/oled.c 

OBJS += \
./PeripheralDriver/Src/oled.o 

C_DEPS += \
./PeripheralDriver/Src/oled.d 


# Each subdirectory must supply rules for building sources it contributes
PeripheralDriver/Src/%.o: ../PeripheralDriver/Src/%.c PeripheralDriver/Src/subdir.mk
	arm-none-eabi-gcc "$<" -mcpu=cortex-m4 -std=gnu11 -DUSE_HAL_DRIVER -DSTM32F407xx -c -I"C:/Users/nahzr/STM32CubeIDE/workspace_1.8.0/MDP/PeripheralDriver/Inc" -I../Core/Inc -I../Drivers/STM32F4xx_HAL_Driver/Inc -I../Drivers/STM32F4xx_HAL_Driver/Inc/Legacy -I../Drivers/CMSIS/Device/ST/STM32F4xx/Include -I../Drivers/CMSIS/Include -I../Middlewares/Third_Party/FreeRTOS/Source/include -I../Middlewares/Third_Party/FreeRTOS/Source/CMSIS_RTOS_V2 -I../Middlewares/Third_Party/FreeRTOS/Source/portable/GCC/ARM_CM4F -Os -ffunction-sections -fdata-sections -Wall -fstack-usage -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" --specs=nano.specs -mfloat-abi=hard -mthumb -o "$@"

clean: clean-PeripheralDriver-2f-Src

clean-PeripheralDriver-2f-Src:
	-$(RM) ./PeripheralDriver/Src/oled.d ./PeripheralDriver/Src/oled.o

.PHONY: clean-PeripheralDriver-2f-Src

