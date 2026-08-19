# 초텐 튜너 (Choten Tuner) ♪

도스/Win95 감성 + 핑크핑크 귀염뽀짝 레트로 테마의 안드로이드 조율기(튜너) 앱.
초텐짱 프로젝트(choten-translate)의 디자인 언어와 캐릭터를 그대로 이어받았습니다.

## 기능

- **튜너 모드**: 마이크로 소리를 듣고 가장 가까운 음이름(C, C#, … B)과
  센트(cent) 단위 편차(-50 ~ +50)를 아날로그 바늘 게이지로 표시.
  YIN 피치 검출 + 포물선 보간, 미디언 스무딩.
- **소리내기 모드**: 원하는 음(옥타브 2~6, 12음)을 골라 기준음을 계속 재생.
  클릭 없는 사인파, 음 전환 시 부드러운 포르타멘토.
- **변신 버튼**: 라이트(초텐짱) ↔ 다크(아메) 테마 전환. 팔레트·캐릭터·말투가
  통째로 바뀝니다. 설정은 저장됨.
- **마스코트**: 화면 아래에서 캐릭터 상반신이 상태(대기/완벽/삐끗/재생중)에 따라
  둥실둥실 움직이고, 대사창(도스식 타자기 효과)으로 말을 겁니다. 터치하면 반응!
- **설정**: A4 기준 주파수 보정(435~445Hz).

## 기술 스택

- Kotlin + Jetpack Compose (Material 컴포넌트 없이 커스텀 레트로 위젯)
- minSdk 24 / targetSdk 34, AGP 8.5.2, Gradle 8.7
- 오디오: `AudioRecord`(44.1kHz 모노 캡처) / `AudioTrack`(스트리밍 사인파)
- 피치 검출: YIN (de Cheveigné & Kawahara, 2002) 자체 구현
- 폰트: [Galmuri](https://github.com/quiple/galmuri) (SIL OFL 1.1) — 도스 시절 둥근모꼴 계열 픽셀 폰트

## 빌드

```
gradlew.bat :app:assembleDebug
```

APK 출력: `app/build/outputs/apk/debug/app-debug.apk`

이 PC 특이사항: 보안 프로그램의 SSL 검사 때문에 `gradle.properties`의
`-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT` 플래그가 필요합니다(이미 설정됨).

## 테스트

DSP 코어(음정 수학, YIN 검출기)는 합성 파형 기반 JVM 유닛테스트(17개)로 검증합니다.

```
gradlew.bat :app:testDebugUnitTest
```

## 에뮬레이터로 화면 확인

AVD `choten`(pixel_4, android-34 x86_64, AEHD 가속)이 만들어져 있습니다.

```
C:\Android\Sdk\emulator\emulator.exe -avd choten
```

## 디자인 테마 (초텐짱 / 아메)

| 토큰 | 라이트(초텐짱) | 다크(아메) |
|---|---|---|
| bg | `#f8f0ff` | `#221c2b` |
| pink | `#ffb7e5` | `#d16b8a` |
| purple | `#d1b3ff` | `#8f79c9` |
| border | `#4a4a4a` | `#efe6ff` |
| mint | `#a9f0cb` / `#2fbf77` | `#3e8f6c` / `#5ad695` |

체커보드 배경, 3px 보더 + 하드 섀도 창, 그라데이션 타이틀바(─ □ ✕),
픽셀 폰트, 꾹 눌리는 레트로 버튼이 시그니처.

## 주의

`app/src/main/res/drawable-nodpi/`의 캐릭터 일러스트는 팬 컨텐츠 소재이므로
이 저장소는 비공개(private) 유지를 권장합니다.

made by 간지김 ♥
