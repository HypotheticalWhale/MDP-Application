/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2022 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */
/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "cmsis_os.h"
#include "oled.h"
#include "math.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */

/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
ADC_HandleTypeDef hadc1;

TIM_HandleTypeDef htim1;
TIM_HandleTypeDef htim2;
TIM_HandleTypeDef htim3;
TIM_HandleTypeDef htim4;
TIM_HandleTypeDef htim8;

UART_HandleTypeDef huart3;


/* Definitions for defaultTask */
osThreadId_t defaultTaskHandle;
const osThreadAttr_t defaultTask_attributes = {
  .name = "defaultTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityNormal,
};
/* Definitions for MotorTask */
osThreadId_t MotorTaskHandle;
const osThreadAttr_t MotorTask_attributes = {
  .name = "MotorTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* Definitions for Encoder */
osThreadId_t EncoderHandle;
const osThreadAttr_t Encoder_attributes = {
  .name = "Encoder",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* Definitions for showtask */
osThreadId_t showtaskHandle;
const osThreadAttr_t showtask_attributes = {
  .name = "showtask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* USER CODE BEGIN PV */

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_TIM8_Init(void);
static void MX_TIM2_Init(void);
static void MX_TIM1_Init(void);
static void MX_USART3_UART_Init(void);
static void MX_TIM3_Init(void);
static void MX_TIM4_Init(void);
static void MX_ADC1_Init(void);
void StartDefaultTask(void *argument);
void motors(void *argument);
void encoder(void *argument);
void show(void *argument);
void navigate();


/* USER CODE BEGIN PFP */

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int speed1,speed2,y,stop;
//speed1=1530;speed2=810;//home
speed1=1500;speed2=995; //lab
//speed1=1700;speed2=950; //redtiles1
//speed1=1700;speed2=1250; //REDTILES2
y=0;
stop=0;
uint32_t IC_Val1 = 0;
uint32_t IC_Val2 = 0;
uint32_t Difference = 0;
uint8_t Is_First_Captured = 0;  // is the first value captured ?
uint8_t Distance  = 0;
uint8_t aRxBuffer[4];
uint8_t instruction[400];
uint32_t Dist=0;
uint32_t gO=8000;
uint32_t move,dist;
uint32_t x;

#define TRIG_PIN TRIG_Pin
#define TRIG_PORT GPIOE
int main(void)
{
  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */

  /* MCU Configuration--------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_TIM8_Init();
  MX_TIM2_Init();
  MX_TIM1_Init();
  MX_USART3_UART_Init();
  MX_TIM3_Init();
  MX_TIM4_Init();
  MX_ADC1_Init();
  OLED_Init();
  //HAL_UART_Receive_IT(&huart3,*(uint8_t *)aRxBuffer,10);
  /* USER CODE BEGIN 2 */

  /* USER CODE END 2 */

  /* Init scheduler */
  osKernelInitialize();

  /* USER CODE BEGIN RTOS_MUTEX */
  /* add mutexes, ... */
  /* USER CODE END RTOS_MUTEX */

  /* USER CODE BEGIN RTOS_SEMAPHORES */
  /* add semaphores, ... */
  /* USER CODE END RTOS_SEMAPHORES */

  /* USER CODE BEGIN RTOS_TIMERS */
  /* start timers, add new ones, ... */
  /* USER CODE END RTOS_TIMERS */

  /* USER CODE BEGIN RTOS_QUEUES */
  /* add queues, ... */
  /* USER CODE END RTOS_QUEUES */

  /* Create the thread(s) */
  /* creation of defaultTask */
  defaultTaskHandle = osThreadNew(StartDefaultTask, NULL, &defaultTask_attributes);

  /* creation of MotorTask */
  MotorTaskHandle = osThreadNew(motors, NULL, &MotorTask_attributes);

  /* creation of Encoder */
  EncoderHandle = osThreadNew(encoder, NULL, &Encoder_attributes);

  /* creation of showtask */
  showtaskHandle = osThreadNew(show, NULL, &showtask_attributes);

  /* USER CODE BEGIN RTOS_THREADS */
  /* add threads, ... */
  /* USER CODE END RTOS_THREADS */

  /* USER CODE BEGIN RTOS_EVENTS */
  /* add events, ... */
  /* USER CODE END RTOS_EVENTS */

  /* Start scheduler */
  osKernelStart();

  /* We should never get here as control is now taken by the scheduler */
  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  while (1)
  {
    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
  }
  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};

  /** Configure the main internal regulator output voltage
  */
  __HAL_RCC_PWR_CLK_ENABLE();
  __HAL_PWR_VOLTAGESCALING_CONFIG(PWR_REGULATOR_VOLTAGE_SCALE1);
  /** Initializes the RCC Oscillators according to the specified parameters
  * in the RCC_OscInitTypeDef structure.
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB buses clocks
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    Error_Handler();
  }
}

/**
  * @brief ADC1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_ADC1_Init(void)
{

  /* USER CODE BEGIN ADC1_Init 0 */

  /* USER CODE END ADC1_Init 0 */

  ADC_ChannelConfTypeDef sConfig = {0};

  /* USER CODE BEGIN ADC1_Init 1 */

  /* USER CODE END ADC1_Init 1 */
  /** Configure the global features of the ADC (Clock, Resolution, Data Alignment and number of conversion)
  */
  hadc1.Instance = ADC1;
  hadc1.Init.ClockPrescaler = ADC_CLOCK_SYNC_PCLK_DIV2;
  hadc1.Init.Resolution = ADC_RESOLUTION_12B;
  hadc1.Init.ScanConvMode = ENABLE;
  hadc1.Init.ContinuousConvMode = ENABLE;
  hadc1.Init.DiscontinuousConvMode = DISABLE;
  hadc1.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
  hadc1.Init.ExternalTrigConv = ADC_SOFTWARE_START;
  hadc1.Init.DataAlign = ADC_DATAALIGN_RIGHT;
  hadc1.Init.NbrOfConversion = 1;
  hadc1.Init.DMAContinuousRequests = DISABLE;
  hadc1.Init.EOCSelection = ADC_EOC_SINGLE_CONV;
  if (HAL_ADC_Init(&hadc1) != HAL_OK)
  {
    Error_Handler();
  }
  /** Configure for the selected ADC regular channel its corresponding rank in the sequencer and its sample time.
  */
  sConfig.Channel = ADC_CHANNEL_10;
  sConfig.Rank = 1;
  sConfig.SamplingTime = ADC_SAMPLETIME_3CYCLES;
  if (HAL_ADC_ConfigChannel(&hadc1, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN ADC1_Init 2 */

  /* USER CODE END ADC1_Init 2 */

}

/**
  * @brief TIM1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM1_Init(void)
{

  /* USER CODE BEGIN TIM1_Init 0 */

  /* USER CODE END TIM1_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_OC_InitTypeDef sConfigOC = {0};
  TIM_BreakDeadTimeConfigTypeDef sBreakDeadTimeConfig = {0};

  /* USER CODE BEGIN TIM1_Init 1 */

  /* USER CODE END TIM1_Init 1 */
  htim1.Instance = TIM1;
  htim1.Init.Prescaler = 320;
  htim1.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim1.Init.Period = 2000;
  htim1.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim1.Init.RepetitionCounter = 0;
  htim1.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_ENABLE;
  if (HAL_TIM_Base_Init(&htim1) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim1, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_Init(&htim1) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim1, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigOC.OCMode = TIM_OCMODE_PWM1;
  sConfigOC.Pulse = 0;
  sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;
  sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;
  sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;
  sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET;
  if (HAL_TIM_PWM_ConfigChannel(&htim1, &sConfigOC, TIM_CHANNEL_4) != HAL_OK)
  {
    Error_Handler();
  }
  sBreakDeadTimeConfig.OffStateRunMode = TIM_OSSR_DISABLE;
  sBreakDeadTimeConfig.OffStateIDLEMode = TIM_OSSI_DISABLE;
  sBreakDeadTimeConfig.LockLevel = TIM_LOCKLEVEL_OFF;
  sBreakDeadTimeConfig.DeadTime = 0;
  sBreakDeadTimeConfig.BreakState = TIM_BREAK_DISABLE;
  sBreakDeadTimeConfig.BreakPolarity = TIM_BREAKPOLARITY_HIGH;
  sBreakDeadTimeConfig.AutomaticOutput = TIM_AUTOMATICOUTPUT_DISABLE;
  if (HAL_TIMEx_ConfigBreakDeadTime(&htim1, &sBreakDeadTimeConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM1_Init 2 */

  /* USER CODE END TIM1_Init 2 */
  HAL_TIM_MspPostInit(&htim1);

}
/**
  * @brief TIM2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM2_Init(void)
{

  /* USER CODE BEGIN TIM2_Init 0 */

  /* USER CODE END TIM2_Init 0 */

  TIM_Encoder_InitTypeDef sConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};

  /* USER CODE BEGIN TIM2_Init 1 */

  /* USER CODE END TIM2_Init 1 */
  htim2.Instance = TIM2;
  htim2.Init.Prescaler = 0;
  htim2.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim2.Init.Period = 65535;
  htim2.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim2.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  sConfig.EncoderMode = TIM_ENCODERMODE_TI12;
  sConfig.IC1Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC1Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC1Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC1Filter = 10;
  sConfig.IC2Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC2Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC2Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC2Filter = 10;
  if (HAL_TIM_Encoder_Init(&htim2, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim2, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM2_Init 2 */

  /* USER CODE END TIM2_Init 2 */

}

/**
  * @brief TIM3 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM3_Init(void)
{

  /* USER CODE BEGIN TIM3_Init 0 */

  /* USER CODE END TIM3_Init 0 */

  TIM_Encoder_InitTypeDef sConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};

  /* USER CODE BEGIN TIM3_Init 1 */

  /* USER CODE END TIM3_Init 1 */
  htim3.Instance = TIM3;
  htim3.Init.Prescaler = 0;
  htim3.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim3.Init.Period = 65535;
  htim3.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim3.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  sConfig.EncoderMode = TIM_ENCODERMODE_TI12;
  sConfig.IC1Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC1Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC1Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC1Filter = 10;
  sConfig.IC2Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC2Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC2Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC2Filter = 10;
  if (HAL_TIM_Encoder_Init(&htim3, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim3, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM3_Init 2 */

  /* USER CODE END TIM3_Init 2 */

}

/**
  * @brief TIM4 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM4_Init(void)
{

  /* USER CODE BEGIN TIM4_Init 0 */

  /* USER CODE END TIM4_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_IC_InitTypeDef sConfigIC = {0};

  /* USER CODE BEGIN TIM4_Init 1 */

  /* USER CODE END TIM4_Init 1 */
  htim4.Instance = TIM4;
  htim4.Init.Prescaler = 16;
  htim4.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim4.Init.Period = 65535;
  htim4.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim4.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim4) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim4, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_IC_Init(&htim4) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim4, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigIC.ICPolarity = TIM_INPUTCHANNELPOLARITY_RISING;
  sConfigIC.ICSelection = TIM_ICSELECTION_DIRECTTI;
  sConfigIC.ICPrescaler = TIM_ICPSC_DIV1;
  sConfigIC.ICFilter = 0;
  if (HAL_TIM_IC_ConfigChannel(&htim4, &sConfigIC, TIM_CHANNEL_1) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM4_Init 2 */

  /* USER CODE END TIM4_Init 2 */

}

/**
  * @brief TIM8 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM8_Init(void)
{

  /* USER CODE BEGIN TIM8_Init 0 */

  /* USER CODE END TIM8_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_OC_InitTypeDef sConfigOC = {0};
  TIM_BreakDeadTimeConfigTypeDef sBreakDeadTimeConfig = {0};

  /* USER CODE BEGIN TIM8_Init 1 */

  /* USER CODE END TIM8_Init 1 */
  htim8.Instance = TIM8;
  htim8.Init.Prescaler = 0;
  htim8.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim8.Init.Period = 7199;
  htim8.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim8.Init.RepetitionCounter = 0;
  htim8.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim8) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim8, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_Init(&htim8) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim8, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigOC.OCMode = TIM_OCMODE_PWM1;
  sConfigOC.Pulse = 0;
  sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;
  sConfigOC.OCNPolarity = TIM_OCNPOLARITY_HIGH;
  sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;
  sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;
  sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET;
  if (HAL_TIM_PWM_ConfigChannel(&htim8, &sConfigOC, TIM_CHANNEL_1) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_ConfigChannel(&htim8, &sConfigOC, TIM_CHANNEL_2) != HAL_OK)
  {
    Error_Handler();
  }
  sBreakDeadTimeConfig.OffStateRunMode = TIM_OSSR_DISABLE;
  sBreakDeadTimeConfig.OffStateIDLEMode = TIM_OSSI_DISABLE;
  sBreakDeadTimeConfig.LockLevel = TIM_LOCKLEVEL_OFF;
  sBreakDeadTimeConfig.DeadTime = 0;
  sBreakDeadTimeConfig.BreakState = TIM_BREAK_DISABLE;
  sBreakDeadTimeConfig.BreakPolarity = TIM_BREAKPOLARITY_HIGH;
  sBreakDeadTimeConfig.AutomaticOutput = TIM_AUTOMATICOUTPUT_DISABLE;
  if (HAL_TIMEx_ConfigBreakDeadTime(&htim8, &sBreakDeadTimeConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM8_Init 2 */

  /* USER CODE END TIM8_Init 2 */

}

/**
  * @brief USART3 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART3_UART_Init(void)
{

  /* USER CODE BEGIN USART3_Init 0 */

  /* USER CODE END USART3_Init 0 */

  /* USER CODE BEGIN USART3_Init 1 */

  /* USER CODE END USART3_Init 1 */
  huart3.Instance = USART3;
  huart3.Init.BaudRate = 115200;
  huart3.Init.WordLength = UART_WORDLENGTH_8B;
  huart3.Init.StopBits = UART_STOPBITS_1;
  huart3.Init.Parity = UART_PARITY_NONE;
  huart3.Init.Mode = UART_MODE_TX_RX;
  huart3.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart3.Init.OverSampling = UART_OVERSAMPLING_16;
  if (HAL_UART_Init(&huart3) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART3_Init 2 */

  /* USER CODE END USART3_Init 2 */

}

/**
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{
  GPIO_InitTypeDef GPIO_InitStruct = {0};

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOE_CLK_ENABLE();
  __HAL_RCC_GPIOC_CLK_ENABLE();
  __HAL_RCC_GPIOA_CLK_ENABLE();
  __HAL_RCC_GPIOD_CLK_ENABLE();
  __HAL_RCC_GPIOB_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOE, OLED_SCL_Pin|OLED_SDA_Pin|OLED_RST_Pin|OLED_DC_Pin
                          |LED3_Pin|TRIG_Pin, GPIO_PIN_RESET);

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOA, AIN2_Pin|AIN1_Pin|BIN1_Pin|BIN2_Pin, GPIO_PIN_RESET);

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(Bluetooth_TX_GPIO_Port, Bluetooth_TX_Pin, GPIO_PIN_RESET);

  /*Configure GPIO pins : OLED_SCL_Pin OLED_SDA_Pin OLED_RST_Pin OLED_DC_Pin
                           LED3_Pin TRIG_Pin */
  GPIO_InitStruct.Pin = OLED_SCL_Pin|OLED_SDA_Pin|OLED_RST_Pin|OLED_DC_Pin
                          |LED3_Pin|TRIG_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

  /*Configure GPIO pins : AIN2_Pin AIN1_Pin BIN1_Pin BIN2_Pin */
  GPIO_InitStruct.Pin = AIN2_Pin|AIN1_Pin|BIN1_Pin|BIN2_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

  /*Configure GPIO pins : ECHO_Pin Bluetooth_RX_Pin */
  GPIO_InitStruct.Pin = ECHO_Pin|Bluetooth_RX_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_INPUT;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

  /*Configure GPIO pin : Bluetooth_TX_Pin */
  GPIO_InitStruct.Pin = Bluetooth_TX_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(Bluetooth_TX_GPIO_Port, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */

/* USER CODE END 4 */

/* USER CODE BEGIN Header_StartDefaultTask */
/**
  * @brief  Function implementing the defaultTask thread.
  * @param  argument: Not used
  * @retval None
  */
/* USER CODE END Header_StartDefaultTask */
void StartDefaultTask(void *argument)
{
  /* USER CODE BEGIN 5 */
  /* Infinite loop */
	uint8_t ch;
	uint8_t hello[20];

	uint32_t deLay;
	int a;
	int b=0;
  for(;;)
  {
	 HAL_UART_Receive_IT(&huart3,(uint8_t *) aRxBuffer,4);
	// sprintf(hello,"%s",aRxBuffer);
	// OLED_ShowString(10,40,hello);
	 osDelay(200);


  }
  /* USER CODE END 5 */
}

/* USER CODE BEGIN Header_motors */
/**
* @brief Function implementing the MotorTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_motors */
void motors(void *argument)
{
  /* USER CODE BEGIN motors */
  /* Infinite loop */
	//start dc motor
	//HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_1);
	//clockwise
	//HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
	//HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
	//__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1000);
	//anti-clockwise
	//HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	//HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	//servo motor
	//HAL_TIM_PWM_Start(&htim1, TIM_CHANNEL_4);
	//htim1.Instance->CCR4 = 100; extreme right
	//osDelay(4000);
	//htim1.Instance->CCR4 = 72;
	//htim1.Instance->CCR4 = 72; center
	//htim1.Instance->CCR4 = 52 ; extreme left
	//osDelay(3150);
	//htim1.Instance->CCR4 = 75;
	HAL_TIM_PWM_Start(&htim1, TIM_CHANNEL_4);
	HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_1);
	HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_2);
	//htim1.Instance->CCR4 = 60;
	//osDelay(1000);
	htim1.Instance->CCR4 = 73;
	HCSR04_Read();
	osDelay(1000);
	 HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
     HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	uint8_t dis[10]=":MOVED:";
	uint32_t deLay;
	uint32_t hello[20];
	int b=0;
	int a;
	int c=0;


	//gO= (Distance - 50)*100 ;

  for(;;)
  {
	//  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
	 // __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
	 // osDelay(6000);
	  	//  htim1.Instance->CCR4 = 60;
	  //	  	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 800);
	  //	  	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 1400);
	  //	  	osDelay(4000);
	  	  //LEFT TURN
	/*  htim1.Instance->CCR4 = 113 ;
		  	  	  	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1400);
		  	  	  	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 800);
		  	  	  	  osDelay(200);
		  	  	  htim1.Instance->CCR4 = 54;
		  	  	  	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 800);
		  	  	  	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 1400);
		  	  	  	osDelay(2900);
		  	  			HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
		  	  			 HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
		  	  			 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
		  	  			 HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
		  	  			htim1.Instance->CCR4 = 113;
		  		 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
		  		 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
		  		 	 osDelay(200);
		  	  			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1400);
		  	  			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 600);
		  	  			osDelay(500);
		  	  		HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
		  	  		     HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
		  	  			 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
		  	  			HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
		 	  			htim1.Instance->CCR4 = 73;
		 	 	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
		 	 	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
		 	 	 	 osDelay(200);
		 	  			__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1400);
		 	  			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 700);
		 	  			  osDelay(160);
		 	  			 htim1.Instance->CCR4 = 73;*/
	 	  			 //RIGHT TURN
	/*  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
	 	  osDelay(300);
	 	  htim1.Instance->CCR4 = 113 ;
	 	//  HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	 	 // HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1500);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 600);
	 	  osDelay(2500);

	 	  HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
	 	  HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
	 	  HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	 	  HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	 	  htim1.Instance->CCR4 = 54;
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
	 	 osDelay(200);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 900);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 1400);
	 	  osDelay(680);
	 	  htim1.Instance->CCR4 = 73;
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
	 	  osDelay(200);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1510);
	 	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 900);

	 	  HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	 	  HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	 	  HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	 	  HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	 	  osDelay(630);
	 	  htim1.Instance->CCR4 = 73;*/
	 	 // __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1510);
	 	 // __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 900);
	 	 // osDelay(100);
	  		// OLED_ShowString(10,40,hello);
	//  HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_1);
	//  HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_2);
		/* __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
		 osDelay(2000);*/
	  // htim1.Instance->CCR4=110; //30 cm left turn (lab)
	  //  osDelay(1000);
	//    htim1.Instance->CCR4=73;
	 //   osDelay(1000);
	 	//	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1200);
	 //	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 500);
	 //		 osDelay(4550);
	//	 htim1.Instance->CCR4 = 73 ;
		// __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1500);
	//	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 900);

	//  deLay=(gO/185)*100;
	//  osDelay(deLay);
	//  sprintf(dis,"%d",deLay);
	//  OLED_ShowString(0,0,dis);


		// osDelay(5800);
		 //htim1.Instance->CCR4 = 73;
		 //osDelay(5000);
	 // osDelay(2950,6000,9000,12200); 90,180,270,360 - R
		//osDelay(2950,5950,9000,12250) lab
	  // osDelay(3900,7100,10300,13790); 90,180,270,360 -L
		 //osDelay(4000,7800,11700,)
	//  HAL_TIM_PWM_Stop(&htim8, TIM_CHANNEL_1);
	  //HAL_TIM_PWM_Stop(&htim8, TIM_CHANNEL_2);
	//	osDelay(1000);
		//HAL_TIM_PWM_Stop(&htim1, TIM_CHANNEL_4);
	 x=instruction[b];
	  switch(x){
	 case 'f':

		 move=0;

		for(a=1;a<4;a++){

			move=move*10 + (instruction[b+a]-'0');

		}

			gO=move*100;

		sprintf(hello,"%c%d",x,move);
		OLED_ShowString(20,20,hello);
		HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
		if(gO>0){
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
		}
		// OLED_ShowString(10,40,hello);
		 htim1.Instance->CCR4 = 73;
		// osDelay(50);
		// deLay=(gO/185)*100;
	//	 osDelay(deLay);
		// sprintf(hello,"%d",deLay);
		// OLED_ShowString(0,0,hello);
		 if(Dist+stop>=gO){
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
		 osDelay(50);
		 Dist=0;
		 b=b+4;
		 //:MOVED:
		 HAL_UART_Transmit(&huart3,(uint8_t *)dis,7,0xFFFF);
		 }
		 break;
	 case 'b':

		// x='s';
		 move=0;
		 for(a=1;a<4;a++){

		 	move=move*10 + (instruction[b+a]-'0');

		 	}
		 gO=move*100;
		sprintf(hello,"%c%d",x,move);
		OLED_ShowString(20,20,hello);
		 HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
		 HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
		 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
		 HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
		 htim1.Instance->CCR4 = 73;
		// deLay=(gO/185)*100;
		// osDelay(deLay);

		// __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
	//	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);

		 if(Dist+stop>=gO){
		 		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
		 		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
		 		 osDelay(50);
		 		 Dist=0;
		 		 b=b+4;
		 		HAL_UART_Transmit(&huart3,(uint8_t *)dis,7,0xFFFF);
		 		 }

		 break;
	 case 'l':

	 		// x='s';
	 		 move=0;
	 		 for(a=1;a<4;a++){

	 		 	move=move*10 + (instruction[b+a]-'0');

	 		 	}
	 			sprintf(hello,"%c%d",x,move);
	 			OLED_ShowString(20,20,hello);
	 	//			htim1.Instance->CCR4 = 54 ;
	 	  //	  	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1000);
	 	  	//  	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 1200);

	 		// osDelay(4800);

	 		 //180 - 2.06
	 		 //270 - 3.12
	 		 //360 - 4.20162
	 		 if(move==0){

	 			 htim1.Instance->CCR4 = 113 ;
	 				  	  	  	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1400);
	 				  	  	  	  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 800);
	 				  	  	  	  osDelay(220);
	 				  	  	  htim1.Instance->CCR4 = 54;
	 				  	  	  	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 800);
	 				  	  	  	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 1400);
	 				  	  	  	osDelay(2900);
	 				  	  			HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
	 				  	  			 HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
	 				  	  			 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	 				  	  			 HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	 				  	  			htim1.Instance->CCR4 = 113;
	 				  	  			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1400);
	 				  	  			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 600);
	 				  	  			osDelay(1255);
	 				  	  		HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	 				  	  		     HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	 				  	  			 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	 				  	  			HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	 				 	  			htim1.Instance->CCR4 = 73;
	 				 	  			osDelay(200);
	 				 	  			__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
	 				 	  			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
	 				 	  			  osDelay(410);
	 				 	  			 htim1.Instance->CCR4 = 73;
	 		 }
	 		 if(move==1){
	 			 osDelay(7200);
	 		 }
	 		 if(move==2){
	 			 osDelay(10800);
	 		 }
	 		 if(move==3){
	 			 osDelay(14000);
	 		 }
	 		 ////osDelay(4000,7800,11700,)
	 		 htim1.Instance->CCR4 = 73;

	 		    b=b+4;
	 		  HAL_UART_Transmit(&huart3,(uint8_t *)dis,7,0xFFFF);
	 		 break;
	 	 case 'r':
	 		// x='s';
	 		 move=0;
	 		 for(a=1;a<4;a++){

	 		 	move=move*10 + (instruction[b+a]-'0');

	 		 	}
	 			sprintf(hello,"%c%d",x,move);
	 			OLED_ShowString(20,20,hello);
	 	//		htim1.Instance->CCR4 = 110 ;
	 		// __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1200);
	 		// __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 500);
	 		// htim1.Instance->CCR4 = 110 ;
	 		 //180 - 2.04
	 		 //270 - 3.06
	 		 //360 - 4.1
	 		 if(move==0){
	 			 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
	 				  osDelay(460);
	 				  htim1.Instance->CCR4 = 113 ;
	 				//  HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	 				 // HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 1500);
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 600);
	 				  osDelay(2500);
	 				  HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
	 				  HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
	 				  HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	 				  HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	 				  htim1.Instance->CCR4 = 54;
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 500);
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 2000);
	 				  osDelay(1030);
	 				  htim1.Instance->CCR4 = 73;
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
	 				  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);

	 				  HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	 				  HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	 				  HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	 				  HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	 				  osDelay(623);
	 				  htim1.Instance->CCR4 = 73;

	 		 }
	 		 if(move==1){
	 			 osDelay(6300);
	 		 }
	 		 if(move==2){
	 			 osDelay(9500);
	 		 }
	 		 if(move==3){
	 			 osDelay(12250);
	 		 }
	 		// //osDelay(2950,5950,9000,12250)
	 		 htim1.Instance->CCR4 = 73;

	 		    b=b+4;

	 		    HAL_UART_Transmit(&huart3,(uint8_t *)dis,7,0xFFFF);
	 		 break;

	 case 'n':
		 instruction[b]=0;
		// x='s';
		 move=0;
		 for(a=1;a<4;a++){

		 move=move*10 + (instruction[b+a]-'0');

		 	}
			sprintf(hello,"%c%d",x,gO);
			OLED_ShowString(20,20,hello);
		 if(move==0){
			 navigate();
		 	}
		 if(move==1){
			 navigate();
			 navigate();
		 	}
		 if(move==2){
			 navigate();
			 navigate();
			 navigate();
		 	 }
		 if(move==3){
			 navigate();
			 navigate();
			 navigate();
			 navigate();
		  	 }

		    b=b+4;
		    HAL_UART_Transmit(&huart3,(uint8_t *)dis,7,0xFFFF);

		 break;

	 case 's':

		  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
		 osDelay(2000);
		    b=b+4;
		    HAL_UART_Transmit(&huart3,(uint8_t *)dis,7,0xFFFF);

		 break;
	 default:
		  __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
		 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);

		 break;
	 }

    osDelay(100);
   // aRxBuffer[0]='s';


//	osDelay(5000);
//	speed1=speed2=0;
  }
  /* USER CODE END motors */
}

/* USER CODE BEGIN Header_encoder */
/**
* @brief Function implementing the Encoder thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_encoder */
void encoder(void *argument)
{
  /* USER CODE BEGIN encoder */
  /* Infinite loop */
	HAL_TIM_Encoder_Start(&htim2,TIM_CHANNEL_ALL);
	HAL_TIM_Encoder_Start(&htim3,TIM_CHANNEL_ALL);
	int a,b,c,d,e,f;
	uint32_t tick;
	a = __HAL_TIM_GET_COUNTER(&htim2);
	d = __HAL_TIM_GET_COUNTER(&htim3);
	tick=HAL_GetTick();
//	OLED_Display_On();
	uint8_t dis[4];
  for(;;)
  {
	  if(HAL_GetTick()-tick > 100L){
		  b =__HAL_TIM_GET_COUNTER(&htim2);
		  e = __HAL_TIM_GET_COUNTER(&htim3);
		 		  if(__HAL_TIM_IS_TIM_COUNTING_DOWN(&htim2)){
		 			  if(b<a){
		 				  c=a-b;
		 			  }
		 			  else{
		 				  c= (65535 - b)+a;
		 			  }
		 	  }
		 	  else{
		 		  if(b>a){
		 			  c=b-a;
		 		  }
		 		  else{
		 			  c= (65535-a)+b;
		 		  }
		  }
		 	if(__HAL_TIM_IS_TIM_COUNTING_DOWN(&htim3)){
		 		 		 			  if(e<d){
		 		 		 				  f=d-e;
		 		 		 			  }
		 		 		 			  else{
		 		 		 				  f= (65535 - e)+d;
		 		 		 			  }
		 		 		 	  }
		 		 		 	  else{
		 		 		 		  if(e>d){
		 		 		 			  f=e-d;
		 		 		 		  }
		 		 		 		  else{
		 		 		 			  f= (65535-d)+e;
		 		 		 		  }
		 		 		  }
		 	a=__HAL_TIM_GET_COUNTER(&htim2);
		 	d = __HAL_TIM_GET_COUNTER(&htim3);
		    tick=HAL_GetTick();
		   // if(c!=65535)

		    if(c<60000 && (x=='f'||x=='b')){
		   Dist=(Dist+((c*10)/1.28)/6);
		   stop=((c*10)/1.28)/6;

		    }
		   sprintf(dis,"%5d %5d",c,Dist);
		   OLED_ShowString(10,10,dis);

		  // move=move*10;

		    }
		    	// OLED_ShowString(10,10,dis);
		    	//  OLED_Refresh_Gram();
	  }





	  }


  /* USER CODE END encoder */

/* USER CODE BEGIN Header_show */
/**
* @brief Function implementing the showtask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_show */
void show(void *argument)
{
  /* USER CODE BEGIN show */
  /* Infinite loop */
	int a=0;
	uint32_t hello[5];
	uint16_t b;
	uint32_t dis[20];
	int distance;
	 HAL_TIM_Base_Start(&htim4);
	 HAL_TIM_IC_Start_IT(&htim4, TIM_CHANNEL_1);


  for(;;)
  {

	//  HCSR04_Read();
	 // dist=Distance;
	 // sprintf(hello,"%d",dist);


	//  sprintf(dis," %d",a);
	//  OLED_ShowString(10,30,dis);
	//  OLED_Refresh_Gram();
	//  osDelay(200);
	  HAL_ADC_Start(&hadc1);
	  HAL_ADC_PollForConversion(&hadc1,1000);
	  b= HAL_ADC_GetValue(&hadc1);
	  HAL_ADC_Stop(&hadc1);
	  //a=a*100;
	 // distance  = a* .034/2;
	/*  if(b>2300){
	  dist=b*-0.004+22;
	  }
	  else{

		  dist = (3308-b)/85;

	  }
	  if(dist>23){
		  dist=dist+2;
	  }*/
	 dist= (0.0000313111 *pow(b,2))-(0.230545* b)+493.974;
	  sprintf(hello,"%d %d",dist,b);
	  OLED_ShowString(10,30,hello);

	/*  if(Distance<50 && Distance>0){
	 		 htim1.Instance->CCR4 = 52;
	 		 osDelay(3150);
	 		 htim1.Instance->CCR4 = 75;
	 	 }*/
	//e  osDelay(100);
	  OLED_Refresh_Gram();
	//  if(dist<15){
	//	  speed1=0;
	///	  speed2=0;
	//	  HAL_TIM_PWM_Stop(&htim8, TIM_CHANNEL_1);
	//	  HAL_TIM_PWM_Stop(&htim8, TIM_CHANNEL_2);
	//  }*/


	  //osDelay(100);

  }

  /* USER CODE END show */
}

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */


// Let's write the callback function

void HAL_TIM_IC_CaptureCallback(TIM_HandleTypeDef *htim)
{

	if (htim->Channel == HAL_TIM_ACTIVE_CHANNEL_1)  // if the interrupt source is channel1
	{
		if (Is_First_Captured==0) // if the first value is not captured
		{
			IC_Val1 = HAL_TIM_ReadCapturedValue(htim, TIM_CHANNEL_1); // read the first value
			Is_First_Captured = 1;  // set the first captured as true
			// Now change the polarity to falling edge
			__HAL_TIM_SET_CAPTUREPOLARITY(htim, TIM_CHANNEL_1, TIM_INPUTCHANNELPOLARITY_FALLING);
		}

		else if (Is_First_Captured==1)   // if the first is already captured
		{
			IC_Val2 = HAL_TIM_ReadCapturedValue(htim, TIM_CHANNEL_1);  // read second value
			__HAL_TIM_SET_COUNTER(htim, 0);  // reset the counter

			if (IC_Val2 > IC_Val1)
			{
				Difference = IC_Val2-IC_Val1;
			}

			else if (IC_Val1 > IC_Val2)
			{
				Difference = (0xffff - IC_Val1) + IC_Val2;
			}

			Distance = Difference * .034/2;
			Is_First_Captured = 0; // set it back to false

			// set polarity to rising edge
			__HAL_TIM_SET_CAPTUREPOLARITY(htim, TIM_CHANNEL_1, TIM_INPUTCHANNELPOLARITY_RISING);
			__HAL_TIM_DISABLE_IT(&htim4, TIM_IT_CC1);
		}
	}
}
void HCSR04_Read (void)
{
	HAL_GPIO_WritePin(TRIG_PORT, TRIG_PIN, GPIO_PIN_SET);  // pull the TRIG pin HIGH
	delay(10);  // wait for 10 us
	HAL_GPIO_WritePin(TRIG_PORT, TRIG_PIN, GPIO_PIN_RESET);  // pull the TRIG pin low

	__HAL_TIM_ENABLE_IT(&htim4, TIM_IT_CC1);
}
void delay (uint16_t time)
{
	__HAL_TIM_SET_COUNTER(&htim4, 0);
	while (__HAL_TIM_GET_COUNTER (&htim4) < time);
}
void navigate (){
 	  osDelay(1000);
  	  HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_1);
  	HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_2);
  	  HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
       HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
  	 HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
  	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
  	//htim1.Instance->CCR4 = 110;
	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, speed1);
	__HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, speed2);
	osDelay(300);
	htim1.Instance->CCR4 = 57;
	HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	osDelay(2000);
	htim1.Instance->CCR4 = 72;
	osDelay(2000);
	htim1.Instance->CCR4 = 55;
	HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	osDelay(1700);
	HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	htim1.Instance->CCR4 = 100;
	osDelay(1000);
	htim1.Instance->CCR4 = 72;
	osDelay(1600);
	htim1.Instance->CCR4 = 55;
	HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_RESET);
	osDelay(3520);
	htim1.Instance->CCR4 = 72;
	HAL_GPIO_WritePin(GPIOA,AIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,AIN1_Pin,GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOA,BIN2_Pin,GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOA,BIN1_Pin,GPIO_PIN_SET);
	//osDelay(600);
	htim1.Instance->CCR4 = 72;
	osDelay(300);
	//htim1.Instance->CCR4 = 72;

	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_1, 0);
	 __HAL_TIM_SET_COMPARE(&htim8, TIM_CHANNEL_2, 0);
  //	htim1.Instance->CCR4 = 75;
  	osDelay(1000);
}
void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart){
	int a;
	HAL_UART_Transmit(&huart3,(uint8_t *)aRxBuffer,10,0xFFFF);
	for(a=0;a<4;a++){
		instruction[y]=aRxBuffer[a];
		y++;
	}
	instruction[y]='\0';
		 //sprintf(hello,"%s\0 %d ",cmd);
	//OLED_ShowString(10,40,hello);
		// OLED_Refresh();
		// x/=1000;

}

void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */
  __disable_irq();
  while (1)
  {
  }
  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

