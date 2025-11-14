# IoT 실시간 데이터 모니터링 데모 앱
## 프로젝트 개요
실시간 소켓 통신을 시뮬레이션하는 IoT 기기 모니터링 및 제어 애플리케이션입니다.
Circuit, Hilt, Coroutine Flow를 활용한 앱 아키텍처 기반 Android 앱입니다.

최근에 수행한 프로젝트 코드들의 경우 인턴십 과정중 비밀유지계약으로 인해 코드를 공개할 수 없어
단일 피처로 별도로 제작된 샘플 코드를 작성하였습니다.

실제 서비스의 경우 IoT 스마트 화분 기기와의 Wi-Fi/Socket 통신 연결을 수행하는 것을 
본 코드에서는 임시 값을 이용한 시뮬레이션으로 구현하였습니다.

```
/프로젝트 최상위 (com.example.sample)
├── di (Global Dependency Injection)
│   └── CircuitModule.kt           # Circuit Presenter/UI 팩토리 등록
│
├── initializer (App Setup)
│   └── LoggerInitializer.kt           # 앱 시작 시 Logger 초기화
│
├── model (Domain Layer / Business Entities)
│   ├── ControlState.kt            # 기기 제어 상태
│   ├── DashBoardState.kt          # 센서 대시보드 데이터
│   ├── DeviceNetworkInfo.kt       # Wi-Fi 정보 및 기기 메타데이터
│   ├── DeviceStatus.kt            # 기기의 현재 실시간 상태 
│   └── RawDeviceData.kt           # 소켓으로 수신된 원시 JSON 데이터 모델
│
├── ui (Presentation Layer / Circuit)
│   ├── home                       # Home Feature 모듈
│   │   ├── HomePresenter.kt       # Home Circuit Presenter
│   │   ├── HomeUi.kt              # Compose UI 
│   │   └── HomeUiState.kt         # 화면 상태 및 이벤트 정의
│   └── Screens.kt                 # Circuit Screen 정의 
│
├── data (Data Layer / Repository)
│   ├── repository
│   │   ├── api
│   │   │   └── DeviceConnectionRepository.kt  # Repository 인터페이스
│   │   └── impl
│   │       └── DeviceConnectionRepositoryImpl.kt  # Repository 구현체
│   └── di
│       ├── DataModule.kt            # Repository 바인딩
│       ├── CoroutineScopeModule.kt  # ApplicationScope 제공
│       └── DataQualifier.kt         # Scope Qualifier 정의
│
└── p2p (Network / External Layer)
├── controller
│   ├── api
│   │   ├── SocketController.kt    # 소켓 통신 API
│   │   └── WifiController.kt      # Wi-Fi 연결 API
│   └── impl
│       └── FakeSocketController.kt  # ★ 데모용 가짜 소켓 컨트롤러
└── di
└── P2pModule.kt           # Socket/WifiController DI (Fake 구현체 주입)
```
## 핵심 기능
### 1. 실시간 상태 관리

ConnectionState: Sealed Interface로 연결 상태 관리
```
Disconnected: 연결 끊김
Connecting: 연결 시도 중
Connected: 연결 완료
Failed: 연결 실패 (에러 메시지 포함)
```

### 2. 양방향 통신 시뮬레이션

수신: FakeSocketController가 1초마다 랜덤 센서 데이터 생성
송신: 제어 명령을 JSON으로 직렬화하여 전송 시뮬레이션

### 3. 인터랙티브 제어

팬, LED, 워터펌프 상태를 터치로 토글, 제어 결과를 Snackbar로 즉시 피드백   
실제로는 컨트롤 정보를 IoT기기에 전송후 소켓 값을 피드백하여 업데이트하지만 본 코드에서는 반영되지 않았습니다.

### 4. SideEffect 패턴

HandleHomeSideEffects로 UI 부수효과 분리
Snackbar 표시 후 자동으로 상태 소비

### 5. `@Named` 바인딩을 이용한 재바인딩   

객체 지향 환경에서 내부 구현체를 internal로 선언하여 외부에서 접근할 수 없도록 막아야 합니다.   
그러나 public hilt 모듈은 internal 파라미터를 노출할 수 없다는 문제점이 있습니다.   
이 문제를 `@Named` 바인딩 방식을 통해 내부 구조 internal 유지 및 테스트 환경에서의 대체를 원활하게 만들었습니다.

## 주요 기술 스택
```
UI : Jetpack Compose, Material3
Architecture : Circuit, MVI, App Architecture
DI :  Hilt
비동기 : Kotlin Coroutines, Flow
직렬화 : Kotlinx Serialization
로깅 : Logger (orhanobut)
```
